package gov.nasa.pds.crawler.mq.msg;

public class DirectoryMessage
{
    public String jobId;
    public String dir;
    
    public DirectoryMessage(String jobId, String dir)
    {
        this.jobId = jobId;
        this.dir = dir;
    }
}
