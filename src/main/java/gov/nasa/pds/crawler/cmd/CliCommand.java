package gov.nasa.pds.crawler.cmd;

import org.apache.commons.cli.CommandLine;

/**
 * All Command-line interface (CLI) commands should implement this interface.
 * 
 * @author karpenko
 */
public interface CliCommand
{
    /**
     * Run CLI command. 
     * @param cmdLine Command line parameters.
     * @throws Exception an exception
     */
    public void run(CommandLine cmdLine) throws Exception;
}
