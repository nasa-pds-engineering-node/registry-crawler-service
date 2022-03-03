package gov.nasa.pds.crawler.meta;

import java.io.FileReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import gov.nasa.pds.registry.common.util.CloseUtils;


/**
 * Streaming XML parser to extract basic information from PDS label files.
 * @author karpenko
 *
 */
public class PdsLabelInfoParser
{
    private XMLInputFactory factory;
    
    /**
     * Constructor
     */
    public PdsLabelInfoParser()
    {
        factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
    }
    
    
    private PdsLabelInfo createLabelInfo(String productClass)
    {
        PdsLabelInfo info;
        
        if("Product_Collection".equals(productClass))
        {
            info = new PdsCollectionInfo();
        }
        else
        {
            info = new PdsLabelInfo();
        }
        
        info.productClass = productClass;
        return info;
    }
    
    
    /**
     * Extract basic PDS label information
     * @param path PDS label file path
     * @return an instance of PdsLabelInfo or its sub classes.
     * @throws Exception an exception
     */
    public PdsLabelInfo getBasicInfo(String path) throws Exception
    {
        XMLEventReader reader = null;
        
        try
        {
            reader = factory.createXMLEventReader(new FileReader(path));
            
            // Root element / product class
            String productClass = getNextElementName(reader);
            if(productClass == null || !productClass.startsWith("Product_")) return null;

            PdsLabelInfo info = createLabelInfo(productClass);

            if(findElement(reader, "Identification_Area") == null) return null;

            // Lid
            if(!"logical_identifier".equals(getNextElementName(reader))) return null;
            String lid = trim(reader.getElementText());
            if(lid == null) return null;
            
            // Vid
            if(!"version_id".equals(getNextElementName(reader))) return null;
            String vid = trim(reader.getElementText());
            if(vid == null) return null;
            
            info.lidvid = lid + "::" + vid;
            
            // Extract collection inventory file name
            if("Product_Collection".equals(productClass))
            {
                ((PdsCollectionInfo)info).inventoryFileName = getInventoryFileName(reader);
            }
            
            return info;
        }
        finally
        {
            CloseUtils.close(reader);
        }
    }
    
    
    private String getInventoryFileName(XMLEventReader reader) throws Exception
    {
        if(findElement(reader, "File_Area_Inventory") == null) return null;
        if(!"File".equals(getNextElementName(reader))) return null;
        if(!"file_name".equals(getNextElementName(reader))) return null;

        String fileName = trim(reader.getElementText());
        return fileName;
    }
    
    
    private StartElement getNextElement(XMLEventReader reader) throws Exception
    {
        while(reader.hasNext())
        {
            XMLEvent event = reader.nextEvent();
            if(event.isStartElement())
            {
                return event.asStartElement();
            }
        }
        
        return null;
    }

    
    private String getNextElementName(XMLEventReader reader) throws Exception
    {
        StartElement el = getNextElement(reader);
        if(el == null) return null;

        return el.getName().getLocalPart();
    }

    
    private StartElement findElement(XMLEventReader reader, String findName) throws Exception
    {
        while(reader.hasNext())
        {
            XMLEvent event = reader.nextEvent();
            if(event.isStartElement())
            {
                StartElement el = event.asStartElement();
                String name = el.getName().getLocalPart();
                if(name.equals(findName))
                {
                    return el;
                }
            }
        }
        
        return null;
    }

    
    private String trim(String str)
    {
        if(str == null) return null;
        
        str = str.trim();
        if(str.isEmpty()) return null;
        
        return str;
    }
}
