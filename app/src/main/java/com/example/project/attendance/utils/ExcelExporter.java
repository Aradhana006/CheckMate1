package com.example.project.attendance.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.AttendanceSession;
import com.example.project.attendance.model.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class for exporting attendance data to Excel files
 * Generates formatted Excel reports with summary statistics
 */
public class ExcelExporter {
    
    private static final String TAG = "ExcelExporter";
    
    /**
     * Export attendance report to Excel
     * 
     * @param context Application context
     * @param session Attendance session
     * @param attendanceList List of attendance records
     * @param studentMap Map of student ID to Student object
     * @return URI of the created file
     */
    public static Uri exportAttendanceReport(Context context, 
                                            AttendanceSession session,
                                            List<Attendance> attendanceList,
                                            Map<Integer, Student> studentMap) throws Exception {
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");
        
        // Create header style
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle summaryStyle = createSummaryStyle(workbook);
        
        int rowNum = 0;
        
        // Title row
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ATTENDANCE REPORT");
        titleCell.setCellStyle(headerStyle);
        
        // Session info
        rowNum++;
        createInfoRow(sheet, rowNum++, "Session Name:", session.getSessionName());
        createInfoRow(sheet, rowNum++, "Date:", session.getDate());
        createInfoRow(sheet, rowNum++, "Time:", session.getStartTime() + 
                (session.getEndTime() != null ? " - " + session.getEndTime() : ""));
        
        // Summary section
        rowNum++;
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryCell = summaryHeaderRow.createCell(0);
        summaryCell.setCellValue("SUMMARY");
        summaryCell.setCellStyle(summaryStyle);
        
        createInfoRow(sheet, rowNum++, "Total Students:", String.valueOf(session.getTotalStudents()));
        createInfoRow(sheet, rowNum++, "Present:", String.valueOf(session.getPresentCount()));
        createInfoRow(sheet, rowNum++, "Absent:", String.valueOf(session.getAbsentCount()));
        createInfoRow(sheet, rowNum++, "Percentage:", 
                String.format(Locale.US, "%.2f%%", session.getAttendancePercentage()));
        
        // Attendance data header
        rowNum++;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Sr. No.", "Roll Number", "Name", "Department", "Year", "Status", "Time", "Scan Method"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Attendance data rows
        int srNo = 1;
        for (Attendance attendance : attendanceList) {
            Row row = sheet.createRow(rowNum++);
            Student student = studentMap.get(attendance.getStudentId());
            
            row.createCell(0).setCellValue(srNo++);
            row.createCell(1).setCellValue(student != null ? student.getRollNumber() : "N/A");
            row.createCell(2).setCellValue(student != null ? student.getName() : "N/A");
            row.createCell(3).setCellValue(student != null ? student.getDepartment() : "N/A");
            row.createCell(4).setCellValue(student != null ? student.getYear() : "N/A");
            row.createCell(5).setCellValue(attendance.getStatus().equals("present") ? "✓" : "✗");
            row.createCell(6).setCellValue(attendance.getTime());
            row.createCell(7).setCellValue(attendance.getScanMethod().toUpperCase());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = "Attendance_" + session.getSessionName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".xlsx";
        
        // Save file
        Uri fileUri = saveWorkbookToDownloads(context, workbook, fileName);
        workbook.close();
        
        return fileUri;
    }
    
    /**
     * Create a header cell style
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }
    
    /**
     * Create a summary section style
     */
    private static CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }
    
    /**
     * Create an info row with label and value
     */
    private static void createInfoRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }
    
    /**
     * Save workbook to Downloads folder
     * Uses MediaStore API for Android 10+ and direct file access for older versions
     */
    private static Uri saveWorkbookToDownloads(Context context, Workbook workbook, String fileName) throws Exception {
        Uri fileUri;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CheckMate/Reports");
            
            fileUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            
            if (fileUri == null) {
                throw new Exception("Failed to create file in Downloads");
            }
            
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri)) {
                if (outputStream == null) {
                    throw new Exception("Cannot open output stream");
                }
                workbook.write(outputStream);
            }
        } else {
            // For older Android versions
            java.io.File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            java.io.File reportDir = new java.io.File(downloadDir, "CheckMate/Reports");
            
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            
            java.io.File file = new java.io.File(reportDir, fileName);
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                workbook.write(fos);
            }
            
            fileUri = Uri.fromFile(file);
        }
        
        Log.d(TAG, "File saved: " + fileUri);
        return fileUri;
    }
}
