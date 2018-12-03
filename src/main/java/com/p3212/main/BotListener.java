package com.p3212.main;

import com.p3212.EntityClasses.Stats;
import com.p3212.EntityClasses.User;
import com.p3212.Repositories.StatsRepository;
import com.p3212.Repositories.UserRepository;
import com.rabbitmq.jms.admin.RMQConnectionFactory;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class BotListener {
    @Autowired
    StatsRepository stats;
    @Autowired
    UserRepository userRepository;
    private Session session;
    private MessageConsumer consumer;
    private MessageProducer producer;

    {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setHost("localhost");
        Connection con;
        try {
            con = factory.createConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue("bot"));
            con.start();
            producer = session.createProducer(session.createQueue("botResponse"));
            listen();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void listen() throws JMSException {
        consumer.setMessageListener((message) -> {
            try {
                System.out.println(((TextMessage) message).getText());
                respond(((TextMessage) message).getText());
            } catch (JMSException e) {
                System.out.println("R U SERIOUS?");
            }
        });
    }

    private void respond(String msg) throws JMSException {
        producer.send(session.createTextMessage(fetchData(msg)));
    }

    private String fetchData(String msg) {
        String[] param = msg.split(" ");
        switch (param[0]) {
            case "stats": {
                Optional<User> usr = userRepository.findById(param[1]);
                if (usr.isPresent()) return usr.get().getStats().toString();
                return "The user doesn't exist";
            }
            case "top": {
                Iterable<User> list = getTopUsers(100);
                StringBuilder result = new StringBuilder();
                for (User usr : list) {
                    result.append(usr.getLogin()).append(": rating = ").append(usr.getStats().getRating()).append("\n");
                }
                if (result.length() == 0) return "No top here)0";
                return result.toString();
            }
        }
        return "kek";
    }

    private ArrayList<User> getTopUsers(int number) {
        ArrayList<User> users = new ArrayList<>();
        Page<Stats> stts = stats.getTopStats(PageRequest.of(0, number));
        for (Stats st : stts) {
            if (st.getUser() != null)
                users.add(st.getUser());
        }
        return users;
    }

}
