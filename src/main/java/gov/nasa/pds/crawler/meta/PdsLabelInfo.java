package gov.nasa.pds.crawler.meta;

/**
 * Basic PDS label file information
 * @author karpenko
 */
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
