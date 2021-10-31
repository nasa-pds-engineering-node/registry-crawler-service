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

import gov.nasa.pds.crawler.Constants;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.crawler.mq.msg.JobMessage;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;


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
    
    
    public JobConsumerActiveMQ(Connection connection) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        gson = new Gson();
        
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        jobQueue = session.createQueue(Constants.MQ_JOBS);
        dirQueue = session.createQueue(Constants.MQ_DIRS);
        
        jobConsumer = session.createConsumer(jobQueue);
        dirProducer = session.createProducer(dirQueue);
        dirProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
    }

    
    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }
    
    
    public void stop()
    {
        stopRequested = true;
    }
    
    
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
        
        CloseUtils.close(session);
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
        
        if(jobMsg.dirs == null) return;
        
        for(String dir: jobMsg.dirs)
        {
            DirectoryMessage dirMsg = DirectoryMessageBuilder.create(jobMsg, dir);
            jsonStr = gson.toJson(dirMsg);
            
            TextMessage mqDirMsg = session.createTextMessage(jsonStr);            
            dirProducer.send(mqDirMsg);
        }
    }
}
