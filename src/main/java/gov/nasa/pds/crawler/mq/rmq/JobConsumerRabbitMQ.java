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
import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.registry.common.mq.msg.DirectoryMessage;
import gov.nasa.pds.registry.common.mq.msg.JobMessage;


/**
 * RabbitMQ consumer to process job messages
 * @author karpenko
 */
public class JobConsumerRabbitMQ extends DefaultConsumer
{
    private Logger log;
    private Gson gson;
    
    
    /**
     * Constructor
     * @param channel
     */
    public JobConsumerRabbitMQ(Channel channel)
    {
        super(channel);
        log = LogManager.getLogger(this.getClass());
        gson = new Gson();
    }

    
    /**
     * Start consuming messages
     * @throws Exception
     */
    public void start() throws Exception
    {
        getChannel().basicConsume(Constants.MQ_JOBS, false, this);
    }
    
    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, 
            AMQP.BasicProperties properties, byte[] body) throws IOException
    {
        long deliveryTag = envelope.getDeliveryTag();
        
        JobMessage jobMsg = null;
        try
        {
            String jsonStr = new String(body);
            jobMsg = gson.fromJson(jsonStr, JobMessage.class);
        }
        catch(Exception ex)
        {
            log.error("Invalid message", ex);

            // ACK message (delete from the queue)
            getChannel().basicAck(deliveryTag, false);        
            return;
        }

        processMessage(jobMsg);
        
        // ACK message (delete from the queue)
        getChannel().basicAck(deliveryTag, false);        
    }
    
    
    private void processMessage(JobMessage jobMsg) throws IOException
    {
        log.info("Processing job " + jobMsg.jobId);
        
        // Directories
        if(jobMsg.dirs != null)
        {
            for(String dir: jobMsg.dirs)
            {
                DirectoryMessage dirMsg = DirectoryMessageBuilder.createDirectoryMessage(jobMsg, dir);
                String jsonStr = gson.toJson(dirMsg);
                
                getChannel().basicPublish("", Constants.MQ_DIRS, 
                        MessageProperties.MINIMAL_PERSISTENT_BASIC, jsonStr.getBytes());
            }
        }
        
        // Manifests
        if(jobMsg.manifests != null)
        {
            for(String manifest: jobMsg.manifests)
            {
                DirectoryMessage dirMsg = DirectoryMessageBuilder.createManifestMessage(jobMsg, manifest);
                String jsonStr = gson.toJson(dirMsg);
                
                getChannel().basicPublish("", Constants.MQ_DIRS, 
                        MessageProperties.MINIMAL_PERSISTENT_BASIC, jsonStr.getBytes());
            }
        }
    }
}
