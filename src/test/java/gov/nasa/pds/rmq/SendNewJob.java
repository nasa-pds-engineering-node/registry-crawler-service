package gov.nasa.pds.rmq;

import java.util.Arrays;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import gov.nasa.pds.registry.common.mq.msg.JobMessage;


public class SendNewJob
{

    public static void main(String[] args) throws Exception
    {
        ConnectionFactory factory = new ConnectionFactory();
        Connection con = factory.newConnection();
        Channel channel = con.createChannel();

        // Create message
        JobMessage msg = new JobMessage();
        msg.jobId = "123";
        msg.dirs = Arrays.asList("/ws3/OREX/orex_spice");
        Gson gson = new Gson();
        String jsonStr = gson.toJson(msg);
        System.out.println(jsonStr);
        
        channel.basicPublish("", "q.jobs", null, jsonStr.getBytes());

        channel.close();
        con.close();
    }

}
