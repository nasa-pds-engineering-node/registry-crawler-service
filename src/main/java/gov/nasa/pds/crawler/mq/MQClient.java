package gov.nasa.pds.crawler.mq;

public interface MQClient
{
    public void run() throws Exception;
    
    public String getType();
    public String getConnectionInfo();
    public boolean isConnected();
}
