package gov.nasa.pds.crawler.util;

import java.io.Closeable;

import javax.jms.Session;
import javax.xml.stream.XMLEventReader;

/**
 * Close resources without throwing exceptions.
 * 
 * @author karpenko
 */
public class CloseUtils
{
    public static void close(Closeable cl)
    {
        if(cl == null) return;
        
        try
        {
            cl.close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }
    

    public static void close(XMLEventReader cl)
    {
        if(cl == null) return;
        
        try
        {
            cl.close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }

    
    public static void close(Session cl)
    {
        if(cl == null) return;
        
        try
        {
            cl.close();
        }
        catch(Exception ex)
        {
            // Ignore
        }
    }

}
