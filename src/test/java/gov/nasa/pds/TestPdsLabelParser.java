package gov.nasa.pds;

import gov.nasa.pds.crawler.util.xml.PdsLabelInfo;
import gov.nasa.pds.crawler.util.xml.PdsLabelParser;


public class TestPdsLabelParser
{

    public static void main(String[] args) throws Exception
    {
        PdsLabelParser parser = new PdsLabelParser();
        PdsLabelInfo info = parser.getBasicInfo("/tmp/d1/1294638377.xml");
        System.out.println(info);
    }

}
