package gov.nasa.pds.crawler.mq.msg;

/**
 * Helper methods to create File Messages.
 * @author karpenko
 */
public class FileMessageBuilder
{
    /**
     * Create new File Message from a Directory Message and a File Batch
     * @param dirMsg directory message
     * @param batch file batch
     * @return new file message
     */
    public static FileMessage create(DirectoryMessage dirMsg, FileBatch batch)
    {
        FileMessage msg = new FileMessage();
        
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
