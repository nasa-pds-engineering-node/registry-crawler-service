package gov.nasa.pds.crawler.mq.msg;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.pds.crawler.util.xml.PdsLabelInfo;

/**
 * Collection of files for Harvest server to process.
 * @author karpenko
 */
public class FileBatch
{
    /**
     * List of file paths
     */
    public List<String> paths;
    
    /**
     * List of corresponding LIDVIDs
     */
    public List<String> lidvids;
    
    /**
     * Constructor
     * @param batchSize number of files in this batch
     */
    public FileBatch(int batchSize)
    {
        paths = new ArrayList<>(batchSize);
        lidvids = new ArrayList<>(batchSize);
    }
    
    /**
     * Adds a file path and corresponding LIDVID
     * @param path file path
     * @param info PDS label info, including LIDVID
     */
    public void add(String path, PdsLabelInfo info)
    {
        if(info == null) return;
        
        paths.add(path);
        lidvids.add(info.lidvid);
    }
    
    /**
     * Get batch size
     * @return number of files in this batch
     */
    public int size()
    {
        return paths.size();
    }
    
    /**
     * Clear data.
     */
    public void clear()
    {
        paths.clear();
        lidvids.clear();
    }
}
