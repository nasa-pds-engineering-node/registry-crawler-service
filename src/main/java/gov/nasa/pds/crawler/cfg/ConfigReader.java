package gov.nasa.pds.crawler.cfg;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gov.nasa.pds.crawler.cfg.model.Configuration;
import gov.nasa.pds.crawler.cfg.parser.DirsParser;
import gov.nasa.pds.crawler.cfg.parser.FileRefsParser;
import gov.nasa.pds.crawler.cfg.parser.NodeNameValidator;
import gov.nasa.pds.crawler.util.xml.XmlDomUtils;


/**
 * Crawler configuration file reader.
 * 
 * @author karpenko
 */
public class ConfigReader
{
    private static final String ERROR = "Invalid Crawler configuration: ";
    
    private int dirsCount = 0;
    

    /**
     * Constructor
     */
    public ConfigReader()
    {
    }
    
    
    /**
     * Read Harvest configuration file.
     * @param file Configuration file
     * @return Configuration model object
     * @throws Exception Generic exception
     */
    public Configuration read(File file) throws Exception
    {
        resetCounters();
        
        Document doc = XmlDomUtils.readXml(file);
        Element root = doc.getDocumentElement();
        if(!"crawler".equals(root.getNodeName()))
        {
            throw new Exception(ERROR + "Invalid root element '" + root.getNodeName() + "'. Expected 'crawler'.");
        }

        Configuration cfg = new Configuration();
        cfg.nodeName = XmlDomUtils.getAttribute(root, "nodeName");
        NodeNameValidator nnValidator = new NodeNameValidator();
        nnValidator.validate(cfg.nodeName);
        
        validate(root);
        
        if(dirsCount > 0) cfg.dirs = DirsParser.parseDirectories(root);
        
        //cfg.filters = FiltersParser.parseFilters(doc);
        cfg.fileRefs = FileRefsParser.parseFileInfo(doc);

        return cfg;
    }

    
    private void resetCounters()
    {
        dirsCount = 0;
    }
    
    
    private void validate(Element root) throws Exception
    {
        NodeList nodes = root.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE)
            {
                String name = node.getNodeName();
                switch(name)
                {
                case "crawler":
                    break;
                case "directories":
                    dirsCount++;
                    break;
                case "productFilter":
                    break;
                case "fileRefs":
                    break;
                default:
                    throw new Exception(ERROR + "Invalid XML element '/crawler/" + name + "'");
                }
            }
        }
        
        if(dirsCount == 0)
        {
            throw new Exception(ERROR + "Missing required element '/crawler/directories'.");
        }
    }
}
