package gov.nasa.pds.crawler.mq.amq;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import gov.nasa.pds.crawler.Constants;
import gov.nasa.pds.crawler.mq.MQPublisher;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.ProductMessage;
import gov.nasa.pds.crawler.proc.DirectoryProcessor;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;


/**
 * RabbitMQ consumer to process directory messages
 * @author karpenko
 */
public class DirectoryConsumerActiveMQ implements Runnable, MQPublisher
{
    private Logger log;
    private Thread thread;
    
    private Gson gson;
    private DirectoryProcessor proc;

    private Session session;    
    
    private Destination dirQueue;
    private Destination prodQueue;
    
    private MessageConsumer dirConsumer;
    private MessageProducer dirProducer;
    private MessageProducer prodProducer;
        
    private volatile boolean stopRequested = false; 

    
    /**
     * Constructor
     * @param connection JMS connection 
     * @throws Exception an exception
     */
    public DirectoryConsumerActiveMQ(Connection connection) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        gson = new Gson();
        proc = new DirectoryProcessor(this);
        
        session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        dirQueue = session.createQueue(Constants.MQ_DIRS);
        prodQueue = session.createQueue(Constants.MQ_PRODUCTS);
        
        dirConsumer = session.createConsumer(dirQueue);
        
        dirProducer = session.createProducer(dirQueue);
        dirProducer.setDeliveryMode(DeliveryMode.PERSISTENT);

        prodProducer = session.createProducer(prodQueue);
        prodProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
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

    
    /**
     * Handle delivery of a new message from the directory queue
     */
    @Override
    public void run()
    {
        while(true)
        {
            Message message = null;
            
            try
            {
                message = dirConsumer.receive(3000);
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


    private void processMessage(Message mqMsg) throws Exception
    {
        if(!(mqMsg instanceof TextMessage))
        {
            log.warn("Invalid message. ID = " + mqMsg.getJMSMessageID());
            return;
        }
     
        String jsonStr = ((TextMessage)mqMsg).getText();
        DirectoryMessage dirMsg = null;
        
        try
        {
            dirMsg = gson.fromJson(jsonStr, DirectoryMessage.class);
        }
        catch(Exception ex)
        {
            log.error("Could not parse message: " + jsonStr);
            return;
        }
        
        proc.processMessage(dirMsg);
    }


    @Override
    public void publish(DirectoryMessage msg) throws Exception
    {
        String jsonStr = gson.toJson(msg);
        TextMessage newMsg = session.createTextMessage(jsonStr);
        dirProducer.send(newMsg);
    }


    @Override
    public void publish(ProductMessage msg) throws Exception
    {
        String jsonStr = gson.toJson(msg);
        TextMessage newMsg = session.createTextMessage(jsonStr);
        prodProducer.send(newMsg);
    }
    
}
