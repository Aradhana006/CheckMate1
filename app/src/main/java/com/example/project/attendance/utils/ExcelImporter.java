package com.example.project.attendance.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.project.attendance.model.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing student data from Excel files
 * Supports both .xlsx and .xls formats
 */
public class ExcelImporter {
    
    private static final String TAG = "ExcelImporter";
    
    /**
     * Import students from Excel file
     * Expected columns: Roll Number, Name, Department, Year, Email
     * 
     * @param context Application context
     * @param fileUri URI of the Excel file
     * @return List of Student objects
     */
    public static List<Student> importStudents(Context context, Uri fileUri) throws Exception {
        List<Student> students = new ArrayList<>();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) {
                throw new Exception("Cannot open file");
            }
            
            Workbook workbook;
            String fileName = fileUri.getLastPathSegment();
            
            // Determine file type and create appropriate workbook
            if (fileName != null && fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String rollNumber = getCellValueAsString(row.getCell(0));
                    String name = getCellValueAsString(row.getCell(1));
                    String department = getCellValueAsString(row.getCell(2));
                    String year = getCellValueAsString(row.getCell(3));
                    String email = getCellValueAsString(row.getCell(4));
                    
                    // Skip if roll number is empty
                    if (rollNumber == null || rollNumber.trim().isEmpty()) {
                        continue;
                    }
                    
                    Student student = new Student(
                            rollNumber.trim(),
                            name != null ? name.trim() : "",
                            department != null ? department.trim() : "",
                            year != null ? year.trim() : "",
                            email != null ? email.trim() : ""
                    );
                    
                    students.add(student);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing row " + i + ": " + e.getMessage());
                }
            }
            
            workbook.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error importing Excel: " + e.getMessage());
            throw e;
        }
        
        return students;
    }
    
    /**
     * Get cell value as string, handling different cell types
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle numbers as strings (for roll numbers)
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * Preview first few rows of Excel file
     * 
     * @param context Application context
     * @param fileUri URI of the Excel file
     * @param rowCount Number of rows to preview
     * @return List of string arrays representing rows
     */
    public static List<String[]> previewExcel(Context context, Uri fileUri, int rowCount) throws Exception {
        List<String[]> preview = new ArrayList<>();
        
        try (InputStream inputStream = context.getContentResolver().openInputStream(fileUri)) {
            if (inputStream == null) {
                throw new Exception("Cannot open file");
            }
            
            Workbook workbook;
            String fileName = fileUri.getLastPathSegment();
            
            if (fileName != null && fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            int maxRows = Math.min(rowCount, sheet.getLastRowNum() + 1);
            
            for (int i = 0; i < maxRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                String[] rowData = new String[5];
                for (int j = 0; j < 5; j++) {
                    rowData[j] = getCellValueAsString(row.getCell(j));
                }
                preview.add(rowData);
            }
            
            workbook.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error previewing Excel: " + e.getMessage());
            throw e;
        }
        
        return preview;
    }
}
