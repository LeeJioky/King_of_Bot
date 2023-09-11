package com.kob.botrunningsystem.service.impl.utils;

import com.kob.botrunningsystem.utils.BotInterface;
import org.joor.Reflect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class Consumer extends Thread {
    private static RestTemplate restTemplate;
    private Bot bot;
    private static final String receiveBotMoveUrl = "http://127.0.0.1:3000/pk/receive/bot/move/";

    @Autowired
    public void setRestTemplate (RestTemplate restTemplate){
        Consumer.restTemplate = restTemplate;
    }

    public void startTimeout(long timeout, Bot bot){
        this.bot = bot;
        this.start();//调用start()会开辟一个新线程来执行run()
        //当前线程继续执行
        try{
            this.join(timeout);//最多等待timeout秒
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            this.interrupt();//一旦线程执行完毕，或者超过timeout秒，中断当前线程
        }
    }

    private String addUid(String code, String uid){//在BotCode的Bot类名之后添加uid
//        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotInterface");
        int k = code.indexOf(" implements java.util.function.Supplier<Integer>");
        return code.substring(0, k) + uid + code.substring(k);
    }

    @Override
    public void run(){
        UUID uuid = UUID.randomUUID();
        //类名之前添加一个随机字符串
        String uid = uuid.toString().substring(0, 8);

        Supplier<Integer> botInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.Bot" + uid,
                addUid(bot.getBotCode(),uid)
        ).create().get();//创建类后获取

//        BotInterface botInterface = Reflect.compile(
//                "com.kob.botrunningsystem.utils.Bot" + uid,
//                addUid(bot.getBotCode(),uid)
//        ).create().get();//创建类后获取

        //通过文件读取参数
        File file = new File("input.txt");
        try(PrintWriter fout = new PrintWriter(file)){
            fout.println(bot.getInput());
            fout.flush();
        } catch (FileNotFoundException e){
            throw new RuntimeException(e);
        }

//        Integer direction = botInterface.nextMove(bot.getInput());
        Integer direction = botInterface.get();
        System.out.println("bot-userId "  + bot.getUserId() + " move direction " + direction);

        //通过RestTemplate将结果返回主服务器
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", bot.getUserId().toString());
        data.add("direction", direction.toString());
        restTemplate.postForObject(receiveBotMoveUrl, data, String.class);
    }
}
