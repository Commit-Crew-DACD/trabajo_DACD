package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        String brokerUrl = "tcp://localhost:61616";
        String[] topics = {"flights", "events"};

        EventStoreWriter writer = new EventStoreWriter();
        JmsSubscriber subscriber = new JmsSubscriber(brokerUrl, topics, writer);

        try {
            subscriber.start();
            System.out.println("EventStoreBuilder activo.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}