package com.example.project.attendance.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.project.attendance.model.Student;

import java.util.List;

/**
 * DAO for Student entity
 * Provides database operations for student management
 */
@Dao
public interface StudentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Student student);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Student> students);
    
    @Update
    void update(Student student);
    
    @Delete
    void delete(Student student);
    
    @Query("SELECT * FROM students ORDER BY rollNumber ASC")
    List<Student> getAllStudents();
    
    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    Student getStudentByRollNumber(String rollNumber);
    
    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    Student getStudentById(int id);
    
    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%'")
    List<Student> searchStudents(String query);
    
    @Query("SELECT COUNT(*) FROM students")
    int getStudentCount();
    
    @Query("DELETE FROM students")
    void deleteAll();
    
    @Query("SELECT rollNumber FROM students ORDER BY rollNumber ASC")
    List<String> getAllRollNumbers();
}
