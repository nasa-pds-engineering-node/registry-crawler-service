package gov.nasa.pds.crawler.cfg;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.util.CloseUtils;

public class ConfigurationReader
{
    private Logger log;
    
    
    public ConfigurationReader()
    {
        log = LogManager.getLogger(this.getClass());
    }

    
    public Configuration read(File file) throws Exception
    {
        Configuration cfg = new Configuration();
        
        Reader rd = null;
        try
        {
            // Read properties from a file 
            Properties props = new Properties();
            rd = new FileReader(file);
            props.load(rd);
            
            // Parse web port
            cfg.webPort = parseWebPort(props);
            
            // Parse message queue addresses
            cfg.mqAddresses = parseMQAddresses(props);
        }
        finally
        {
            CloseUtils.close(rd);
        }
                
        return cfg;
    }

    
    private int parseWebPort(Properties props) throws Exception
    {
        String tmp = props.getProperty("web.port");
        if(tmp == null)
        {
            tmp = "8001";
            log.warn("'web.port' property is not set. Will use default value: " + tmp);
        }

        try
        {
            return Integer.parseInt(tmp);
        }
        catch(Exception ex)
        {
            throw new Exception("Could not parse 'web.port' property " + tmp);
        }
    }
    
    
    private List<IPAddress> parseMQAddresses(Properties props) throws Exception
    {
        String tmp = props.getProperty("mq.host");
        if(tmp == null)
        {
            tmp = "localhost:5672";
            log.warn("'mq.host' property is not set. Will use default value: " + tmp);
        }
        
        List<IPAddress> list = new ArrayList<>();
        
        StringTokenizer tkz = new StringTokenizer(tmp, ",;");
        while(tkz.hasMoreTokens())
        {
            String item = tkz.nextToken();
            if(item == null) continue;
            item = item.trim();
            
            String[] tokens = item.split(":");
            if(tokens.length != 2) throw new Exception("Invalid host entry: '" + item + "'. Expected 'host:port' value.");
            
            String host = tokens[0];
            int port = 0;
            
            try
            {
                port = Integer.parseInt(tokens[1]);
            }
            catch(Exception ex)
            {
                throw new Exception("Invalid host entry: '" + item + "'. Could not parse port.");
            }
            
            list.add(new IPAddress(host, port));
        }
        
        return list;
    }
}
