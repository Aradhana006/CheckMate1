package com.example.project.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.AttendanceSession;
import com.example.project.attendance.model.Student;
import com.example.project.attendance.utils.AttendanceValidator;
import com.example.project.attendance.utils.QRCodeScanner;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced QR/Barcode scanning activity for attendance
 * Features: Multi-format scanning, live count, duplicate prevention, undo
 */
public class ScanAttendanceActivity extends AppCompatActivity {
    
    private TextView tvPresentCount, tvTotalCount, tvPercentage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button btnScan, btnUndo, btnFinish;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    
    private AttendanceSession currentSession;
    private List<Attendance> scannedAttendance;
    private ScannedStudentsAdapter adapter;
    
    private int totalStudents = 0;
    private int presentCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_attendance);
        
        initializeDatabase();
        initializeViews();
        loadOrCreateSession();
        setupRecyclerView();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPercentage = findViewById(R.id.tvPercentage);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerViewScanned);
        btnScan = findViewById(R.id.btnScan);
        btnUndo = findViewById(R.id.btnUndo);
        btnFinish = findViewById(R.id.btnFinish);
        
        btnScan.setOnClickListener(v -> startBarcodeScanner());
        btnUndo.setOnClickListener(v -> undoLastEntry());
        btnFinish.setOnClickListener(v -> finishSession());
    }
    
    private void loadOrCreateSession() {
        executorService.execute(() -> {
            // Get total student count
            totalStudents = database.studentDao().getStudentCount();
            
            // Create a new session
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            
            currentSession = new AttendanceSession("Attendance " + date, date, time);
            currentSession.setTotalStudents(totalStudents);
            
            long sessionId = database.attendanceSessionDao().insert(currentSession);
            currentSession.setId((int) sessionId);
            
            // Load existing attendance for this session
            scannedAttendance = new ArrayList<>(database.attendanceDao().getAttendanceBySession((int) sessionId));
            presentCount = scannedAttendance.size();
            
            runOnUiThread(() -> {
                updateUI();
                setupRecyclerView();
            });
        });
    }
    
    private void setupRecyclerView() {
        if (scannedAttendance == null) {
            scannedAttendance = new ArrayList<>();
        }
        
        adapter = new ScannedStudentsAdapter(this, scannedAttendance, database);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void startBarcodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan student ID barcode/QR code");
        integrator.setBeepEnabled(false); // We'll provide custom feedback
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(
                IntentIntegrator.QR_CODE,
                IntentIntegrator.CODE_128,
                IntentIntegrator.CODE_39,
                IntentIntegrator.EAN_13,
                IntentIntegrator.EAN_8,
                IntentIntegrator.UPC_A,
                IntentIntegrator.UPC_E
        );
        integrator.initiateScan();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedData = result.getContents();
                processScannedData(scannedData);
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    private void processScannedData(String scannedData) {
        String rollNumber = QRCodeScanner.parseRollNumber(scannedData);
        rollNumber = AttendanceValidator.sanitizeRollNumber(rollNumber);
        
        if (!AttendanceValidator.isValidRollNumber(rollNumber)) {
            Toast.makeText(this, "Invalid roll number format: " + rollNumber, Toast.LENGTH_SHORT).show();
            return;
        }
        
        executorService.execute(() -> {
            Student student = database.studentDao().getStudentByRollNumber(rollNumber);
            
            if (student == null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Student not found: " + rollNumber, Toast.LENGTH_LONG).show()
                );
                return;
            }
            
            // Check for duplicate
            Attendance existing = database.attendanceDao().getAttendanceBySessionAndStudent(
                    currentSession.getId(), student.getId()
            );
            
            if (existing != null) {
                runOnUiThread(() -> showDuplicateDialog(student, existing));
                return;
            }
            
            // Mark attendance
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            
            Attendance attendance = new Attendance(
                    student.getId(),
                    currentSession.getId(),
                    date,
                    time,
                    "present",
                    "qr"
            );
            
            long id = database.attendanceDao().insert(attendance);
            attendance.setId((int) id);
            
            scannedAttendance.add(0, attendance);
            presentCount++;
            
            // Update session
            currentSession.setPresentCount(presentCount);
            database.attendanceSessionDao().update(currentSession);
            
            runOnUiThread(() -> {
                QRCodeScanner.provideScanFeedback(this);
                Toast.makeText(this, "✓ " + student.getName() + " (" + rollNumber + ")", Toast.LENGTH_SHORT).show();
                updateUI();
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    private void showDuplicateDialog(Student student, Attendance existing) {
        new AlertDialog.Builder(this)
                .setTitle("Duplicate Entry")
                .setMessage(student.getName() + " (" + student.getRollNumber() + ")\n" +
                        "Already marked present at " + existing.getTime())
                .setPositiveButton("OK", null)
                .setNegativeButton("Override", (dialog, which) -> {
                    // Allow override if needed
                    executorService.execute(() -> {
                        database.attendanceDao().delete(existing);
                        scannedAttendance.remove(existing);
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            processScannedData(student.getRollNumber());
                        });
                    });
                })
                .show();
    }
    
    private void undoLastEntry() {
        if (scannedAttendance.isEmpty()) {
            Toast.makeText(this, "No entries to undo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Undo Last Entry")
                .setMessage("Remove the last scanned entry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    executorService.execute(() -> {
                        Attendance lastAttendance = scannedAttendance.remove(0);
                        database.attendanceDao().delete(lastAttendance);
                        presentCount--;
                        
                        currentSession.setPresentCount(presentCount);
                        database.attendanceSessionDao().update(currentSession);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Last entry removed", Toast.LENGTH_SHORT).show();
                            updateUI();
                            adapter.notifyDataSetChanged();
                        });
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    private void finishSession() {
        new AlertDialog.Builder(this)
                .setTitle("Finish Session")
                .setMessage("End this attendance session?\n\n" +
                        "Present: " + presentCount + "/" + totalStudents)
                .setPositiveButton("Finish", (dialog, which) -> {
                    executorService.execute(() -> {
                        String endTime = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
                        currentSession.setEndTime(endTime);
                        database.attendanceSessionDao().update(currentSession);
                        
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Session completed", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updateUI() {
        tvPresentCount.setText(String.valueOf(presentCount));
        tvTotalCount.setText(String.valueOf(totalStudents));
        
        double percentage = totalStudents > 0 ? (presentCount * 100.0 / totalStudents) : 0;
        tvPercentage.setText(String.format(Locale.US, "%.1f%%", percentage));
        
        progressBar.setMax(totalStudents);
        progressBar.setProgress(presentCount);
        
        // Color-coded progress
        if (percentage < 50) {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
        } else if (percentage < 75) {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
        } else {
            progressBar.setProgressTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
        }
        
        btnUndo.setEnabled(!scannedAttendance.isEmpty());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
