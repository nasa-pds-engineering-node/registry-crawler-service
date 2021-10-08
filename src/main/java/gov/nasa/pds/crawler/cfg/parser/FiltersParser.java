package gov.nasa.pds.crawler.cfg.parser;

import org.w3c.dom.Document;

import gov.nasa.pds.crawler.cfg.model.FiltersCfg;
import gov.nasa.pds.crawler.util.xml.XPathUtils;


/**
 * Harvest configuration file parser. Parses "/harvest/productFilter" section.
 * 
 * @author karpenko
 */
public class FiltersParser
{
    public static FiltersCfg parseFilters(Document doc) throws Exception
    {
        FiltersCfg cfg = new FiltersCfg();
        parseProductFilter(doc, cfg);
        return cfg;
    }

    
    private static void parseProductFilter(Document doc, FiltersCfg cfg) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        int count = xpu.getNodeCount(doc, "/crawler/productFilter");
        if(count > 1) throw new Exception("Could not have more than one '/crawler/productFilter' element.");

        cfg.prodClassInclude = xpu.getStringSet(doc, "/crawler/productFilter/includeClass");
        cfg.prodClassExclude = xpu.getStringSet(doc, "/crawler/productFilter/excludeClass");
        
        if(cfg.prodClassInclude != null && cfg.prodClassInclude.size() > 0 
                && cfg.prodClassExclude != null && cfg.prodClassExclude.size() > 0)
        {
            throw new Exception("<productFilter> could not have both <include> and <exclude> at the same time.");
        }

    }

}
