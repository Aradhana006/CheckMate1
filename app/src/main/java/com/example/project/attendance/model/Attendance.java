package com.example.project.attendance.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Attendance entity for Room database
 * Records individual attendance entries with timestamp and scan method
 */
@Entity(tableName = "attendance",
        foreignKeys = @ForeignKey(entity = Student.class,
                parentColumns = "id",
                childColumns = "studentId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("studentId"), @Index(value = {"studentId", "sessionId", "date"})})
public class Attendance {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int studentId;
    private int sessionId;
    private String date;
    private String time;
    private String status; // "present", "absent"
    private String scanMethod; // "qr", "barcode", "ocr", "manual"
    
    public Attendance() {
    }
    
    public Attendance(int studentId, int sessionId, String date, String time, String status, String scanMethod) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.date = date;
        this.time = time;
        this.status = status;
        this.scanMethod = scanMethod;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getScanMethod() {
        return scanMethod;
    }
    
    public void setScanMethod(String scanMethod) {
        this.scanMethod = scanMethod;
    }
}
