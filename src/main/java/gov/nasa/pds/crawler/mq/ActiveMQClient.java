package gov.nasa.pds.crawler.mq;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.cfg.ActiveMQCfg;

/**
 * ActiveMQ connection
 * @author karpenko
 */
public class ActiveMQClient implements MQClient
{
    private Logger log;
    private ActiveMQCfg cfg;
    
    private ActiveMQConnectionFactory factory;
    private ActiveMQListener listener;
    private Connection con;

    
    /**
     * Constructor
     * @param cfg configuration
     */
    public ActiveMQClient(ActiveMQCfg cfg)
    {
        // Get logger
        log = LogManager.getLogger(this.getClass());

        // Validate and store configuration
        if(cfg == null || cfg.url == null || cfg.url.isBlank()) 
        {
            throw new IllegalArgumentException("ActiveMQ URL is not set.");
        }
        
        this.cfg = cfg;
        
        // Enable failover
        if(!cfg.url.startsWith("failover:"))
        {
            cfg.url = "failover:" + cfg.url;
        }
        
        // Create connection factory
        factory = new ActiveMQConnectionFactory(cfg.url);
        if(cfg.userName != null)
        {
            factory.setUserName(cfg.userName);
            factory.setPassword(cfg.password);
        }
        
        // Setup listener
        listener = new ActiveMQListener();
        factory.setTransportListener(listener);
        factory.setExceptionListener(listener);
    }


    @Override
    public String getType()
    {
        return "ActiveMQ";
    }

    
    @Override
    public String getConnectionInfo()
    {
        return cfg.url;
    }

    
    @Override
    public boolean isConnected()
    {
        return listener.isConnected();
    }
    
    
    @Override
    public void run() throws Exception
    {
        connect();
        
        //startJobConsumer();
        //startDirectoryConsumer();
        
        Thread.currentThread().join();
    }

    
    public void connect() throws Exception
    {        
        if(con != null) 
        {
            log.warn("Already connected.");
            return;
        }
        
        log.info("Connecting to ActiveMQ at " + cfg.url);
        con = factory.createConnection();
        con.setExceptionListener(listener);
        con.start();
    }
}
