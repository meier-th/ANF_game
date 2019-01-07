package com.p3212.Configurations;

import com.p3212.EntityClasses.Fight;
import com.p3212.EntityClasses.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@Scope("singleton")
public class FightDataBean implements Serializable {
    private ConcurrentHashMap<Integer, Fight> fights;
    private ConcurrentSkipListSet<String> usersInFight;
    private ConcurrentHashMap<Integer, ArrayDeque<String>> queues;
    public static LinkedList<Pair<String, Date>> onlineUsers;

    @Autowired
    WebSocketsController wsController;
    
    @PostConstruct
    public void init() {
        fights = new ConcurrentHashMap<>();
        usersInFight = new ConcurrentSkipListSet<>();
        queues = new ConcurrentHashMap<>();
        onlineUsers = new LinkedList<>();
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException exc) {}
                    Date currDate = new Date();
                    Iterator<Pair<String, Date>> iterator = onlineUsers.iterator();
                    while (iterator.hasNext()) {
                        Pair<String, Date> element = iterator.next();
                        if(element.getValue().getTime() - currDate.getTime() > 300000) {
                            String msg = element.getKey() + ":offline";
                            wsController.sendOnline(msg);
                            iterator.remove();
                        }
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public ConcurrentHashMap<Integer, Fight> getFights() {
        return fights;
    }

    public ConcurrentSkipListSet<String> getUsersInFight() {
        return usersInFight;
    }

    public ConcurrentHashMap<Integer, ArrayDeque<String>> getQueues() {
        return queues;
    }
    
    public static synchronized void setOffline(String username) {
        Iterator<Pair<String, Date>> iterator = onlineUsers.iterator();
            while (iterator.hasNext()) {
                Pair<String, Date> element = iterator.next();
                if (element.getKey().equals(username))
                    iterator.remove();
            }
    }
   
    public static synchronized void setOnline(String username) {
        onlineUsers.add(new Pair<>(username, new Date()));
    }
    
}
