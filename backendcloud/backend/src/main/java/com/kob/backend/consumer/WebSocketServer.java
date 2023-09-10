package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.BotMapper;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    private Session session = null;
    private User user;
    public static UserMapper userMapper;
    private static BotMapper botMapper;
    public static RecordMapper recordMapper;
    public static RestTemplate restTemplate;

    public Game game = null;

    private final static String addPlayerUrl = "http://127.0.0.1:3001/player/add/";
    private final static String removePlayerUrl = "http://127.0.0.1:3001/player/remove/";

    //需要建立一个线程安全的Set作为匹配池
    private static CopyOnWriteArrayList<User>
            matchpool = new CopyOnWriteArrayList<>();

    public static ConcurrentHashMap<Integer, WebSocketServer>
            users = new ConcurrentHashMap<>();

    //由于WebSocket不属于Spring的一个组件，不是单例模式，因此，注入mapper的方式有些区别
    @Autowired
    public void setUserMapper(UserMapper userMapper){
        WebSocketServer.userMapper = userMapper;
    }

    @Autowired
    public void setRecordMapper(RecordMapper recordMapper){
        WebSocketServer.recordMapper = recordMapper;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        WebSocketServer.restTemplate = restTemplate;
    }

    @Autowired
    public void setBotMapper(BotMapper botMapper){
        WebSocketServer.botMapper = botMapper;
    }

    public void sendMessage(String message){
        //Server发送消息
        synchronized (this.session){
            try{
                this.session.getBasicRemote().sendText(message);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void startGame(Integer aId, Integer aBotId, Integer bId, Integer bBotId){
        User userA = userMapper.selectById(aId);
        User userB = userMapper.selectById(bId);
        Bot botA = botMapper.selectById(aBotId);
        Bot botB = botMapper.selectById(bBotId);
        Game game = new Game(13,14,20,userA.getId(),botA, userB.getId(), botB);
        game.createMap();

        //game是属于A和B两个玩家，因此需要赋值给A和B两名玩家的对应连接上
        if(users.get(userA.getId())!=null){
            users.get(userA.getId()).game = game;
        }
        if(users.get(userB.getId())!=null){
            users.get(userB.getId()).game = game;
        }

        game.start();//开辟一个新线程

        JSONObject respGame = new JSONObject();
        respGame.put("a_id", game.getPlayerA().getId());
        respGame.put("a_sx", game.getPlayerA().getSx());
        respGame.put("a_sy", game.getPlayerA().getSy());
        respGame.put("b_id", game.getPlayerB().getId());
        respGame.put("b_sx", game.getPlayerB().getSx());
        respGame.put("b_sy", game.getPlayerB().getSy());
        respGame.put("gamemap", game.getG());

        JSONObject respA = new JSONObject();
        respA.put("event", "start-matching");
        respA.put("opponent_username", userB.getUsername());
        respA.put("opponent_photo", userB.getPhoto());
        respA.put("game", respGame);
        WebSocketServer webSocketServerA = users.get(userA.getId());
        if(webSocketServerA != null){
            webSocketServerA.sendMessage(respA.toJSONString());
        }

        JSONObject respB = new JSONObject();
        respB.put("event", "start-matching");
        respB.put("opponent_username", userA.getUsername());
        respB.put("opponent_photo", userA.getPhoto());
        respB.put("game", respGame);
        WebSocketServer webSocketServerB = users.get(userB.getId());
        if(webSocketServerB != null){
            webSocketServerB.sendMessage(respB.toJSONString());
        }
    }

    private void startMatching(Integer botId){
        System.out.println("start matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        data.add("rating", this.user.getRating().toString());
        data.add("bot_id", botId.toString());
        //向MatchingSystem发请求
        restTemplate.postForObject(addPlayerUrl, data, String.class);
    }

    private void stopMatching(){
        System.out.println("stop matching");
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", this.user.getId().toString());
        restTemplate.postForObject(removePlayerUrl, data, String.class);
    }

    //在建立连接时，需要建立用户ID与WebSocketServer实例的映射
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) throws IOException {
        // 建立连接时自动调用
        this.session = session;
        System.out.println("connected");
//        int userId = Integer.parseInt(token);//假设token为userId
        int userId = JwtAuthentication.getUserId(token);
        this.user = userMapper.selectById(userId);
        if(this.user!=null){
//            System.out.println(this.user);
            users.put(userId, this);
        }else {
            this.session.close();
        }
        users.put(userId, this);
    }

    //在关闭连接时，删除这种映射
    @OnClose
    public void onClose() {
        // 关闭链接时自动调用
        System.out.println("Disconnected");
        if(this.user != null){
            users.remove(this.user.getId());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 从Client接收消息时触发
        System.out.println("Receive message");
        JSONObject data = JSONObject.parseObject(message);
        String event = data.getString("event");
        System.out.println(event);
        if("start-matching".equals(event)){
            startMatching(data.getInteger("bot_id"));
        }else if("stop-matching".equals(event)){
            stopMatching();
        }else if ("move".equals(event)){
            Integer direction = data.getInteger("direction");
            System.out.println(direction);
            move(direction);
        }
    }

    private void move(Integer direction){
        //判断是哪个玩家在操作
        if(game.getPlayerA().getId().equals(user.getId())){
            if(game.getPlayerA().getBotId().equals(-1)){
                game.setNextStepA(direction);
            }
//            System.out.println("我是玩家A");
        }else if(game.getPlayerB().getId().equals(user.getId())){
            if(game.getPlayerB().getBotId().equals(-1)){
                game.setNextStepB(direction);
            }
//            System.out.println("我是玩家B");
        }else {
            Exception e = new Exception("Error");
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }
}