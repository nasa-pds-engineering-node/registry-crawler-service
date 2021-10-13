package gov.nasa.pds.crawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import gov.nasa.pds.crawler.mq.JobConsumer;
import gov.nasa.pds.crawler.mq.DirectoryConsumer;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;


/**
 * Harvest server
 * @author karpenko
 */
public class CrawlerServer
{
    private Logger log;
    
    private ConnectionFactory rmqConFactory;
    private Connection rmqConnection;
    private List<Address> rmqAddr;
    

    public CrawlerServer(String cfgFilePath)
    {
        log = LogManager.getLogger(this.getClass());
        
        rmqConFactory = new ConnectionFactory();
        rmqConFactory.setAutomaticRecoveryEnabled(true);
        
        rmqAddr = new ArrayList<>();
        rmqAddr.add(new Address("localhost", 5672));
    }
    
    
    public void run()
    {
        connect();
        
        try
        {
            startJobConsumer();
            startSubDirConsumer();
        }
        catch(Exception ex)
        {
            log.error(ExceptionUtils.getMessage(ex));
            CloseUtils.close(rmqConnection);
        }
    }
    
    
    private void startJobConsumer() throws Exception
    {
        Channel channel = rmqConnection.createChannel();
        channel.basicQos(1);
        
        JobConsumer consumer = new JobConsumer(channel);
        channel.basicConsume("q.jobs", false, consumer);

        log.info("Started job consumer");
    }
    
    
    private void startSubDirConsumer() throws Exception
    {
        Channel channel = rmqConnection.createChannel();
        channel.basicQos(1);
        
        DirectoryConsumer consumer = new DirectoryConsumer(channel);
        channel.basicConsume("q.dirs", false, consumer);

        log.info("Started sub-directory consumer");
    }
    

    private void connect()
    {
        while(true)
        {
            try
            {
                rmqConnection = rmqConFactory.newConnection(rmqAddr);
                break;
            }
            catch(Exception ex)
            {
                log.warn("Could not connect to RabbitMQ. " + ex + ". Will retry in 10 sec.");
                sleepSec(10);
            }
        }

        log.info("Connected to RabbitMQ");
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
