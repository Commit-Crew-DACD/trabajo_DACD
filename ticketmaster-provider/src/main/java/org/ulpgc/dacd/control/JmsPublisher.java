package org.ulpgc.dacd.control;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsPublisher {
    private final String brokerUrl;
    private final String topic;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public JmsPublisher(String brokerUrl, String topic) {
        this.brokerUrl = brokerUrl;
        this.topic = topic;
    }

    public void connect() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(topic);
        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }

    public void publish(String json) throws JMSException {
        TextMessage message = session.createTextMessage(json);
        producer.send(message);
    }

    public void close() throws JMSException {
        if (connection != null) connection.close();
    }
}