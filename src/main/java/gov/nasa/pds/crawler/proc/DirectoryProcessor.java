package gov.nasa.pds.crawler.proc;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.Constants;
import gov.nasa.pds.crawler.meta.PdsLabelInfo;
import gov.nasa.pds.crawler.meta.PdsLabelInfoParser;
import gov.nasa.pds.crawler.mq.MQPublisher;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessage;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.crawler.mq.msg.FileBatch;
import gov.nasa.pds.crawler.mq.msg.ProductMessage;
import gov.nasa.pds.crawler.mq.msg.FileMessageBuilder;
import gov.nasa.pds.crawler.util.CloseUtils;
import gov.nasa.pds.crawler.util.ExceptionUtils;


public class DirectoryProcessor
{
    private Logger log;
    private int batchSize = Constants.FILES_MESSAGE_MAX_ITEMS;
    private PdsLabelInfoParser labelInfoParser;

    private MQPublisher publisher;
    
    
    public DirectoryProcessor(MQPublisher publisher)
    {
        log = LogManager.getLogger(this.getClass());
        labelInfoParser = new PdsLabelInfoParser();
        this.publisher = publisher;
    }

    
    /**
     * Process Directory Message
     * @param dirMsg directory message 
     * @throws IOException an exception
     */
    public void processMessage(DirectoryMessage dirMsg) throws Exception
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
                    publishDirectory(dirMsg, path);
                }
                else
                {
                    processFile(path, dirMsg, fileBatch);
                }
            }
            
            // Publish final batch if it is not empty
            publishFileBatch(dirMsg, fileBatch);
        }
        finally
        {
            CloseUtils.close(dirStream);
        }
    }

    
    /**
     * Process a file
     * @param path file path
     * @param dirMsg Directory message being processed
     * @param fileBatch file batch info
     * @throws IOException an exception
     */
    private void processFile(Path path, DirectoryMessage dirMsg, FileBatch fileBatch) throws Exception
    {
        String fileName = path.getFileName().toString().toLowerCase();
        // Only process PDS labels (XML files)
        if(!fileName.endsWith(".xml")) return;
        
        // Get PDS label info - LIDVID and product class
        String strPath = path.toAbsolutePath().toString();
        PdsLabelInfo info = getFileInfo(strPath);
        // This is not a PDS label
        if(info == null) return;
        
        // Apply product class filters (declared in the directory message)
        if(skipProductClass(dirMsg, info.productClass)) return;
        
        // Add PDS label info (path and LIDVID) to the batch
        fileBatch.add(strPath, info);
        
        // Publish batch
        if(fileBatch.size() >= batchSize)
        {
            publishFileBatch(dirMsg, fileBatch);
            fileBatch.clear();
        }
    }

    
    /**
     * Apply product class filters declared in a directory message to 
     * a product class of a PDS label file to find out if this file
     * should be skipped / ignored.
     * @param dirMsg directory message with product class filters
     * @param prodClass product class of a PDS label file
     * @return true if the PDS label file with a given product class should be skipped
     */
    private boolean skipProductClass(DirectoryMessage dirMsg, String prodClass)
    {
        if(dirMsg.prodClassInclude != null)
        {
            return !dirMsg.prodClassInclude.contains(prodClass);
        }
        
        if(dirMsg.prodClassExclude != null)
        {
            return dirMsg.prodClassExclude.contains(prodClass);
        }

        return false;
    }
    
    
    /**
     * Get basic PDS label info - LIDVID and product class
     * @param path PDS label file path
     * @return PDS label info
     */
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

    
    /**
     * Publish File Message to a RabbitMQ queue
     * @param dirMsg Directory message being processed
     * @param batch file batch info
     * @throws IOException an exception
     */
    private void publishFileBatch(DirectoryMessage dirMsg, FileBatch batch) throws Exception
    {
        if(batch.size() == 0) return;
        
        ProductMessage newMsg = FileMessageBuilder.create(dirMsg, batch);
        publisher.publish(newMsg);
    }

    
    /**
     * Publish Directory Message to a RabbitMQ queue
     * @param dirMsg Directory message being processed
     * @param path directory path
     * @throws IOException an exception
     */
    private void publishDirectory(DirectoryMessage dirMsg, Path path) throws Exception
    {
        String strPath = path.toAbsolutePath().toString();
        
        DirectoryMessage newMsg = DirectoryMessageBuilder.create(dirMsg, strPath);
        publisher.publish(newMsg);
    }

}
