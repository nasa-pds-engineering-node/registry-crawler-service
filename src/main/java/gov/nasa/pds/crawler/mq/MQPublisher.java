package gov.nasa.pds.crawler.mq;

import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.ProductMessage;

/**
 * Message queue publisher interface 
 * @author karpenko
 */
public interface MQPublisher
{
    /**
     * Publish directory message
     * @param dirMsg directory message
     * @throws Exception an exception
     */
    public void publish(DirectoryMessage dirMsg) throws Exception;
        
    /**
     * Publish product message 
     * @param newMsg product message
     * @throws Exception an exception
     */
    public void publish(ProductMessage newMsg) throws Exception;
}
