package gov.nasa.pds.crawler.cfg.parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import gov.nasa.pds.crawler.cfg.model.FileRefsCfg;
import gov.nasa.pds.crawler.util.xml.XPathUtils;
import gov.nasa.pds.crawler.util.xml.XmlDomUtils;


/**
 * Crawler configuration file parser. Parses "/crawler/fileRefs" section.
 * 
 * @author karpenko
 */
public class FileRefsParser
{
    /**
     * Parse &lt;fileRefs&gt; section of Crawler configuration file
     * @param doc Parsed Crawler configuration file (XMl DOM)
     * @return File info model object
     * @throws Exception an exception
     */
    public static FileRefsCfg parseFileInfo(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        FileRefsCfg fileInfo = new FileRefsCfg();
        
        int count = xpu.getNodeCount(doc, "/crawler/fileRefs");
        if(count == 0) return fileInfo;
        if(count > 1) throw new Exception("Could not have more than one '/crawler/fileRefs' element.");

        // <fileRef> nodes
        fileInfo.fileRef = parseFileRef(doc);
        
        return fileInfo;
    }
    
    
    public static List<FileRefsCfg.FileRefCfg> parseFileRef(Document doc) throws Exception
    {
        XPathUtils xpu = new XPathUtils();
        
        NodeList nodes = xpu.getNodeList(doc, "/crawler/fileRefs/fileRef");
        if(nodes == null || nodes.getLength() == 0) return null;
        
        List<FileRefsCfg.FileRefCfg> list = new ArrayList<>();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            FileRefsCfg.FileRefCfg rule = new FileRefsCfg.FileRefCfg();
            rule.prefix = XmlDomUtils.getAttribute(nodes.item(i), "replacePrefix");
            rule.replacement = XmlDomUtils.getAttribute(nodes.item(i), "with");
            
            if(rule.prefix == null) throw new Exception("'/crawler/fileRefs/fileRef' missing 'replacePrefix' attribute");
            if(rule.replacement == null) throw new Exception("'/crawler/fileRefs/fileRef' missing 'with' attribute");
            
            list.add(rule);
        }

        return list;
    }

}
