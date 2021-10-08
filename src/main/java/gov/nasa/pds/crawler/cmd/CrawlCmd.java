package gov.nasa.pds.crawler.cmd;

import java.io.File;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.cfg.ConfigReader;
import gov.nasa.pds.crawler.cfg.model.Configuration;
import gov.nasa.pds.crawler.proc.DirsProcessor;
import gov.nasa.pds.crawler.pub.DataPublisher;
import gov.nasa.pds.crawler.util.log.LogUtils;


/**
 * A CLI command to crawl files and queue them for processing.  
 * 
 * @author karpenko
 */
public class CrawlCmd implements CliCommand
{
    private Logger log;
    private Configuration cfg;
    private String runId;
    
    private DirsProcessor dirsProc;
    
    
    /**
     * Constructor
     */
    public CrawlCmd(String runId)
    {
        this.runId = runId;
    }
    

    @Override
    public void run(CommandLine cmdLine) throws Exception
    {
        log = LogManager.getLogger(this.getClass());
        
        if(cmdLine.hasOption("help"))
        {
            printHelp();
            return;
        }

        configure(cmdLine);

        if(cfg.dirs == null) return;
        for(String path: cfg.dirs)
        {
            processDirectory(path);
        }
    }

    
    private void processDirectory(String path) throws Exception
    {
        File rootDir = new File(path);
        if(!rootDir.exists())
        {
            log.warn("Invalid path: " + rootDir.getAbsolutePath());
            return;
        }
        
        log.info("Processing directory: " + rootDir.getAbsolutePath());
        
        dirsProc.process(rootDir);
    }

    
    /**
     * Print help screen.
     */
    public void printHelp()
    {
        System.out.println("Usage: crawler crawl <options>");

        System.out.println();
        System.out.println("Crawl file system and queue PDS4 labels for processing");
        System.out.println();
        System.out.println("Required parameters:");
        System.out.println("  -c <path>   Configuration file");
        System.out.println();
        System.out.println("Optional parameters:");
        System.out.println("  -dry        Dry run. Run crawler without queueing PDS4 labels for processing.");
        
        System.out.println();
    }

    
    private void configure(CommandLine cmdLine) throws Exception
    {
        // Configuration file
        String fileName = cmdLine.getOptionValue("c");
        if(fileName == null) throw new Exception("Missing required parameter '-c'");
        
        File cfgFile = new File(fileName);
        log.log(LogUtils.LEVEL_SUMMARY, "Reading configuration from " + cfgFile.getAbsolutePath());
        ConfigReader cfgReader = new ConfigReader();
        cfg = cfgReader.read(cfgFile);
        
        // Dry run option
        boolean dryRun = cmdLine.hasOption("dry");

        DataPublisher pub = new DataPublisher(cfg, runId);                
        dirsProc = new DirsProcessor(cfg, pub);
        
        log.log(LogUtils.LEVEL_SUMMARY, "Run (Package) ID: " + runId);        
        
    }
}
