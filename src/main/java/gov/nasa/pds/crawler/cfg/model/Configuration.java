package gov.nasa.pds.crawler.cfg.model;

import java.util.List;


/**
 * Crawler configuration model
 * 
 * @author karpenko
 */
public class Configuration
{
    public String nodeName;

    public RegistryCfg registryCfg;
    
    public List<BundleCfg> bundles;
    public List<String> dirs;
    
    public FiltersCfg filters;
    
    public FileRefsCfg fileRefs;
    
}
