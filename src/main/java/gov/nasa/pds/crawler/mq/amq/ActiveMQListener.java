package gov.nasa.pds.crawler.mq.amq;

import java.io.IOException;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.apache.activemq.transport.TransportListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.util.ExceptionUtils;


/**
 * Listens for exceptions and connection status
 * @author karpenko
 */
public class ActiveMQListener implements ExceptionListener, TransportListener
{
    private Logger log;
    
    private volatile boolean connected = false;
    

    /**
     * Constructor
     */
    public ActiveMQListener()
    {
        log = LogManager.getLogger(this.getClass());
    }
    
    
    /**
     * Returns connection status
     * @return connection status
     */
    public boolean isConnected()
    {
        return connected;
    }
    
    
    /**
     * Non-transport exception (ExceptionListener)
     */
    @Override
    public void onException(JMSException ex)
    {
        log.error(ExceptionUtils.getMessage(ex));
    }

    
    @Override
    public void onCommand(Object cmd)
    {
    }


    /**
     * Transport exception (TransportListener)
     */
    @Override
    public void onException(IOException ex)
    {
        log.error(ExceptionUtils.getMessage(ex));
    }

    
    @Override
    public void transportInterupted()
    {
        connected = false;
        log.warn("Disconnected from ActiveMQ.");
    }

    
    @Override
    public void transportResumed()
    {
        connected = true;
        log.info("Connected to ActiveMQ.");
    }
}
