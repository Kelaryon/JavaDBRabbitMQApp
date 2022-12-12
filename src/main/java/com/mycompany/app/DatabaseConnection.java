package com.mycompany.app;

import java.sql.*;


/**
 * Hello world!
 *
 */
public class DatabaseConnection
{
    public Statement st;
    public  Connection cn;

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnection(String url, String username, String password) {

        this.url = url;
        this.username = username;
        this.password = password;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            cn = DriverManager.getConnection(url,username,password);

            st = cn.createStatement();
            cn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            System.out.println("Connection secured");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Insert data in the db for the XML parser;
    public void InsertData(String name, String id, String stock){
        try {
            st.executeUpdate("INSERT INTO product (Name, Id, Stock) VALUES("+name+", "+id+", "+stock+") ON DUPLICATE KEY UPDATE Stock= Stock + "+stock);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Generate connections for each thread;
    public Connection returnConnection(){
        try {
            return DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
