package com.mycompany.app;

import com.rabbitmq.client.*;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeoutException;

public class Consumer implements Runnable
{
    private Thread t;
    private final String threadName;
    Channel channelSend;
    private final java.sql.Connection connection;
    String host;

    DatabaseConnection dbCon;

    public Consumer(String name, DatabaseConnection dbCon,String host){
        this.host = host;
        this.dbCon = dbCon;
        threadName = name;
        connection = dbCon.returnConnection();
        try {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_COMMITTED);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Multithreading methods
    @Override
    public void run() {
        try {
            consumerRun();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

    //Main method used to create the channel for sending and receiving rabbitMQ messages
    public void consumerRun() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        final Channel channelReceive;
        Connection connection = factory.newConnection();

            channelReceive = connection.createChannel();
            channelSend = connection.createChannel();


        channelReceive.queueDeclare("ORDERS", true, false, false, null);

        channelSend.queueDeclare("ORDERS_RESPONSE", true, false, false, null);

        channelReceive.basicQos(1);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            System.out.println("Consumer: "+threadName+ " Received '" + message + "'");
            try {
                doWork(message);
            } finally {
                channelReceive.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channelReceive.basicConsume("ORDERS", false, deliverCallback, consumerTag -> { });
    }

    //Method to run the message task
    private void doWork(String task) {
        if (task == null){
            return;
        }
        String[] arr = task.split(",");
        if (arr.length < 2){
            return;
        }
        String st = processData(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]),arr[2]);
        try {
            String temp = "Order Id: " + arr[0]+"\n" + "Order Status: " + st+"\n" +"Client: " + arr[2]+"\n" + "Stock Order: " + arr[1];
            channelSend.basicPublish("","ORDERS_RESPONSE", MessageProperties.PERSISTENT_TEXT_PLAIN,temp.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //DB data processing
    public String processData(int id, int stock,String clientName){
        try {
            String st;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT stock FROM product Where Id = "+id +" FOR UPDATE");
            if(resultSet.next()) {
                int temp = resultSet.getInt(1);
                if (temp >= stock) {
                    statement.executeUpdate("UPDATE product SET Stock = " + (temp - stock) + " WHERE Id = " + id);
                    statement.executeUpdate("INSERT INTO Orders(ProductID, ProductName, ClientName, Status, StockOrder) VALUES ("+id+",'Filler','"+clientName+"','RESERVED','"+stock+"')");
                    st = "RESERVED";
                } else {
                    statement.executeUpdate("INSERT INTO Orders(ProductID, ProductName, ClientName, Status, StockOrder) VALUES ("+id+",'Filler','"+clientName+"','INSUFFICIENT_STOCK','"+stock+"')");
                    st = "INSUFFICIENT_STOCK";
                }
            }else {
                statement.executeUpdate("INSERT INTO Orders(ProductID, ProductName, ClientName, Status) VALUES ("+id+",'Filler','"+clientName+"','INSUFFICIENT_STOCK','"+stock+"')");
                st = "INSUFFICIENT_STOCK";
            }
            connection.commit();
            resultSet.close();
            statement.close();
            return  st;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
