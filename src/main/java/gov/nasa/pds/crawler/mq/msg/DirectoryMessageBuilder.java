package gov.nasa.pds.crawler.mq.msg;

import gov.nasa.pds.registry.common.mq.msg.DirectoryMessage;
import gov.nasa.pds.registry.common.mq.msg.JobMessage;

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
    public static DirectoryMessage createDirectoryMessage(JobMessage jobMsg, String dir)
    {
        DirectoryMessage msg = create(jobMsg);
        msg.dir = dir;

        return msg;
    }

    
    /**
     * Create new Directory Message from a Job Message and a manifest file
     * @param jobMsg job message
     * @param manifest manifest
     * @return new DirectoryMessage
     */
    public static DirectoryMessage createManifestMessage(JobMessage jobMsg, String manifest)
    {
        DirectoryMessage msg = create(jobMsg);
        msg.manifest = manifest;

        return msg;
    }

    
    private static DirectoryMessage create(JobMessage jobMsg)
    {
        DirectoryMessage msg = new DirectoryMessage();
        msg.jobId = jobMsg.jobId;
        msg.nodeName = jobMsg.nodeName;
        msg.prodClassInclude = jobMsg.prodClassInclude;
        msg.prodClassExclude = jobMsg.prodClassExclude;
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
        msg.fileRefs = dirMsg.fileRefs;
        msg.overwrite = dirMsg.overwrite;
        
        return msg;
    }

}
