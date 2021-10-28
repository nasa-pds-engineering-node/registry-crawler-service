package gov.nasa.pds.crawler;

/**
 * Some constants used by different classes.
 * 
 * @author karpenko
 */
public interface Constants
{
    public static final String MQ_JOBS = "harvest.jobs";
    public static final String MQ_DIRS = "harvest.dirs";
    public static final String MQ_PRODUCTS = "harvest.products";
    public static final String MQ_COLLECTIONS = "harvest.collections";
    
    public static final int FILES_MESSAGE_MAX_ITEMS = 50;
}
