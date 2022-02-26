package gov.nasa.pds.crawler.mq;

import gov.nasa.pds.registry.common.mq.msg.CollectionInventoryMessage;
import gov.nasa.pds.registry.common.mq.msg.DirectoryMessage;
import gov.nasa.pds.registry.common.mq.msg.ProductMessage;

/**
 * Message queue publisher interface 
 * @author karpenko
 */
public interface MQPublisher
{
    /**
     * Publish directory message
     * @param msg directory message
     * @throws Exception an exception
     */
    public void publish(DirectoryMessage msg) throws Exception;

    /**
     * Publish product message 
     * @param msg product message
     * @throws Exception an exception
     */
    public void publish(ProductMessage msg) throws Exception;
    
    /**
     * Publish collection inventory message
     * @param msg collection inventory message
     * @throws Exception an exception
     */
    public void publish(CollectionInventoryMessage msg) throws Exception;
}
