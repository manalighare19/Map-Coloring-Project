package com.example.manalighare.mapColoring;

public class Point {
    String latitude;
    String longitude;
    String state;
    int stateID;
    float Color;

    public Point(String latitude, String longitude,String state,int Color,int stateID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.state=state;
        this.Color=-1;
        this.stateID=-1;
    }


    @Override
    public String toString() {
        return "Point{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", state='" + state + '\'' +
                ", stateID=" + stateID +
                ", Color=" + Color +
                '}';
    }
}
