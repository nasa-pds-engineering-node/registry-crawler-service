package gov.nasa.pds.crawler.cfg;

public class IPAddress
{
    private String host;
    private int port;

    public IPAddress(String host, int port)
    {
        this.host = host;
        this.port = port;
    }
        
    public String getHost()
    {
        return host;
    }
    
    public int getPort()
    {
        return port;
    }
    
    @Override
    public String toString()
    {
        return host + ":" + port;
    }
}
