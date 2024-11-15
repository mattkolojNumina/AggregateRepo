package packlist;

import rds.RDSUtil;

public class TestPacklist {

    public static void main( String[] args) {
        System.out.println(RDSUtil.stringToDouble("11.0", 0));


        String testString = "11.000";
        int periodIndex = testString.indexOf(".");
        System.out.println(testString.substring(0, periodIndex));
    }
}
