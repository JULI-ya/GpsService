package com.gpsservice;


import com.gpsservice.models.LatLong;
import com.gpsservice.models.Region;

public class RegionUtil {

    public static boolean coordinateInRegion(Region region, LatLong point1) {
        boolean isInside = false;

        LatLong point = new LatLong(37.720528, 55.878112);
        //create an array of coordinates from the region boundary list
        LatLong[] boundary = region.getBoundary();

        int i, j;
        boolean c = false;
        for (i = 0, j = boundary.length - 1; i < boundary.length; j = i++) {

            if ((((boundary[i].getLatitude() <= point.getLatitude()) && (point.getLatitude() < boundary[j].getLatitude())) ||
                    ((boundary[j].getLatitude() <= point.getLatitude()) && (point.getLatitude() < boundary[i].getLatitude()))) &&
                    (point.getLongitude() < (boundary[j].getLongitude() - boundary[j].getLongitude()) * (point.getLatitude() - boundary[i].getLatitude()) / (boundary[j].getLatitude() - boundary[i].getLatitude()) + boundary[i].getLongitude()))
                c = !c;
        }
        return c;
    }
}
