package com.example.manalighare.mapColoring;

public class PointFc {

        private String state;
        private double latitude;
        private double longitude;
        private int stateID;
        private int singletonDomain;
         Domain domain;
        private int color;

    public int getSingletonDomain() {
        return singletonDomain;
    }

    public void setSingletonDomain(int singletonDomain) {
        this.singletonDomain = singletonDomain;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getStateID() {
        return stateID;
    }

    public void setStateID(int stateID) {
        this.stateID = stateID;
    }


    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "PointFc{" +
                "state='" + state + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", stateID=" + stateID +
                ", singletonDomain=" + singletonDomain +
                ", domain=" + domain +
                ", color=" + color +
                '}';
    }
}


