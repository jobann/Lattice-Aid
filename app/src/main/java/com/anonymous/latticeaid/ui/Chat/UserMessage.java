package com.anonymous.latticeaid.ui.Chat;

import java.io.Serializable;
import java.util.Date;

public class UserMessage implements Serializable {
    String message;
    Date date;
    String android_id;
    double lat;
    double lng;
    int msgType;

    public UserMessage(String message, Date date, String android_id, int msgType) {
        this.message = message;
        this.date = date;
        this.android_id = android_id;
        this.msgType = msgType;
    }

    public UserMessage(String android_id, double lat, double lng, int msgType) {
        this.android_id = android_id;
        this.lat = lat;
        this.lng = lng;
        this.msgType = msgType;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lng;
    }

    public int getMsgType() {
        return msgType;
    }
}
