package org.ulpgc.dacd;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class JmsSubscriber {
    private final String brokerUrl;
    private final String[] topics;
    private final EventStoreWriter writer;

    public JmsSubscriber(String brokerUrl, String[] topics, EventStoreWriter writer) {
        this.brokerUrl = brokerUrl;
        this.topics = topics;
        this.writer = writer;
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.setClientID("event-store-builder");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        for (String topic : topics) {
            Topic destination = session.createTopic(topic);
            TopicSubscriber subscriber = session.createDurableSubscriber(
                    destination, "sub-" + topic);

            subscriber.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        String json = ((TextMessage) message).getText();
                        writer.write(topic, json);
                        System.out.println("Evento guardado [" + topic + "]: " + json);
                    }
                } catch (JMSException e) {
                    System.err.println("Error procesando mensaje: " + e.getMessage());
                }
            });
        }

        System.out.println("EventStoreBuilder suscrito a: " + String.join(", ", topics));
    }
}