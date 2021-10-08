package gov.nasa.pds.crawler.pub;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import gov.nasa.pds.crawler.cfg.model.Configuration;

public class DataPublisher
{
    private Logger log;
    private DataMessage msg;
    private Gson gson;
    
    public DataPublisher(Configuration cfg, String runId)
    {
        log = LogManager.getLogger(this.getClass());

        // JSON serializer
        gson = new Gson();
        
        // Message template
        msg = new DataMessage();
        msg.rid = runId;
        msg.node = cfg.nodeName;
        
        if(cfg.fileRefs.fileRef != null && !cfg.fileRefs.fileRef.isEmpty())
        {
            msg.fref = new ArrayList<String>();
            cfg.fileRefs.fileRef.forEach((item) -> {
                String msgItem = item.prefix + "|" + item.replacement;
                msg.fref.add(msgItem);
            });
        }
    }
    
    
    public void publish(String filePath)
    {
        msg.file = filePath;
        String jsonMsg = gson.toJson(msg);
        
        //log.info(jsonMsg);
    }
}
