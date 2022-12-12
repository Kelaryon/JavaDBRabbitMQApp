package com.mycompany.app;

import com.rabbitmq.tools.json.JSONUtil;
import org.json.*;

public class JSONMessage {
   private JSONObject obj;

   public JSONMessage(String name){
       obj.put("Client_name", name);
       obj.put("num", 10);
       obj.put("balance", 10.1);
       obj.put("is_vip", true);

       System.out.print(obj);
   }
}
