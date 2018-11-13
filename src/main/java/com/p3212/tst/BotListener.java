package com.p3212.tst;

import EntityClasses.Stats;
import Repositories.StatsRepository;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.*;
import java.util.Optional;

@Service
public class BotListener {
    @Autowired
    StatsRepository stats;

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
                Optional<Stats> stat = stats.findById(param[1]);
                if (stat.isPresent()) return stat.get().toString();
                return "The user doesn't exist";
            }
            case "top": {
                Iterable<Stats> list = stats.findFirstByRating(10);
                StringBuilder result = new StringBuilder();
                for (Stats stat : list) {
                    result.append(stat.getLogin()).append(": rating = ").append(stat.getRating());
                }
                if (result.length() == 0) return "No top here)0";
                return result.toString();
            }
        }
        return "kek";
    }
}
