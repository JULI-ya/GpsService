package com.gpsservice.models;


public abstract class Region {

    public abstract LatLong[] getBoundary();

    public LatLong[] parseBoundary(String source) {
        String[] splitted = source.split(",0");
        LatLong[] boundary = new LatLong[splitted.length];
        for (int i = 0; i < splitted.length; i++) {
            String latLongString = splitted[i];
            String[] latLongSp = latLongString.split(",");
            LatLong latLong = new LatLong(Double.parseDouble(latLongSp[0]), Double.parseDouble(latLongSp[1]));
            boundary[i] = latLong;
        }
        return boundary;
    }
}
