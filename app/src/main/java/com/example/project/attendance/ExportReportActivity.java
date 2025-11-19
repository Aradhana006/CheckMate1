package com.example.project.attendance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.AttendanceSession;
import com.example.project.attendance.model.Student;
import com.example.project.attendance.utils.ExcelExporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Export attendance reports to Excel
 * Supports multiple sessions and date ranges
 */
public class ExportReportActivity extends AppCompatActivity {
    
    private Spinner spinnerSessions;
    private Button btnExport, btnShare;
    private TextView tvStatus;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    
    private List<AttendanceSession> sessions;
    private Uri lastExportedUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_report);
        
        initializeDatabase();
        initializeViews();
        loadSessions();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        spinnerSessions = findViewById(R.id.spinnerSessions);
        btnExport = findViewById(R.id.btnExport);
        btnShare = findViewById(R.id.btnShare);
        tvStatus = findViewById(R.id.tvStatus);
        
        btnExport.setOnClickListener(v -> exportSelectedSession());
        btnShare.setOnClickListener(v -> shareLastExport());
        
        btnShare.setEnabled(false);
    }
    
    private void loadSessions() {
        executorService.execute(() -> {
            sessions = database.attendanceSessionDao().getAllSessions();
            
            runOnUiThread(() -> {
                if (sessions.isEmpty()) {
                    tvStatus.setText("No attendance sessions found. Please mark attendance first.");
                    btnExport.setEnabled(false);
                    return;
                }
                
                List<String> sessionNames = new ArrayList<>();
                for (AttendanceSession session : sessions) {
                    String name = session.getSessionName() + " (" + 
                            session.getPresentCount() + "/" + session.getTotalStudents() + ")";
                    sessionNames.add(name);
                }
                
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sessionNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSessions.setAdapter(adapter);
                
                tvStatus.setText("Select a session to export");
            });
        });
    }
    
    private void exportSelectedSession() {
        int position = spinnerSessions.getSelectedItemPosition();
        if (position < 0 || position >= sessions.size()) {
            Toast.makeText(this, "Please select a session", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AttendanceSession session = sessions.get(position);
        tvStatus.setText("Exporting...");
        btnExport.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                // Get attendance records for session
                List<Attendance> attendanceList = database.attendanceDao().getAttendanceBySession(session.getId());
                
                // Get student data
                Map<Integer, Student> studentMap = new HashMap<>();
                for (Attendance attendance : attendanceList) {
                    Student student = database.studentDao().getStudentById(attendance.getStudentId());
                    if (student != null) {
                        studentMap.put(student.getId(), student);
                    }
                }
                
                // Export to Excel
                Uri fileUri = ExcelExporter.exportAttendanceReport(
                        this,
                        session,
                        attendanceList,
                        studentMap
                );
                
                lastExportedUri = fileUri;
                
                runOnUiThread(() -> {
                    tvStatus.setText("Export successful!\n" + fileUri.getLastPathSegment());
                    btnExport.setEnabled(true);
                    btnShare.setEnabled(true);
                    
                    Toast.makeText(this, "Report saved to Downloads", Toast.LENGTH_LONG).show();
                    
                    // Offer to open file
                    showOpenFileDialog(fileUri);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Export failed: " + e.getMessage());
                    btnExport.setEnabled(true);
                    Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showOpenFileDialog(Uri fileUri) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Export Complete")
                .setMessage("Attendance report saved successfully.\n\nWould you like to open it?")
                .setPositiveButton("Open", (dialog, which) -> openFile(fileUri))
                .setNegativeButton("Later", null)
                .setNeutralButton("Share", (dialog, which) -> shareFile(fileUri))
                .show();
    }
    
    private void openFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app available to open Excel files", Toast.LENGTH_LONG).show();
        }
    }
    
    private void shareLastExport() {
        if (lastExportedUri == null) {
            Toast.makeText(this, "No file to share", Toast.LENGTH_SHORT).show();
            return;
        }
        shareFile(lastExportedUri);
    }
    
    private void shareFile(Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        try {
            startActivity(Intent.createChooser(shareIntent, "Share Attendance Report"));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to share file", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
