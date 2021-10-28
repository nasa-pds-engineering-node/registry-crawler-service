package gov.nasa.pds.crawler.meta;

/**
 * Basic PDS collection file information
 * @author karpenko
 */
public class PdsCollectionInfo extends PdsLabelInfo
{
    public String inventoryFileName;

    
    @Override
    public String toString()
    {
        return String.format("[%s, %s, %s]", productClass, lidvid, inventoryFileName);
    }
}
