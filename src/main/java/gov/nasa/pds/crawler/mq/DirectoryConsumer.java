package gov.nasa.pds.crawler.mq;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;
import gov.nasa.pds.crawler.util.xml.PdsLabelInfo;
import gov.nasa.pds.crawler.util.xml.PdsLabelInfoParser;


public class DirectoryConsumer extends DefaultConsumer
{
    private Logger log;
    private int batchSize = 20;
    private Gson gson;
    private PdsLabelInfoParser labelInfoParser;
    
    
    private static class FileBatch
    {
        public List<String> paths;
        public List<String> lidvids;
        
        public FileBatch(int batchSize)
        {
            paths = new ArrayList<>(batchSize);
            lidvids = new ArrayList<>(batchSize);
        }
        
        public void add(String path, String lidvid)
        {
            paths.add(path);
            lidvids.add(lidvid);
        }
        
        public int size()
        {
            return paths.size();
        }
        
        public void clear()
        {
            paths.clear();
            lidvids.clear();
        }
    }
    
    
    public DirectoryConsumer(Channel channel)
    {
        super(channel);
        log = LogManager.getLogger(this.getClass());
        
        gson = new Gson();
        labelInfoParser = new PdsLabelInfoParser();
    }

    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, 
            AMQP.BasicProperties properties, byte[] body) throws IOException
    {
        long deliveryTag = envelope.getDeliveryTag();
        
        String jsonStr = new String(body);
        DirectoryMessage dirMsg = gson.fromJson(jsonStr, DirectoryMessage.class);
        
        processMessage(dirMsg);
        
        // ACK message (delete from the queue)
        getChannel().basicAck(deliveryTag, false);        
    }


    private void processMessage(DirectoryMessage dirMsg) throws IOException
    {
        log.info("Processing directory " + dirMsg.dir);
        
        File dir = new File(dirMsg.dir);
        DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir.toPath());
        try
        {
            FileBatch fileBatch = new FileBatch(batchSize);
            
            Iterator<Path> itr = dirStream.iterator();
            while(itr.hasNext())
            {
                Path path = itr.next();
                
                if(Files.isDirectory(path))
                {
                    publishDirectory(dirMsg.jobId, path);
                }
                else
                {
                    String fileName = path.getFileName().toString().toLowerCase();
                    if(fileName.endsWith(".xml"))
                    {
                        String strPath = path.toAbsolutePath().toString();
                        PdsLabelInfo info = getFileInfo(strPath);
                        if(info == null) continue;
                        
                        fileBatch.add(strPath, info.lidvid);
                        
                        if(fileBatch.size() >= batchSize)
                        {
                            processFileBatch(fileBatch);
                            fileBatch.clear();
                        }
                    }
                }
            }
            
            processFileBatch(fileBatch);
        }
        finally
        {
            CloseUtils.close(dirStream);
        }
    }
    
    
    private PdsLabelInfo getFileInfo(String path)
    {
        try
        {
            PdsLabelInfo info = labelInfoParser.getBasicInfo(path);
            if(info == null) log.warn("Could not get LIDVID from label " + path);
            return info;
        }
        catch(Exception ex)
        {
            log.error("Could not parse label " + path + ", " + ExceptionUtils.getMessage(ex));
        }
        
        return null;
    }
    
    
    private void processFileBatch(FileBatch batch) throws IOException
    {
        if(batch.size() == 0) return;
        
        System.out.println(batch.size());
    }

    
    private void publishDirectory(String jobId, Path path) throws IOException
    {
        String strPath = path.toAbsolutePath().toString();
        
        DirectoryMessage newDirMsg = new DirectoryMessage(jobId, strPath);
        String jsonStr = gson.toJson(newDirMsg);
        
        getChannel().basicPublish("", "q.dirs", null, jsonStr.getBytes());
    }
}
