package org.ulpgc.dacd.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.storage.DatamartRepository;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

public class JmsEventConsumer {
    private final String brokerUrl;
    private final String[] topics;
    private final EventMessageParser parser;
    private final DatamartRepository repository;
    private final RecommendationService recommendationService;

    private Connection connection;
    private Session session;

    public JmsEventConsumer(String brokerUrl, String[] topics,
                            EventMessageParser parser,
                            DatamartRepository repository,
                            RecommendationService recommendationService) {
        this.brokerUrl = brokerUrl;
        this.topics = topics;
        this.parser = parser;
        this.repository = repository;
        this.recommendationService = recommendationService;
    }

    public void start() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        connection = factory.createConnection();
        connection.setClientID("business-unit");
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        for (String topicName : topics) {
            Topic topic = session.createTopic(topicName);
            TopicSubscriber subscriber = session.createDurableSubscriber(topic, "business-unit-" + topicName);

            subscriber.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String json = textMessage.getText();
                        System.out.println("Real-time message received from topic " + topicName);
                        processMessage(topicName, json);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing real-time message from " + topicName + ": " + e.getMessage());
                }
            });
        }

        System.out.println("Business Unit subscribed to: " + String.join(", ", topics));
    }

    private void processMessage(String topicName, String json) {
        if (parser.isEventMessage(json)) {
            int before = repository.countEvents();
            repository.saveEvent(parser.parseEvent(json));
            int after = repository.countEvents();
            if (after > before) {
                recommendationService.rebuildRecommendations();
                System.out.println("Real-time event processed from " + topicName + ". Events: " + after);
            }
            return;
        }

        if (parser.isFlightMessage(json)) {
            repository.saveFlight(parser.parseFlight(json));
            System.out.println("Real-time flight processed from " + topicName + ". Flights: " + repository.countFlights());
            return;
        }

        System.out.println("Ignored real-time message from " + topicName + ": unknown source");
    }
}
