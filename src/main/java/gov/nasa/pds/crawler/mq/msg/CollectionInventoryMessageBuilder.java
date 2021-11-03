package gov.nasa.pds.crawler.mq.msg;

import java.io.File;

import gov.nasa.pds.crawler.meta.PdsCollectionInfo;

/**
 * Builds collection inventory messages
 * @author karpenko
 */
public class CollectionInventoryMessageBuilder
{
    
    /**
     * Build collection inventory message
     * @param dirMsg directory message being processed
     * @param collectionFile collection file
     * @param info basic PDS label info
     * @return new collection inventory message
     */
    public static CollectionInventoryMessage create(DirectoryMessage dirMsg, File collectionFile, PdsCollectionInfo info)
    {
        CollectionInventoryMessage msg = new CollectionInventoryMessage();
        msg.jobId = dirMsg.jobId;
        msg.collectionLidvid = info.lidvid;
        
        File invFile = new File(collectionFile.getParentFile(), info.inventoryFileName);
        msg.inventoryFile = invFile.getAbsolutePath();
        
        return msg;
    }
}
