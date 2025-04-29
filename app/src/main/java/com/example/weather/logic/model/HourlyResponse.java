package com.example.weather.logic.model;

import java.util.Date;
import java.util.List;

public class HourlyResponse {

    private String status;
    private Result result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {
        private Hourly hourly;

        public Hourly getHourly() {
            return hourly;
        }

        public void setHourly(Hourly hourly) {
            this.hourly = hourly;
        }
    }

    public static class Hourly {
        private List<Temperature> temperature;
        private List<Skycon> skycon;

        public List<Temperature> getTemperature() {
            return temperature;
        }

        public void setTemperature(List<Temperature> temperature) {
            this.temperature = temperature;
        }

        public List<Skycon> getSkycon() {
            return skycon;
        }

        public void setSkycon(List<Skycon> skycon) {
            this.skycon = skycon;
        }
    }

    public static class Temperature {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    public static class Skycon {
        private String value;
        private Date datetime;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Date getDatetime() {
            return datetime;
        }

        public void setDatetime(Date datetime) {
            this.datetime = datetime;
        }
    }
}

