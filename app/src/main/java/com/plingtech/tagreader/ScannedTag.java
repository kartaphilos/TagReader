package com.plingtech.tagreader;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


@Entity (tableName = "tags_table")
public class ScannedTag {

    @PrimaryKey (autoGenerate = true)
    private int id;

    @NonNull
    private Date session;

    @NonNull
    @ColumnInfo (name = "time_scanned")
    private Date timeScanned;

    @NonNull
    private String rfid;

    private String nlis;

    private int type ;
    static final int UNDEFINED = 0;
    static final int CATTLE = 1;
    static final int SHEEP = 2;
    static final int GOATS = 3;
    static final int PETS = 4;

    public ScannedTag(Date session, Date timeScanned, String rfid, String nlis, int type) {
        this.session = session;
        this.timeScanned = timeScanned;
        this.rfid = rfid;
        this.nlis = nlis;
        this.type = type;
    }

    public int getId() { return id; }
    public Date getSession() {
        return session;
    }
    public Date getTimeScanned() {
        return timeScanned;
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

    public void setId (int i) {
        id = i;
    }
    public void setSession (Date s) {
        session = s;
    }
    public void setTimeScanned (Date t) {
        timeScanned = t;
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
                + "ID="+ id
                + ", Session="+ session
                + ", timeScanned=" + timeScanned
                + ", tagRfid=" + rfid
                + ", NLIS=" + nlis
                + ", stockType=" + type
                + '}';
    }

}



