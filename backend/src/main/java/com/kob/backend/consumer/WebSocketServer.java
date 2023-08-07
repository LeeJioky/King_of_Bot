package com.kob.backend.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.kob.backend.consumer.utils.Game;
import com.kob.backend.consumer.utils.JwtAuthentication;
import com.kob.backend.mapper.RecordMapper;
import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@ServerEndpoint("/websocket/{token}")  // 注意不要以'/'结尾
public class WebSocketServer {
    private Session session = null;
    private User user;
    private static UserMapper userMapper;
    public static RecordMapper recordMapper;
    private Game game = null;


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

    private void stopMatching(){
        System.out.println("stop matching");
        matchpool.remove(this.user);
    }

    private void startMatching(){
        System.out.println("start matching");
        matchpool.add(this.user);
        while (matchpool.size()>=2){
            Iterator<User> iterator = matchpool.iterator();
            User userA = iterator.next();
            User userB = iterator.next();
            matchpool.remove(userA);
            matchpool.remove(userB);
            Game game = new Game(13,14,20, userA.getId(), userB.getId());
            game.createMap();
            users.get(userA.getId()).game = game;
            users.get(userB.getId()).game = game;

            game.start();

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
            users.get(userA.getId()).sendMessage(respA.toJSONString());

            JSONObject respB = new JSONObject();
            respB.put("event", "start-matching");
            respB.put("opponent_username", userA.getUsername());
            respB.put("opponent_photo", userA.getPhoto());
            respB.put("game", respGame);
            users.get((userB.getId())).sendMessage(respB.toJSONString());
        }
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
            matchpool.remove(this.user);
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
            startMatching();
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
            game.setNextStepA(direction);
//            System.out.println("我是玩家A");
        }else if(game.getPlayerB().getId().equals(user.getId())){
            game.setNextStepB(direction);
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