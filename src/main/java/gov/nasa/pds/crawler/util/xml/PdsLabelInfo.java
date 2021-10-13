package gov.nasa.pds.crawler.util.xml;

public class PdsLabelInfo
{
    public String productClass;
    public String lidvid;
    
    @Override
    public String toString()
    {
        return String.format("[%s, %s]", productClass, lidvid);
    }
}
