package com.mycompany.app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AddStock {
    //implements runnable for multithreading;
    DatabaseConnection dbCon;
    File folder = new File("stocks_new");
    String newPath;
    String processedPath;

// To stop the thread;
//boolean isOn = true;
//private Thread t;
//private final String threadName;


//    @Override
//    public void run() {
//        while(isOn){
//        try {
//            stock();
//            Thread.sleep(1000*3600);
//          } catch (IOException | TimeoutException e) {
//            throw new RuntimeException(e);
//          }
//        }
//    }
//    public void start () {
//        if (t == null) {
//            t = new Thread (this, threadName);
//            t.start ();
//        }
//    }

    public AddStock(DatabaseConnection dbCon, String newPath,String processPath){
        this.dbCon = dbCon;
        this.newPath = newPath;
        this.processedPath = processPath;
        //stockInsert();
    }

    //Inserts the information form the xml in the database
    public void stockInsert() {

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                String filename = listOfFile.getName();
                if (filename.endsWith(".xml") || filename.endsWith(".XML")) {
                    parseXML(newPath + "/" + filename);
                    listOfFile.renameTo(new File(processedPath+"/"+filename));
                }
            }
        }
    }
    public void parseXML(String file) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(file));
            doc.getDocumentElement().normalize();

            // get <stock>
            NodeList list = doc.getElementsByTagName("stock");

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String id = element.getElementsByTagName("product_id").item(0).getTextContent();
                    String quantity = element.getElementsByTagName("quantity").item(0).getTextContent();
                    if (isNumeric(id) && isNumeric(quantity)) {
                        dbCon.InsertData(null,id,quantity);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
    //Check if data is numeric;
    private boolean isNumeric(String str){
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}

