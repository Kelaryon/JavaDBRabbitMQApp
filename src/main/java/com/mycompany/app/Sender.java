package com.mycompany.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class Sender
{
    public static void main( String[] args ) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare("ORDERS", true, false, false, null);

            String message;
            for (int i = 0; i < 10; i ++) {
                message = MessageGenerate(i+1,i+2,"Test");
                channel.basicPublish("", "ORDERS", MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    //First is ID followed by stock quantity and client name
    private static String MessageGenerate(int id_name, int required_stock, String clientName){
        return id_name + "," + required_stock + "," + clientName;
    }
}
