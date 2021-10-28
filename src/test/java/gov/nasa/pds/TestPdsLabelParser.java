package gov.nasa.pds;

import gov.nasa.pds.crawler.meta.PdsLabelInfo;
import gov.nasa.pds.crawler.meta.PdsLabelInfoParser;

public class TestPdsLabelParser
{

    public static void main(String[] args) throws Exception
    {
        PdsLabelInfoParser parser = new PdsLabelInfoParser();

        PdsLabelInfo info = parser.getBasicInfo("/tmp/d2/collection_calibration.xml");
        System.out.println(info);

        info = parser.getBasicInfo("/tmp/d5/bundle_kaguya_derived.xml");
        System.out.println(info);
    }

}
