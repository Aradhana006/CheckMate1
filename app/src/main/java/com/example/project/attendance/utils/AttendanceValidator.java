package com.example.project.attendance.utils;

import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.Student;

import java.util.regex.Pattern;

/**
 * Utility class for validating attendance data
 * Handles roll number validation and duplicate checking
 */
public class AttendanceValidator {
    
    // Common roll number patterns (can be customized)
    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");
    
    /**
     * Validate roll number format
     * 
     * @param rollNumber Roll number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRollNumber(String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return false;
        }
        
        String cleanRollNumber = rollNumber.trim().toUpperCase();
        return ROLL_NUMBER_PATTERN.matcher(cleanRollNumber).matches();
    }
    
    /**
     * Check if a student exists in the database
     * 
     * @param student Student object
     * @return true if student exists (not null), false otherwise
     */
    public static boolean isStudentExists(Student student) {
        return student != null;
    }
    
    /**
     * Check if attendance already marked for a student in a session
     * 
     * @param attendance Attendance object
     * @return true if attendance exists (duplicate), false otherwise
     */
    public static boolean isDuplicateAttendance(Attendance attendance) {
        return attendance != null;
    }
    
    /**
     * Sanitize roll number (remove spaces, convert to uppercase)
     * 
     * @param rollNumber Roll number to sanitize
     * @return Sanitized roll number
     */
    public static String sanitizeRollNumber(String rollNumber) {
        if (rollNumber == null) {
            return "";
        }
        return rollNumber.trim().toUpperCase().replaceAll("\\s+", "");
    }
    
    /**
     * Validate email format
     * 
     * @param email Email address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }
        
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailPattern, email.trim());
    }
    
    /**
     * Validate student data completeness
     * 
     * @param student Student object
     * @return Validation message (null if valid)
     */
    public static String validateStudent(Student student) {
        if (student == null) {
            return "Student data is null";
        }
        
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            return "Roll number is required";
        }
        
        if (!isValidRollNumber(student.getRollNumber())) {
            return "Invalid roll number format";
        }
        
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return "Student name is required";
        }
        
        if (student.getEmail() != null && !student.getEmail().isEmpty() && !isValidEmail(student.getEmail())) {
            return "Invalid email format";
        }
        
        return null; // Valid
    }
}
