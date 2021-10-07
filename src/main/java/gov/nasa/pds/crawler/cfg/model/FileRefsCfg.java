package gov.nasa.pds.crawler.cfg.model;

import java.util.List;


/**
 * Crawler configuration model.
 * 
 * @author karpenko
 */
public class FileRefsCfg
{
    public static class FileRefCfg
    {
        public String prefix;
        public String replacement;
    }

    public List<FileRefCfg> fileRef;
    
    public FileRefsCfg()
    {
    }
}
