package gov.nasa.pds.crawler.mq.amq;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.registry.common.mq.msg.DirectoryMessage;
import gov.nasa.pds.registry.common.mq.msg.JobMessage;
import gov.nasa.pds.registry.common.mq.msg.MQConstants;
import gov.nasa.pds.registry.common.util.ExceptionUtils;


/**
 * ActiveMQ consumer to process job messages
 * @author karpenko
 */
public class JobConsumerActiveMQ implements Runnable
{
    private Logger log;
    private Thread thread;
    
    private Gson gson;
    
    private Session session;    
    
    private Destination jobQueue;
    private Destination dirQueue;
    
    private MessageConsumer jobConsumer;
    private MessageProducer dirProducer;
    
    private volatile boolean stopRequested = false; 
    
    
    /**
     * Constructor
     * @param connection JMS connection
     * @throws Exception an exception
     */
    public JobConsumerActiveMQ(Connection connection) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        gson = new Gson();
        
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        jobQueue = session.createQueue(MQConstants.MQ_JOBS);
        dirQueue = session.createQueue(MQConstants.MQ_DIRS);
        
        jobConsumer = session.createConsumer(jobQueue);
        dirProducer = session.createProducer(dirQueue);
        dirProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }

    
    /**
     * Start consumer thread
     */
    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }
    
    
    /**
     * Stop consumer thread
     */
    public void stop()
    {
        stopRequested = true;
    }
    
    
    /**
     * Join consumer thread
     * @throws InterruptedException
     */
    public void join() throws InterruptedException
    {
        thread.join();
    }
    
    
    @Override
    public void run()
    {
        while(true)
        {
            Message message = null;
            
            try
            {
                message = jobConsumer.receive(3000);
            }
            catch(Exception ex)
            {
                log.error(ExceptionUtils.getMessage(ex));
            }
            
            if(message != null)
            {
                try
                {
                    processMessage(message);
                    message.acknowledge();
                }
                catch(Exception ex)
                {
                    log.error(ExceptionUtils.getMessage(ex));
                }
            }
            
            if(stopRequested) break;
        }
        
        close(session);
    }
    
    
    private void processMessage(Message mqJobMsg) throws JMSException
    {
        if(!(mqJobMsg instanceof TextMessage))
        {
            log.warn("Invalid message. ID = " + mqJobMsg.getJMSMessageID());
            return;
        }
     
        String jsonStr = ((TextMessage)mqJobMsg).getText();
        JobMessage jobMsg = null;
        
        try
        {
            jobMsg = gson.fromJson(jsonStr, JobMessage.class);
        }
        catch(Exception ex)
        {
            log.error("Could not parse message. ID = " + mqJobMsg.getJMSMessageID());
            return;
        }
        
        log.info("Processing job " + jobMsg.jobId);

        // Directories
        if(jobMsg.dirs != null)
        {
            for(String dir: jobMsg.dirs)
            {
                DirectoryMessage dirMsg = DirectoryMessageBuilder.createDirectoryMessage(jobMsg, dir);
                jsonStr = gson.toJson(dirMsg);
                
                TextMessage mqDirMsg = session.createTextMessage(jsonStr);            
                dirProducer.send(mqDirMsg);
            }
        }

        // Manifests
        if(jobMsg.manifests != null)
        {
            for(String manifest: jobMsg.manifests)
            {
                DirectoryMessage dirMsg = DirectoryMessageBuilder.createManifestMessage(jobMsg, manifest);
                jsonStr = gson.toJson(dirMsg);
                
                TextMessage mqDirMsg = session.createTextMessage(jsonStr);            
                dirProducer.send(mqDirMsg);
            }
        }
    }
    
    
    private void close(Session session)
    {
        try
        {
            session.close();
        }
        catch(Exception ex)
        {
        }
    }

}
