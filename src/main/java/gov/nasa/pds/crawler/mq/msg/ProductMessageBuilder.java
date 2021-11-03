package gov.nasa.pds.crawler.mq.msg;

/**
 * Helper methods to create File Messages.
 * @author karpenko
 */
public class ProductMessageBuilder
{
    /**
     * Create new Product Message from a Directory Message and a File Batch
     * @param dirMsg directory message
     * @param batch file batch
     * @return new file message
     */
    public static ProductMessage create(DirectoryMessage dirMsg, FileBatch batch)
    {
        ProductMessage msg = new ProductMessage();
        
        msg.jobId = dirMsg.jobId;
        msg.nodeName = dirMsg.nodeName;
        msg.dateFields = dirMsg.dateFields;
        msg.fileRefs = dirMsg.fileRefs;
        msg.overwrite = dirMsg.overwrite;

        msg.files = batch.paths;
        msg.lidvids = batch.lidvids;
        
        return msg;
    }
}
