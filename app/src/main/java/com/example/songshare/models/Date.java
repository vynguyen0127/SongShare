package com.example.songshare.models;

import org.parceler.Parcel;

@Parcel
public class Date {
    int year;
    int month;
    int day;

    public Date(){}

    public Date(String s){

        year = Integer.parseInt(s.substring(0, 4));
        month = Integer.parseInt(s.substring(5, 7));
        day = Integer.parseInt(s.substring(8, 10));
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}
