package gov.nasa.pds;

import java.util.ArrayList;

import com.google.gson.Gson;

import gov.nasa.pds.crawler.mq.DataMessage;

public class TestMessage
{

    public static void main(String[] args)
    {
        DataMessage msg = new DataMessage();
        msg.node = "PDS_ENG";
        msg.rid = "cd9869ee-bb36-4122-87b2-d1b21fa4edbd";
        msg.fref = new ArrayList<>();
        msg.fref.add("/C:/tmp/|http://test.local/");
        msg.file = "C:\\tmp\\d5\\bundle_kaguya_derived.xml";
        
        Gson gson = new Gson();
        String jsonStr = gson.toJson(msg);        
        System.out.println(jsonStr);
                
        DataMessage msg2 = gson.fromJson(jsonStr, DataMessage.class);
        System.out.println(msg2.node + ", " + msg2.rid + ", " + msg2.fref.get(0) + ", " + msg2.file);
        
    }

}
