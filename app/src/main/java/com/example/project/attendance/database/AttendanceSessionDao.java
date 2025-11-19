package com.example.project.attendance.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project.attendance.model.AttendanceSession;

import java.util.List;

/**
 * DAO for AttendanceSession entity
 * Provides database operations for attendance sessions
 */
@Dao
public interface AttendanceSessionDao {
    
    @Insert
    long insert(AttendanceSession session);
    
    @Update
    void update(AttendanceSession session);
    
    @Delete
    void delete(AttendanceSession session);
    
    @Query("SELECT * FROM attendance_sessions ORDER BY date DESC, startTime DESC")
    List<AttendanceSession> getAllSessions();
    
    @Query("SELECT * FROM attendance_sessions WHERE id = :id LIMIT 1")
    AttendanceSession getSessionById(int id);
    
    @Query("SELECT * FROM attendance_sessions WHERE date = :date ORDER BY startTime DESC")
    List<AttendanceSession> getSessionsByDate(String date);
    
    @Query("SELECT * FROM attendance_sessions WHERE sessionName LIKE '%' || :query || '%'")
    List<AttendanceSession> searchSessions(String query);
    
    @Query("SELECT * FROM attendance_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<AttendanceSession> getSessionsBetweenDates(String startDate, String endDate);
    
    @Query("DELETE FROM attendance_sessions")
    void deleteAll();
}
