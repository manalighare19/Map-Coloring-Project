package com.example.manalighare.mapColoring;

public class PointUSAFc {

    private String state;
    private double latitude;
    private double longitude;
    private int stateID;
    DomainUSA domain;
    private int color;

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

    public DomainUSA getDomain() {
        return domain;
    }

    public void setDomain(DomainUSA domain) {
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
                ", domain='" + domain + '\'' +
                ", color=" + color +
                '}';
    }
}


