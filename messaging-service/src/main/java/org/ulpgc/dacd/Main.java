package org.ulpgc.dacd;

public class Main {
    public static void main(String[] args) {
        String brokerUrl = "tcp://localhost:61616";
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