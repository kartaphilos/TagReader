package com.plingtech.tagreader;

public class ScannedTag {

    private final String rfid;
    private String nlis;
    private String timestamp;
    private int type ;
    static final int UNDEFINED = 0;
    static final int CATTLE = 1;
    static final int SHEEP = 2;
    static final int GOATS = 3;

    public ScannedTag(String rfid, String nlis, String timestamp, int type) {
        this.rfid = rfid;
        this.nlis = nlis;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getTagRfid() {
            return this.rfid;
    }

    public String getTagNlis() {
            return this.nlis;
    }

    public void setTagNlis(String n) {
        nlis = n;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp (String t) {
        timestamp = t;
    }

    public int getStockTye() {
        return type;
    }

    public void setStockType (int t) {
        type = t;
    }

    @Override
    public String toString() {
        return "ScannedTag{"
                + "tagRfid=" + rfid
                + ", NLIS=" + nlis
                + ", timestamp=" + timestamp
                + ", stockType=" + type
                + '}';
    }

}



