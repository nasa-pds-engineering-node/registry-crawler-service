package gov.nasa.pds.crawler.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.util.CloseUtils;


/**
 * Reads Crawler server configuration file.
 * @author karpenko
 */
public class ConfigurationReader
{
    // Message server / queue type
    private static final String PROP_MQ_TYPE = "mq.type";
    
    // RabbitMQ
    private static final String PROP_RMQ_HOST = "rmq.host";
    private static final String PROP_RMQ_USER = "rmq.user";
    private static final String PROP_RMQ_PASS = "rmq.password";
    private static final IPAddress DEFAULT_RMQ_HOST = new IPAddress("localhost", 5672);

    // ActiveMQ
    private static final String PROP_AMQ_URL = "amq.url";
    private static final String PROP_AMQ_USER = "amq.user";
    private static final String PROP_AMQ_PASS = "amq.password";
    private static final String DEFAULT_AMQ_URL = "tcp://localhost:61616";

    // Embedded web server
    private static final String PROP_WEB_PORT = "web.port";
    private static final int DEFAULT_WEB_PORT = 8001;
    
    private Logger log;

    
    /**
     * Constructor
     */
    public ConfigurationReader()
    {
        log = LogManager.getLogger(this.getClass());
    }

    /**
     * Read configuration file (Java properties / key-value file)
     * @param file a configuration file
     * @return parsed configuration
     * @throws Exception an exception
     */
    public Configuration read(File file) throws Exception
    {
        Configuration cfg = parseConfigFile(file);
        validate(cfg);
        
        return cfg;
    }

    
    private void validate(Configuration cfg) throws Exception
    {
        if(cfg.mqType == null)
        {
            String msg = String.format("Invalid configuration. Property '%s' is not set.", PROP_MQ_TYPE);
            throw new Exception(msg);
        }
        
        switch(cfg.mqType)
        {
        case ActiveMQ:
            validateAMQ(cfg.amqCfg);
            break;
        case RabbitMQ:
            validateRMQ(cfg.rmqCfg);
            break;
        }

        // Validate web port
        if(cfg.webPort == 0)
        {
            cfg.webPort = DEFAULT_WEB_PORT;
            String msg = String.format("Property '%s' is not set. Will use default value: %d", 
                    PROP_WEB_PORT, cfg.webPort);
            log.warn(msg);
        }
    }
    
    
    private void validateRMQ(RabbitMQCfg cfg) throws Exception
    {
        // Validate MQ address
        if(cfg.addresses.isEmpty())
        {
            cfg.addresses.add(DEFAULT_RMQ_HOST);
            String msg = String.format("'%s' property is not set. Will use default value: %s", 
                    PROP_RMQ_HOST, DEFAULT_RMQ_HOST.toString());
            log.warn(msg);
        }
    }

    
    private void validateAMQ(ActiveMQCfg cfg) throws Exception
    {
        if(cfg.url == null || cfg.url.isBlank())
        {
            cfg.url = DEFAULT_AMQ_URL;
            String msg = String.format("'%s' property is not set. Will use default value: %s", 
                    PROP_AMQ_URL, cfg.url);
            log.warn(msg);
        }
    }

    
    private Configuration parseConfigFile(File file) throws Exception
    {
        Configuration cfg = new Configuration();
        
        BufferedReader rd = null;
        try
        {
            rd = new BufferedReader(new FileReader(file));
            String line;
            while((line = rd.readLine()) != null)
            {
                line = line.trim();
                if(line.startsWith("#") || line.isEmpty()) continue;
                
                String[] tokens = line.split("=");
                if(tokens.length != 2) throw new Exception("Invalid property line: " + line);
                String key = tokens[0].trim();
                String value = tokens[1].trim();
                
                switch(key)
                {
                case PROP_WEB_PORT:
                    cfg.webPort = parseWebPort(value);
                    break;
                
                case PROP_MQ_TYPE:
                    cfg.mqType = parseMQType(value);
                    break;
                    
                // RabbitMQ
                case PROP_RMQ_HOST:
                    cfg.rmqCfg.addresses.add(parseMQAddresses(value));
                    break;
                case PROP_RMQ_USER:
                    cfg.rmqCfg.userName = value;
                    break;
                case PROP_RMQ_PASS:
                    cfg.rmqCfg.password = value;
                    break;

                // ActiveMQ
                case PROP_AMQ_URL:
                    cfg.amqCfg.url = value;
                    break;
                case PROP_AMQ_USER:
                    cfg.amqCfg.userName = value;
                    break;
                case PROP_AMQ_PASS:
                    cfg.amqCfg.password = value;
                    break;

                default:
                    throw new Exception("Invalid property '" + key + "'");
                }
            }
        }
        finally
        {
            CloseUtils.close(rd);
        }
        
        return cfg;
    }

    
    private MQType parseMQType(String str) throws Exception
    {
        if("ActiveMQ".equalsIgnoreCase(str)) return MQType.ActiveMQ;
        if("RabbitMQ".equalsIgnoreCase(str)) return MQType.RabbitMQ;
        
        String msg = String.format("Invalid '%s' property value: '%s'. Expected 'ActiveMQ' or 'RabbitMQ'.", 
                PROP_MQ_TYPE, str);
        throw new Exception(msg);
    }

    
    private int parseWebPort(String port) throws Exception
    {
        try
        {
            return Integer.parseInt(port);
        }
        catch(Exception ex)
        {
            String msg = String.format("Could not parse '%s' property: '%s'", PROP_WEB_PORT, port);
            throw new Exception(msg);
        }
    }
    
    
    private IPAddress parseMQAddresses(String str) throws Exception
    {
        String[] tokens = str.split(":");
        if(tokens.length != 2) 
        {
            String msg = String.format("Invalid '%s' property value: '%s'. Expected 'host:port'.", PROP_RMQ_HOST, str);
            throw new Exception(msg);
        }
        
        String host = tokens[0];
        int port = 0;
        
        try
        {
            port = Integer.parseInt(tokens[1]);
        }
        catch(Exception ex)
        {
            String msg = String.format("Invalid port in '%s' property: '%s'", PROP_RMQ_HOST, str);
            throw new Exception(msg);
        }
            
        return new IPAddress(host, port);
    }
}
