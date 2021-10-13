package gov.nasa.pds.crawler.mq;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.JobMessage;


public class JobConsumer extends DefaultConsumer
{
    private Logger log;
    private Gson gson;
    
    
    public JobConsumer(Channel channel)
    {
        super(channel);
        log = LogManager.getLogger(this.getClass());
        
        gson = new Gson();
    }

    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, 
            AMQP.BasicProperties properties, byte[] body) throws IOException
    {
        long deliveryTag = envelope.getDeliveryTag();
        
        String jsonStr = new String(body);
        JobMessage jobMsg = gson.fromJson(jsonStr, JobMessage.class);
        
        processMessage(jobMsg);
        
        // ACK message (delete from the queue)
        getChannel().basicAck(deliveryTag, false);        
    }
    
    
    private void processMessage(JobMessage jobMsg) throws IOException
    {
        log.info("Processing job " + jobMsg.id);
        
        if(jobMsg.dirs == null) return;
        
        for(String dir: jobMsg.dirs)
        {
            DirectoryMessage dirMsg = new DirectoryMessage(jobMsg.id, dir);
            String jsonStr = gson.toJson(dirMsg);
            
            getChannel().basicPublish("", "q.dirs", null, jsonStr.getBytes());
        }
    }
}
