package gov.nasa.pds.crawler.mq;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import gov.nasa.pds.crawler.cfg.IPAddress;
import gov.nasa.pds.crawler.cfg.RabbitMQCfg;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;

public class RabbitMQClient
{
    private Logger log;
    private RabbitMQCfg cfg;
    
    private ConnectionFactory factory;
    private Connection connection;
    

    public RabbitMQClient(RabbitMQCfg cfg)
    {
        log = LogManager.getLogger(this.getClass());
        
        this.cfg = cfg;
        
        factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        
        if(cfg.userName != null)
        {
            factory.setUsername(cfg.userName);
            factory.setPassword(cfg.password);
        }
    }
    
    
    /**
     * Connect to RabbitMQ server. Wait until RabbitMQ is up. 
     */
    public void connect()
    {
        if(connection != null) return;
        
        // Get the list of RabbitMQ addresses as a string for logging
        StringBuilder bld = new StringBuilder();
        for(int i = 0; i < cfg.addresses.size(); i++)
        {
            if(i != 0) bld.append(", ");
            IPAddress ipa = cfg.addresses.get(i);
            bld.append(ipa.getHost() + ":" + ipa.getPort());
        }
        
        log.info("Connecting to RabbitMQ at " + bld.toString());
        
        // Convert configuration model classes to RabbitMQ model classes
        List<Address> rmqAddr = new ArrayList<>();
        for(IPAddress ipa: cfg.addresses)
        {
            rmqAddr.add(new Address(ipa.getHost(), ipa.getPort()));
        }
        
        // Wait for RabbitMQ
        while(true)
        {
            try
            {
                connection = factory.newConnection(rmqAddr);
                break;
            }
            catch(Exception ex)
            {
                String msg = ExceptionUtils.getMessage(ex);
                log.warn("Could not connect to RabbitMQ. " + msg + ". Will retry in 10 sec.");
                sleepSec(10);
            }
        }

        log.info("Connected to RabbitMQ");
    }


    public void close()
    {
        CloseUtils.close(connection);    
    }
    
    
    public JobConsumer createJobConsumer() throws Exception
    {
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        
        JobConsumer consumer = new JobConsumer(channel);
        return consumer;
    }
    
    
    public DirectoryConsumer createDirectoryConsumer() throws Exception
    {
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        
        DirectoryConsumer consumer = new DirectoryConsumer(channel);
        return consumer;
    }
    
    
    private static void sleepSec(int sec)
    {
        try
        {
            Thread.sleep(sec * 1000);
        }
        catch(InterruptedException ex)
        {
            // Ignore
        }
    }

}
