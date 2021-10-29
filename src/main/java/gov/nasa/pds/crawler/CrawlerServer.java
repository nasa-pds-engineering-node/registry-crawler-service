package gov.nasa.pds.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import gov.nasa.pds.crawler.mq.JobConsumer;
import gov.nasa.pds.crawler.cfg.Configuration;
import gov.nasa.pds.crawler.cfg.ConfigurationReader;
import gov.nasa.pds.crawler.cfg.IPAddress;
import gov.nasa.pds.crawler.http.StatusServlet;
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
    private Configuration cfg;
    
    private ConnectionFactory rmqConFactory;
    private Connection rmqConnection;
    

    /**
     * Constructor
     * @param cfgFilePath configuration file path
     * @throws Exception an exception
     */
    public CrawlerServer(String cfgFilePath) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        
        // Read configuration file
        File file = new File(cfgFilePath);
        log.info("Reading configuration from " + file.getAbsolutePath());        
        ConfigurationReader cfgReader = new ConfigurationReader();
        cfg = cfgReader.read(file);
        
        // Init RabbitMQ connection factory
        rmqConFactory = new ConnectionFactory();
        rmqConFactory.setAutomaticRecoveryEnabled(true);
        
        if(cfg.rmqCfg.userName != null)
        {
            rmqConFactory.setUsername(cfg.rmqCfg.userName);
            rmqConFactory.setPassword(cfg.rmqCfg.password);
        }
    }
    
    
    /**
     * Run the server
     */
    public void run()
    {
        connectToRabbitMQ();
        
        try
        {
            startJobConsumer();
            startDirectoryConsumer();
            startWebServer(cfg.webPort);
        }
        catch(Exception ex)
        {
            log.error(ExceptionUtils.getMessage(ex));
            CloseUtils.close(rmqConnection);
        }
    }
    
    
    /**
     * Start job message consumer
     * @throws Exception an exception
     */
    private void startJobConsumer() throws Exception
    {
        Channel channel = rmqConnection.createChannel();
        channel.basicQos(1);
        
        JobConsumer consumer = new JobConsumer(channel);
        channel.basicConsume(Constants.MQ_JOBS, false, consumer);

        log.info("Started job consumer");
    }
    
    
    /**
     * Start directory message consumer 
     * @throws Exception an exception
     */
    private void startDirectoryConsumer() throws Exception
    {
        Channel channel = rmqConnection.createChannel();
        channel.basicQos(1);
        
        DirectoryConsumer consumer = new DirectoryConsumer(channel);
        channel.basicConsume(Constants.MQ_DIRS, false, consumer);

        log.info("Started directory consumer");
    }
    
    
    /**
     * Start embedded web server
     * @param port a port to listen for incoming connections
     */
    private void startWebServer(int port) throws Exception
    {
        Server server = new Server();
        
        // HTTP connector
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("0.0.0.0");
        connector.setPort(port);
        server.addConnector(connector);

        // Servlet handler
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(StatusServlet.class, "/*");
        server.setHandler(handler);
        
        // Start web server
        server.start();
        
        log.info("Started web server on port " + port);
    }

    
    /**
     * Connect to RabbitMQ server. Wait until RabbitMQ is up. 
     */
    private void connectToRabbitMQ()
    {
        // Get the list of RabbitMQ addresses as a string for logging
        StringBuilder bld = new StringBuilder();
        for(int i = 0; i < cfg.rmqCfg.addresses.size(); i++)
        {
            if(i != 0) bld.append(", ");
            IPAddress ipa = cfg.rmqCfg.addresses.get(i);
            bld.append(ipa.getHost() + ":" + ipa.getPort());
        }
        
        log.info("Connecting to RabbitMQ at " + bld.toString());
        
        // Convert configuration model classes to RabbitMQ model classes
        List<Address> rmqAddr = new ArrayList<>();
        for(IPAddress ipa: cfg.rmqCfg.addresses)
        {
            rmqAddr.add(new Address(ipa.getHost(), ipa.getPort()));
        }
        
        // Wait for RabbitMQ
        while(true)
        {
            try
            {
                rmqConnection = rmqConFactory.newConnection(rmqAddr);
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
