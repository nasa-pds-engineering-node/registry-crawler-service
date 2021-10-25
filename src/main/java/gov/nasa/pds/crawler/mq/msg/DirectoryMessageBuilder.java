package gov.nasa.pds.crawler.mq.msg;

/**
 * Helper methods to create Directory Messages.
 * @author karpenko
 */
public class DirectoryMessageBuilder
{
    /**
     * Create new Directory Message from a Job Message and a directory
     * @param jobMsg job message
     * @param dir directory
     * @return new DirectoryMessage
     */
    public static DirectoryMessage create(JobMessage jobMsg, String dir)
    {
        DirectoryMessage msg = new DirectoryMessage();
        msg.jobId = jobMsg.jobId;
        msg.nodeName = jobMsg.nodeName;
        msg.dir = dir;
        msg.prodClassInclude = jobMsg.prodClassInclude;
        msg.prodClassExclude = jobMsg.prodClassExclude;
        msg.dateFields = jobMsg.dateFields;
        msg.fileRefs = jobMsg.fileRefs;
        msg.overwrite = jobMsg.overwrite;
        
        return msg;
    }


    /**
     * Create new Directory Message from a Directory Message and a directory
     * @param dirMsg job message
     * @param dir directory
     * @return new DirectoryMessage
     */
    public static DirectoryMessage create(DirectoryMessage dirMsg, String dir)
    {
        DirectoryMessage msg = new DirectoryMessage();
        msg.jobId = dirMsg.jobId;
        msg.nodeName = dirMsg.nodeName;
        msg.dir = dir;
        msg.prodClassInclude = dirMsg.prodClassInclude;
        msg.prodClassExclude = dirMsg.prodClassExclude;
        msg.dateFields = dirMsg.dateFields;
        msg.fileRefs = dirMsg.fileRefs;
        msg.overwrite = dirMsg.overwrite;
        
        return msg;
    }

}
