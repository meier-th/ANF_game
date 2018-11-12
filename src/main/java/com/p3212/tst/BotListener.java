package com.p3212.tst;

import EntityClasses.Stats;
import Repositories.StatsRepository;
import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.util.List;
import java.util.Optional;

public class BotListener {
    @Autowired
    static StatsRepository stats;

    public static void setUp() {
        RMQConnectionFactory factory = new RMQConnectionFactory();
        factory.setHost("localhost");
        Connection con = null;
        final Session session;
        final MessageConsumer consumer;
        Message message;
        final MessageProducer producer;
        try {
            con = factory.createConnection();
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = session.createConsumer(session.createQueue("bot"));
            con.start();
            message = consumer.receive();
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

    private static String fetchData(String msg) {
        String[] param = msg.split(" ");
        switch (param[0]) {
            case "stats": {
                Optional<Stats> stat = stats.findById(param[1]);
                if (stat.isPresent()) return stat.get().toString();
                else return "The user doesn't exist";
            }
            case "top": {
                Iterable<Stats> list = stats.findTop100ByRating();
                StringBuilder result = new StringBuilder();
                for (Stats stat : list) {
                    result.append(stat.getLogin()).append(": rating = ").append(stat.getRating());
                }
                return result.toString();
            }
        }
        return "kek";
    }
}
