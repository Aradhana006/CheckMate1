package com.example.project.attendance.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project.attendance.model.Attendance;

import java.util.List;

/**
 * DAO for Attendance entity
 * Provides database operations for attendance records
 */
@Dao
public interface AttendanceDao {
    
    @Insert
    long insert(Attendance attendance);
    
    @Update
    void update(Attendance attendance);
    
    @Delete
    void delete(Attendance attendance);
    
    @Query("SELECT * FROM attendance WHERE sessionId = :sessionId ORDER BY time DESC")
    List<Attendance> getAttendanceBySession(int sessionId);
    
    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC, time DESC")
    List<Attendance> getAttendanceByStudent(int studentId);
    
    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY time DESC")
    List<Attendance> getAttendanceByDate(String date);
    
    @Query("SELECT * FROM attendance WHERE sessionId = :sessionId AND studentId = :studentId LIMIT 1")
    Attendance getAttendanceBySessionAndStudent(int sessionId, int studentId);
    
    @Query("SELECT COUNT(*) FROM attendance WHERE sessionId = :sessionId AND status = 'present'")
    int getPresentCountBySession(int sessionId);
    
    @Query("SELECT COUNT(*) FROM attendance WHERE sessionId = :sessionId")
    int getTotalCountBySession(int sessionId);
    
    @Query("DELETE FROM attendance WHERE sessionId = :sessionId")
    void deleteBySession(int sessionId);
    
    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    List<Attendance> getAttendanceBetweenDates(String startDate, String endDate);
}
