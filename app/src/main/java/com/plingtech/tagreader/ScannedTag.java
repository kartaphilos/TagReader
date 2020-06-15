package com.plingtech.tagreader;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity (tableName = "tags_table")
public class ScannedTag {

    @PrimaryKey (autoGenerate = true)
    private int scanId;

    @NonNull
    private Date session;

    @NonNull
    private Date timestamp;

    @NonNull
    private String rfid;

    private String nlis;

    private int type ;
    static final int UNDEFINED = 0;
    static final int CATTLE = 1;
    static final int SHEEP = 2;
    static final int GOATS = 3;
    static final int PETS = 4;

    public ScannedTag(Date session, Date timestamp, String rfid, String nlis, int type) {
        this.session = session;
        this.timestamp = timestamp;
        this.rfid = rfid;
        this.nlis = nlis;
        this.type = type;
    }

    public int getScanId() { return scanId; }
    public Date getSession() {
        return session;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public String getRfid() {
            return this.rfid;
    }
    public String getNlis() {
            return this.nlis;
    }
    public int getType() {
        return type;
    }

    public void setScanId (int i) {
        scanId = i;
    }
    public void setSession (Date s) {
        session = s;
    }
    public void setTimestamp (Date t) {
        timestamp = t;
    }
    public void setRfid(String r) {
        rfid = r;
    }
    public void setTagNlis(String n) {
        nlis = n;
    }
    public void setType (int t) {
        type = t;
    }

    @Override
    public String toString() {
        return "ScannedTag{"
                + "ScanID="+ scanId
                + ", Session="+ session
                + ", timestamp=" + timestamp
                + ", tagRfid=" + rfid
                + ", NLIS=" + nlis
                + ", stockType=" + type
                + '}';
    }

}



