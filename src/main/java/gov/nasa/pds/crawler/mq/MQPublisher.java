package gov.nasa.pds.crawler.mq;

import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.ProductMessage;


public interface MQPublisher
{
    public void publish(DirectoryMessage dirMsg) throws Exception;
    public void publish(ProductMessage newMsg) throws Exception;
}
