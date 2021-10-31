package gov.nasa.pds.crawler.mq.rmq;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

import gov.nasa.pds.crawler.Constants;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.crawler.mq.msg.JobMessage;


public class JobConsumerRabbitMQ extends DefaultConsumer
{
    private Logger log;
    private Gson gson;
    
    
    public JobConsumerRabbitMQ(Channel channel)
    {
        super(channel);
        log = LogManager.getLogger(this.getClass());
        
        gson = new Gson();
    }

    
    public void start() throws Exception
    {
        getChannel().basicConsume(Constants.MQ_JOBS, false, this);
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
        log.info("Processing job " + jobMsg.jobId);
        
        if(jobMsg.dirs == null) return;
        
        for(String dir: jobMsg.dirs)
        {
            DirectoryMessage dirMsg = DirectoryMessageBuilder.create(jobMsg, dir);
            String jsonStr = gson.toJson(dirMsg);
            
            getChannel().basicPublish("", Constants.MQ_DIRS, 
                    MessageProperties.MINIMAL_PERSISTENT_BASIC, jsonStr.getBytes());
        }
    }
}
