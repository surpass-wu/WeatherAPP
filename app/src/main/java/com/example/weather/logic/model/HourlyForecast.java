package com.example.weather.logic.model;

import java.util.Date;

public class HourlyForecast {

    private float temVal;
    private String skyVal;
    private Date datetime;

    public HourlyForecast(float temVal, String skyVal, Date datetime) {
        this.temVal = temVal;
        this.skyVal = skyVal;
        this.datetime = datetime;
    }

    public float getTemVal() {
        return temVal;
    }

    public void setTemVal(float temVal) {
        this.temVal = temVal;
    }

    public String getSkyVal() {
        return skyVal;
    }

    public void setSkyVal(String skyVal) {
        this.skyVal = skyVal;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
