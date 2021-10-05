package gov.nasa.pds.crawler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crawler main class.
 *  
 * @author karpenko
 */
public class CrawlerMain
{
    public static void main(String[] args)
    {
        // We don't use "java.util" logger.
        Logger log = Logger.getLogger("");
        log.setLevel(Level.OFF);
        
        CrawlerCli cli = new CrawlerCli();
        cli.run(args);
    }

}
