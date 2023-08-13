package com.kob.botrunningsystem.service.impl.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BotPool extends Thread{
    private static final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<Bot> bots = new LinkedList<>();
    public void addBot(Integer userId, String botCode, String input){
        lock.lock();
        try {
            bots.add(new Bot(userId, botCode, input));
            condition.signalAll();//当addBot()被调用，队列中添加新的任务时，线程将被唤醒
        }finally {
            lock.unlock();
        }
    }

    private void consum(Bot bot){
        System.out.println("------consum-----");
        Consumer consumer = new Consumer();
        consumer.startTimeout(2000, bot);//最多执行2秒
    }

    @Override
    public void run(){
        while (true){
            lock.lock();
            if(bots.isEmpty()){
                try {
                    condition.await();//阻塞线程，锁自动释放
                }catch (InterruptedException e){
                    e.printStackTrace();
                    lock.unlock();
                    break;
                }
            }else {
                Bot bot = bots.remove();//返回并删除队头
                lock.unlock();
//                System.out.println("--------");
                consum(bot);//编译并执行代码，比较耗时，可能会执行几秒钟
            }
        }
    }
}
