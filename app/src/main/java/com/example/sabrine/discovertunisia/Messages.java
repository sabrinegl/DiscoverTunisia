package com.example.sabrine.discovertunisia;


public class Messages {

    public static final int BOX_TYPE_SENT = 1;
    public static final int BOX_TYPE_RECEIVED = 2;

    private String message, type;
    private long  time;
    private boolean seen;

    private String from;
    private int mBoxType;

    public Messages(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String message, String type, long time, boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Messages(){

    }

    public void setBoxType(int boxType) {
        mBoxType = boxType;
    }

    public int getBoxType() {
        return mBoxType ;
    }
}
