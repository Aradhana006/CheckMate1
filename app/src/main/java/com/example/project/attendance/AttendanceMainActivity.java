package com.example.project.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project.R;

/**
 * Main dashboard for attendance system
 * Provides navigation to all attendance features
 */
public class AttendanceMainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_main);
        
        // Request necessary permissions
        checkAndRequestPermissions();
        
        // Initialize UI components
        initializeViews();
    }
    
    private void initializeViews() {
        CardView scanAttendanceCard = findViewById(R.id.cardScanAttendance);
        CardView ocrScanCard = findViewById(R.id.cardOCRScan);
        CardView manualEntryCard = findViewById(R.id.cardManualEntry);
        CardView importStudentsCard = findViewById(R.id.cardImportStudents);
        CardView exportReportCard = findViewById(R.id.cardExportReport);
        CardView historyCard = findViewById(R.id.cardHistory);
        
        scanAttendanceCard.setOnClickListener(v -> openActivity(ScanAttendanceActivity.class));
        ocrScanCard.setOnClickListener(v -> openActivity(OCRScanActivity.class));
        manualEntryCard.setOnClickListener(v -> openActivity(ManualEntryActivity.class));
        importStudentsCard.setOnClickListener(v -> openActivity(ImportStudentsActivity.class));
        exportReportCard.setOnClickListener(v -> openActivity(ExportReportActivity.class));
        historyCard.setOnClickListener(v -> openActivity(AttendanceHistoryActivity.class));
    }
    
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
    
    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Some permissions are required for full functionality", Toast.LENGTH_LONG).show();
            }
        }
    }
}
