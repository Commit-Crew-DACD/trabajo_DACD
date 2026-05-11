package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        String brokerUrl = "failover:(tcp://localhost:61616)?maxReconnectAttempts=10&initialReconnectDelay=1000&maxReconnectDelay=30000";
        String[] topics = {"Flight", "Prediction"};

        EventStoreWriter writer = new EventStoreWriter();
        JmsSubscriber subscriber = new JmsSubscriber(brokerUrl, topics, writer);

        try {
            subscriber.start();
            System.out.println("EventStoreBuilder iniciado. Esperando mensajes...");
        } catch (Exception e) {
            System.err.println("Error iniciando EventStoreBuilder: " + e.getMessage());
        }
    }
}