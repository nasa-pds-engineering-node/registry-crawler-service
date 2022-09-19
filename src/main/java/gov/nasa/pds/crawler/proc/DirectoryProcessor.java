package gov.nasa.pds.crawler.proc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.meta.PdsCollectionInfo;
import gov.nasa.pds.crawler.meta.PdsLabelInfo;
import gov.nasa.pds.crawler.meta.PdsLabelInfoParser;
import gov.nasa.pds.crawler.mq.MQPublisher;
import gov.nasa.pds.crawler.mq.msg.CollectionInventoryMessageBuilder;
import gov.nasa.pds.crawler.mq.msg.DirectoryMessageBuilder;
import gov.nasa.pds.crawler.mq.msg.FileBatch;
import gov.nasa.pds.crawler.mq.msg.ProductMessageBuilder;
import gov.nasa.pds.registry.common.mq.msg.CollectionInventoryMessage;
import gov.nasa.pds.registry.common.mq.msg.DirectoryMessage;
import gov.nasa.pds.registry.common.mq.msg.ProductMessage;
import gov.nasa.pds.registry.common.util.CloseUtils;
import gov.nasa.pds.registry.common.util.ExceptionUtils;


/**
 * Process Directory Messages
 * @author karpenko
 */
public class DirectoryProcessor
{
    private static final int FILES_MESSAGE_MAX_ITEMS = 50;
    
    private Logger log;
    private int batchSize = FILES_MESSAGE_MAX_ITEMS;
    private PdsLabelInfoParser labelInfoParser;

    private MQPublisher publisher;
    
    
    /**
     * Constructor
     * @param publisher message queue publisher
     */
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
        if(dirMsg.dir != null)
        {
            processDirectoryMessage(dirMsg);
            return;
        }
        
        if(dirMsg.manifest != null)
        {
            processManifestMessage(dirMsg);
        }
    }
    
    
    private void processDirectoryMessage(DirectoryMessage dirMsg) throws Exception
    {
        log.info("Processing directory " + dirMsg.dir);
        
        File dir = new File(dirMsg.dir);
        if(!dir.exists())
        {
            log.warn("Directory doesn't exist: " + dir.getAbsolutePath());
            return;
        }
        
        DirectoryStream<Path> dirStream = null;
        try
        {
            dirStream = Files.newDirectoryStream(dir.toPath());
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
                    processFile(path.toFile(), dirMsg, fileBatch);
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

    
    private void processManifestMessage(DirectoryMessage dirMsg) throws Exception
    {
        log.info("Processing manifest " + dirMsg.manifest);
        
        File manifest = new File(dirMsg.manifest);
        if(!manifest.exists())
        {
            log.warn("Manifest doesn't exist: " + manifest.getAbsolutePath());
            return;
        }

        BufferedReader rd = null;
        try
        {
            FileBatch fileBatch = new FileBatch(batchSize);
            
            rd = new BufferedReader(new FileReader(manifest));
            
            String line;
            while((line = rd.readLine()) != null)
            {
                line = line.trim();
                if(line.length() == 0 || line.startsWith("#")) continue;
                
                File file = new File(line);
                if(!file.exists())
                {
                    log.warn("File " + line + " doesn't exist");
                    continue;
                }
                
                processFile(file, dirMsg, fileBatch);
            }
            
            // Publish final batch if it is not empty
            publishFileBatch(dirMsg, fileBatch);
        }
        finally
        {
            CloseUtils.close(rd);
        }
    }
    
    
    /**
     * Process a file
     * @param path file path
     * @param dirMsg Directory message being processed
     * @param fileBatch file batch info
     * @throws IOException an exception
     */
    private void processFile(File path, DirectoryMessage dirMsg, FileBatch fileBatch) throws Exception
    {
        String fileName = path.getName().toLowerCase();
        // Only process PDS labels (XML files)
        if(!fileName.endsWith(".xml")) return;
        
        // Get PDS label info - LIDVID and product class
        String strPath = path.getAbsolutePath();
        PdsLabelInfo info = getFileInfo(strPath);
        
        // This is not a PDS label
        if(info == null) return;
        
        // Apply product class filters (declared in the directory message)
        if(skipProductClass(dirMsg, info.productClass)) return;
        
        // Collection label
        if(info instanceof PdsCollectionInfo)
        {
            // Allow a Collection Inventory to overwrite the same ID added as a
            // result of a situation where Harvest Service consumed a FileBatch, which includes the ID
            // of Collection Inventory, before processing the CollectionInventory.
            // Refer to https://github.com/NASA-PDS/registry-harvest-service/issues/25
            dirMsg.overwrite = true;

            publishCollectionInventory(dirMsg, path, (PdsCollectionInfo)info);
        }
        
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
        
        ProductMessage newMsg = ProductMessageBuilder.create(dirMsg, batch);
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

    
    private void publishCollectionInventory(DirectoryMessage dirMsg, File collectionFile, 
            PdsCollectionInfo info) throws Exception
    {
        CollectionInventoryMessage newMsg = CollectionInventoryMessageBuilder.create(dirMsg, collectionFile, info);
        publisher.publish(newMsg);
    }

}
