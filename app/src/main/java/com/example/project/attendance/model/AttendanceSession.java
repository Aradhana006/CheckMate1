package com.example.project.attendance.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * AttendanceSession entity for Room database
 * Represents a single attendance session (e.g., a class, event, or meeting)
 */
@Entity(tableName = "attendance_sessions")
public class AttendanceSession {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String sessionName;
    private String date;
    private String startTime;
    private String endTime;
    private int totalStudents;
    private int presentCount;
    
    public AttendanceSession() {
    }
    
    public AttendanceSession(String sessionName, String date, String startTime) {
        this.sessionName = sessionName;
        this.date = date;
        this.startTime = startTime;
        this.totalStudents = 0;
        this.presentCount = 0;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getSessionName() {
        return sessionName;
    }
    
    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public int getTotalStudents() {
        return totalStudents;
    }
    
    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    public int getPresentCount() {
        return presentCount;
    }
    
    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }
    
    public int getAbsentCount() {
        return totalStudents - presentCount;
    }
    
    public double getAttendancePercentage() {
        if (totalStudents == 0) return 0.0;
        return (presentCount * 100.0) / totalStudents;
    }
}
