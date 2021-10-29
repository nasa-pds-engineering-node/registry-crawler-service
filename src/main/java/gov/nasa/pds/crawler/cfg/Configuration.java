package gov.nasa.pds.crawler.cfg;


/**
 * Crawler server configuration parameters
 * @author karpenko
 */
public class Configuration
{
    /**
     * Message server type
     */
    public MQType mqType;
    
    /**
     * ActiveMQ configuration
     */
    public ActiveMQCfg amqCfg = new ActiveMQCfg();
    
    /**
     * RabbitMQ configuration
     */
    public RabbitMQCfg rmqCfg = new RabbitMQCfg();

    /**
     * Embedded web server port
     */
    public int webPort;
}
