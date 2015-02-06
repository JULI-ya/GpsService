package com.gpsservice.models;


public class TTK extends Region{

    String source = "37.54762792557965,55.7649646968456,0 37.54370586643243,55.74387406302518,0 37.55977017615013,55.72724996888601,0 37.5750114098969,55.72196564133972,0 37.59061454116633,55.71394704260953,0 37.60722817074023,55.70684771101249,0 37.61636950078528,55.71186692879908,0 37.6534814903199,55.7094774222067,0 37.66921373106746,55.71649905111441,0 37.69931960168805,55.72632404240427,0 37.68893170159134,55.73787512487856,0 37.69081452870677,55.74754951267779,0 37.68533848415611,55.75477280217399,0 37.68260732056422,55.76730667576323,0 37.66278192339983,55.77784801909522,0 37.64862749509219,55.78776545028541,0 37.63481848238335,55.78677069738396,0 37.58170119221739,55.78607104383747,0 37.57406695170599,55.78191322260696,0 37.54762792557965,55.7649646968456,0";
    private LatLong[] location;

    @Override
    public LatLong[] getBoundary() {
        if (location == null) {
            location = parseBoundary(source);
        }
        return location;
    }
}
