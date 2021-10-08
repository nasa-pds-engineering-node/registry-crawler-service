package gov.nasa.pds.crawler.proc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.cfg.model.Configuration;
import gov.nasa.pds.crawler.cfg.model.FileRefsCfg;
import gov.nasa.pds.crawler.pub.DataPublisher;


/**
 * Crawl PDS label files in a directory.
 * 
 * @author karpenko
 */
public class DirsProcessor
{
    private Logger log;
    private FileRefsCfg fileRefsCfg;
    private DataPublisher pub;
    
    
    public DirsProcessor(Configuration cfg, DataPublisher pub)
    {
        log = LogManager.getLogger(this.getClass());
        fileRefsCfg = cfg.fileRefs;
        this.pub = pub;
    }
    
    
    /**
     * Inner class used by File.find() to select XML PDS label files
     * @author karpenko
     */
    private static class FileMatcher implements BiPredicate<Path, BasicFileAttributes>
    {
        @Override
        public boolean test(Path path, BasicFileAttributes attrs)
        {
            String fileName = path.getFileName().toString().toLowerCase();
            return (fileName.endsWith(".xml"));
        }
    }

    /**
     * Process a directory
     * @param dir Directory with PDS4 labels
     * @throws Exception Generic exception
     */
    public void process(File dir) throws Exception
    {
        Iterator<Path> it = Files.find(dir.toPath(), Integer.MAX_VALUE, new FileMatcher()).iterator();
        
        while(it.hasNext())
        {
            onFile(it.next().toFile());
        }
    }
    
    
    /**
     * Process one file
     * @param file PDS label XML file
     * @throws Exception Generic exception
     */
    private void onFile(File file) throws Exception
    {
        String filePath = file.getAbsolutePath();
        pub.publish(filePath);
    }
    
}
