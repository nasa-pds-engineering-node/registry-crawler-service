package gov.nasa.pds.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nasa.pds.crawler.cmd.CliCommand;
import gov.nasa.pds.crawler.cmd.CrawlCmd;
import gov.nasa.pds.crawler.util.ExceptionUtils;
import gov.nasa.pds.crawler.util.ManifestUtils;
import gov.nasa.pds.crawler.util.log.Log4jConfigurator;


/**
 * Main CLI (Command-Line Interface) manager / dispatcher.
 * CrawlerCli.run() method parses command-line parameters and 
 * calls different CLI commands, such as "crawl", etc.
 *   
 * @author karpenko
 */
public class CrawlerCli
{
    private Options options;
    private CommandLine cmdLine;
    private Map<String, CliCommand> commands;
    private CliCommand command;
    
    
    /**
     * Constructor
     */
    public CrawlerCli()
    {
        initOptions();
    }

    
    /**
     * Parse command line arguments and run commands.
     * @param args command line arguments passed from the main() function.
     */
    public void run(String[] args)
    {
        if(args.length == 0)
        {
            printHelp();
            System.exit(1);
        }

        // Version
        if(args.length == 1 && ("-V".equals(args[0]) || "--version".equals(args[0])))
        {
            printVersion();
            System.exit(0);
        }        

        if(!parse(args))
        {
            System.out.println();
            printHelp();
            System.exit(1);
        }

        if(!runCommand())
        {
            System.exit(1);
        }        
    }

    
    /**
     * Parse command line parameters
     * @param args
     * @return
     */
    private boolean parse(String[] pArgs)
    {
        try
        {
            CommandLineParser parser = new DefaultParser();
            this.cmdLine = parser.parse(options, pArgs);
            
            String[] args = cmdLine.getArgs();
            if(args == null || args.length == 0)
            {
                System.out.println("[ERROR] Missing command.");
                return false;
            }

            if(args.length > 1)
            {
                System.out.println("[ERROR] Invalid command: " + String.join(" ", args)); 
                return false;
            }
            
            // NOTE: !!! Init logger before creating commands !!!
            initLogger(cmdLine);
            initCommands();
            
            this.command = commands.get(args[0]);
            if(this.command == null)
            {
                System.out.println("[ERROR] Invalid command: " + args[0]);
                return false;
            }
            
            return true;
        }
        catch(ParseException ex)
        {
            System.out.println("[ERROR] " + ex.getMessage());
            return false;
        }
    }

    
    /**
     * Run commands based on command line parameters.
     * @return
     */
    private boolean runCommand()
    {
        try
        {
            command.run(cmdLine);
            return true;
        }
        catch(Exception ex)
        {
            String msg = ExceptionUtils.getMessage(ex);
            Logger log = LogManager.getLogger(this.getClass());
            log.error(msg);
            return false;
        }
    }

    
    /**
     * Print help screen.
     */
    public void printHelp()
    {
        System.out.println("Usage: crawler <command> <options>");

        System.out.println();
        System.out.println("Commands:");
        System.out.println("  crawl           Crawl file system and queue PDS4 labels for processing");
        System.out.println("  -V, --version   Print Crawler version");
        
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -help           Print help for a command");
        System.out.println("  -l <file>       Log file. Default is /tmp/crawler/crawler.log");
        System.out.println("  -v <level>      Logger verbosity: DEBUG, INFO (default), WARN, ERROR");
        
        System.out.println();
        System.out.println("Pass -help after any command to see command-specific usage information, for example,");
        System.out.println("  crawler crawl -help");
    }
    
    
    /**
     * Initialize all CLI commands
     */
    private void initCommands()
    {
        commands = new HashMap<>();
        commands.put("crawl", new CrawlCmd());
    }
    
    /**
     * Initialize Apache Commons CLI library.
     */
    private void initOptions()
    {
        options = new Options();
        
        Option.Builder bld;

        bld = Option.builder("c").hasArg().argName("file");
        options.addOption(bld.build());
    }
    
    
    /**
     * Initialize Log4j logger
     */
    private void initLogger(CommandLine cmdLine)
    {
        String verbosity = cmdLine.getOptionValue("v", "INFO");
        String logFile = cmdLine.getOptionValue("l");

        Log4jConfigurator.configure(verbosity, logFile);
    }

    
    /**
     * Print Harvest version
     */
    public static void printVersion()
    {
        String version = CrawlerCli.class.getPackage().getImplementationVersion();
        System.out.println("Crawler version: " + version);
        Attributes attrs = ManifestUtils.getAttributes();
        if(attrs != null)
        {
            System.out.println("Build time: " + attrs.getValue("Build-Time"));
        }
    }

}
