package com.plingtech.tagreader;

import androidx.room.ColumnInfo;

import java.util.Date;

public class TagView {
    @ColumnInfo (name = "COUNT(rfid)")
    private int scanCount;
    private String rfid;
    @ColumnInfo (name = "time_scanned")
    private Date timeScanned;

    public TagView(int scanCount, String rfid, Date timeScanned) {
        this.scanCount = scanCount;
        this.rfid = rfid;
        this.timeScanned = timeScanned;
    }

    // Getters & Setters
    public int getScanCount() { return scanCount; }
    public String getRfid() { return rfid; }
    public Date getTimeScanned() { return timeScanned; }

    public void setScanCount(int scanCount) { this.scanCount = scanCount; }
    public void setRfid(String rfid) { this.rfid = rfid; }
    public void setTimeScanned(Date timeScanned) { this.timeScanned = timeScanned; }

}
