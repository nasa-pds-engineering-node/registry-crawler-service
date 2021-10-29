package gov.nasa.pds.crawler;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import gov.nasa.pds.crawler.mq.JobConsumer;
import gov.nasa.pds.crawler.mq.RabbitMQClient;
import gov.nasa.pds.crawler.cfg.Configuration;
import gov.nasa.pds.crawler.cfg.ConfigurationReader;
import gov.nasa.pds.crawler.http.StatusServlet;
import gov.nasa.pds.crawler.mq.DirectoryConsumer;
import gov.nasa.pds.crawler.util.ExceptionUtils;


/**
 * Harvest server
 * @author karpenko
 */
public class CrawlerServer
{
    private Logger log;
    private Configuration cfg;
    private RabbitMQClient mqClient;

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
        
        mqClient = new RabbitMQClient(cfg.rmqCfg);
    }
    
    
    /**
     * Run the server
     */
    public void run()
    {
        mqClient.connect();
        
        try
        {
            startJobConsumer();
            startDirectoryConsumer();
            startWebServer(cfg.webPort);
        }
        catch(Exception ex)
        {
            log.error(ExceptionUtils.getMessage(ex));
            mqClient.close();
        }
    }
    
    
    /**
     * Start job message consumer
     * @throws Exception an exception
     */
    private void startJobConsumer() throws Exception
    {
        JobConsumer consumer = mqClient.createJobConsumer();
        consumer.start();

        log.info("Started job consumer");
    }
    
    
    /**
     * Start directory message consumer 
     * @throws Exception an exception
     */
    private void startDirectoryConsumer() throws Exception
    {
        DirectoryConsumer consumer = mqClient.createDirectoryConsumer();
        consumer.start();

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

}
