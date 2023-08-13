package com.kob.matchingsystem.service.impl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

//为了能让RestTemplateCofig中的Bean注入进来，添加@Component
@Component
public class MatchingPool extends Thread{
    private static List<Player> players = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    private static RestTemplate restTemplate;

    private final static String startGameUrl = "http://127.0.0.1:3000/pk/start/game/";

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate){
        MatchingPool.restTemplate = restTemplate;
    }

    public void addPlayer(Integer userId, Integer rating, Integer botId){
        lock.lock();
        try{
            players.add(new Player(userId, rating, botId, 0));
        }finally {
            lock.unlock();
        }
    }

    public void removePlayer(Integer userId){
        lock.lock();
        try {
            for (int i = 0; i<players.size(); i++){
                if (players.get(i).getUserId().equals(userId)){
                    players.remove(i);
                    i--;//注意指针要回退一下，这样i++之后重新判断这一位置上
                }
            }
        }finally {
            lock.unlock();
        }
    }

    private void increaseWaitingTime(){
        for (Player player:players){
            player.setWaitingTime(player.getWaitingTime()+1);
        }
    }

    private boolean checkMatched(Player a, Player b){//判断两名玩家是否匹配
        int ratingDelta = Math.abs(a.getRating() - b.getRating());
        int waitingTime = Math.min(a.getWaitingTime(), b.getWaitingTime());
        return ratingDelta<=waitingTime*10;
    }

    private void sendResult(Player a, Player b){
        //若a和b匹配，则作为参数返回到backend
        System.out.println("send result "+ a + " " + b);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("a_id", a.getUserId().toString());
        data.add("a_bot_id", a.getBotId().toString());
        data.add("b_id", b.getUserId().toString());
        data.add("b_bot_id", b.getBotId().toString());
        restTemplate.postForObject(startGameUrl, data, String.class);
    }

    private void matchPlayers(){//尝试匹配所有玩家
        System.out.println("match players"+players.toString());
        boolean[] used = new boolean[players.size()];
        //优先匹配等待时间比较长的玩家
        //也就是最早add的那一批开始考虑
        for(int i=0; i<players.size(); i++){
            if(used[i]) continue;
            for (int j=i+1;j<players.size();j++){
                if (used[j]) continue;
                Player a = players.get(i);
                Player b = players.get(j);
                if(checkMatched(a,b)){
                    used[i] = used[j] = true;
                    sendResult(a,b);
                    break;
                }
            }
        }

        //更新players 删除已经匹配过的玩家
        for(int i = 0; i<players.size(); i++){
            if (used[i]){
                players.remove(i);
                i--;//back one step
            }
        }
    }

    @Override
    public void run(){
        while(true){
            try {
                Thread.sleep(1000);
                lock.lock();
                try{
                    increaseWaitingTime();
                    matchPlayers();
                }finally {
                    lock.unlock();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
                break;
            }
        }
    }
}
