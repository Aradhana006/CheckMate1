package com.example.project.attendance.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.AttendanceSession;
import com.example.project.attendance.model.Student;

/**
 * Room Database for Attendance System
 * Manages students, attendance records, and attendance sessions
 */
@Database(entities = {Student.class, Attendance.class, AttendanceSession.class}, version = 1, exportSchema = false)
public abstract class AttendanceDatabase extends RoomDatabase {
    
    private static AttendanceDatabase instance;
    
    public abstract StudentDao studentDao();
    public abstract AttendanceDao attendanceDao();
    public abstract AttendanceSessionDao attendanceSessionDao();
    
    public static synchronized AttendanceDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AttendanceDatabase.class,
                    "attendance_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
