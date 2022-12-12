package com.mycompany.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class App
{
    public static void main( String[] args ){

        Properties prop =  new Properties();
        try {
            prop.load(Files.newInputStream(Paths.get("application.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DatabaseConnection dbCon = new DatabaseConnection(prop.getProperty("url"), prop.getProperty("username"), prop.getProperty("password"));
        AddStock addStock = new AddStock(dbCon, prop.getProperty("newOrder"),prop.getProperty("processedOrder"));
        addStock.stockInsert();

        String host = prop.getProperty("host");

        //Enable and start consumers;
        Consumer consumer1 = new Consumer("Consumer 1",dbCon,host);
        Consumer consumer2 = new Consumer("Consumer 2",dbCon,host);
        Consumer consumer3 = new Consumer("Consumer 3",dbCon,host);
        Consumer consumer4 = new Consumer("Consumer 4",dbCon,host);
        Consumer consumer5 = new Consumer("Consumer 5",dbCon,host);
        consumer1.start();
        consumer2.start();
        consumer3.start();
        consumer4.start();
        consumer5.start();

    }
}
