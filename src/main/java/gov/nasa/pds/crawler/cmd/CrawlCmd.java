package gov.nasa.pds.crawler.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A CLI command to crawl files and queue them for processing.  
 * 
 * @author karpenko
 */
public class CrawlCmd implements CliCommand
{
    private Logger log;
    
    /**
     * Constructor
     */
    public CrawlCmd()
    {
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
        
        log.info("Crawling");

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
        System.out.println("  -file <path>      Output file path");
        System.out.println("  -lidvid <id>      Export data by lidvid");
        System.out.println("  -packageId <id>   Export data by package id");
        System.out.println("  -all              Export all data");
        System.out.println("Optional parameters:");
        System.out.println("  -auth <file>      Authentication config file");
        System.out.println();
    }

}
