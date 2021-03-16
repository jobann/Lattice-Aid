package com.anonymous.latticeaid.ui.Chat;

import java.io.Serializable;
import java.util.Date;

public class UserMessage implements Serializable {
    String message;
    Date date;
    String android_id;

    public UserMessage(String message, Date date, String android_id) {
        this.message = message;
        this.date = date;
        this.android_id = android_id;
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
}
