package com.example.project.attendance.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Student entity for Room database
 * Stores student information imported from Excel or added manually
 */
@Entity(tableName = "students", indices = {@Index(value = "rollNumber", unique = true)})
public class Student {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String rollNumber;
    private String name;
    private String department;
    private String year;
    private String email;
    
    public Student() {
    }
    
    public Student(String rollNumber, String name, String department, String year, String email) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.department = department;
        this.year = year;
        this.email = email;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getRollNumber() {
        return rollNumber;
    }
    
    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getYear() {
        return year;
    }
    
    public void setYear(String year) {
        this.year = year;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
