package com.p3212.Configurations;

import com.p3212.EntityClasses.Fight;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
@Scope("singleton")
public class FightDataBean implements Serializable {
    private ConcurrentHashMap<Integer, Fight> fights;
    private ConcurrentSkipListSet<String> usersInFight;
    private ConcurrentHashMap<Integer, ArrayDeque<String>> queues;

    @PostConstruct
    public void init() {
        fights = new ConcurrentHashMap<>();
        usersInFight = new ConcurrentSkipListSet<>();
        queues = new ConcurrentHashMap<>();
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
}
