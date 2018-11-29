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

    {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setHost("localhost");
        Connection con;
        final Session session;
        final MessageConsumer consumer;
        final MessageProducer producer;
        try {
            con = factory.createConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue("bot"));
            con.start();
            producer = session.createProducer(session.createQueue("botResponse"));
            Thread listener = new Thread() {
                @Override
                public void run() {
                    listen();
                }

                void listen() {
                    try {
                        Message message = consumer.receive();
                        System.out.println(((TextMessage) message).getText());
                        respond(((TextMessage) message).getText());
                    } catch (JMSException ex) {
                        System.out.println(ex.getMessage());
                    }
                    listen();
                }

                void respond(String msg) throws JMSException {
                    producer.send(session.createTextMessage(fetchData(msg))); //TODO it should send the response
                }

            };
            listener.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
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
                Iterable<User> list = getTopUsers(100); //TODO here should be request for stats
                StringBuilder result = new StringBuilder();
                for (User usr : list) {
                    result.append(usr.getLogin()).append(": rating = ").append(usr.getStats().getRating());
                }
                if (result.length() == 0) return "No top here)0";
                return result.toString();
            }
        }
        return "kek";
    }
    
    ArrayList<User> getTopUsers(int number) {
        ArrayList<User> users = new ArrayList<>();
        Page<Stats> stts = stats.getTopStats(new PageRequest(0, number));
        for (Stats st : stts){
            users.add(st.getUser());
        }
        return users;
    }
    
}
