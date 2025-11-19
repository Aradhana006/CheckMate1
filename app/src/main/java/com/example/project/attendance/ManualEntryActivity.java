package com.example.project.attendance;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manual roll number entry activity with autocomplete
 * Supports single and batch entry
 */
public class ManualEntryActivity extends AppCompatActivity {
    
    private AutoCompleteTextView autoCompleteRollNumber;
    private TextInputEditText etBatchEntry;
    private Button btnMarkPresent, btnBatchSubmit, btnClear;
    private TextView tvPresentCount, tvStatus;
    private RecyclerView recyclerView;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    
    private AttendanceSession currentSession;
    private List<Attendance> markedAttendance;
    private ScannedStudentsAdapter adapter;
    private List<String> allRollNumbers;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_entry);
        
        initializeDatabase();
        initializeViews();
        loadData();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        autoCompleteRollNumber = findViewById(R.id.autoCompleteRollNumber);
        etBatchEntry = findViewById(R.id.etBatchEntry);
        btnMarkPresent = findViewById(R.id.btnMarkPresent);
        btnBatchSubmit = findViewById(R.id.btnBatchSubmit);
        btnClear = findViewById(R.id.btnClear);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerView = findViewById(R.id.recyclerViewMarked);
        
        markedAttendance = new ArrayList<>();
        
        btnMarkPresent.setOnClickListener(v -> markSinglePresent());
        btnBatchSubmit.setOnClickListener(v -> processBatchEntry());
        btnClear.setOnClickListener(v -> clearInputs());
        
        // Real-time validation for single entry
        autoCompleteRollNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateRollNumber(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadData() {
        executorService.execute(() -> {
            // Load all roll numbers for autocomplete
            allRollNumbers = database.studentDao().getAllRollNumbers();
            
            // Create or load session
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            
            currentSession = new AttendanceSession("Manual Entry " + date, date, time);
            currentSession.setTotalStudents(database.studentDao().getStudentCount());
            
            long sessionId = database.attendanceSessionDao().insert(currentSession);
            currentSession.setId((int) sessionId);
            
            // Load existing attendance
            markedAttendance = new ArrayList<>(database.attendanceDao().getAttendanceBySession((int) sessionId));
            
            runOnUiThread(() -> {
                setupAutocomplete();
                setupRecyclerView();
                updateUI();
            });
        });
    }
    
    private void setupAutocomplete() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                allRollNumbers
        );
        autoCompleteRollNumber.setAdapter(adapter);
        autoCompleteRollNumber.setThreshold(1);
    }
    
    private void setupRecyclerView() {
        adapter = new ScannedStudentsAdapter(this, markedAttendance, database);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void validateRollNumber(String rollNumber) {
        if (rollNumber.isEmpty()) {
            tvStatus.setText("");
            return;
        }
        
        String sanitized = AttendanceValidator.sanitizeRollNumber(rollNumber);
        
        if (AttendanceValidator.isValidRollNumber(sanitized)) {
            tvStatus.setText("✓ Valid format");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("✗ Invalid format");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
    
    private void markSinglePresent() {
        String rollNumber = autoCompleteRollNumber.getText().toString().trim();
        
        if (rollNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a roll number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String sanitized = AttendanceValidator.sanitizeRollNumber(rollNumber);
        
        if (!AttendanceValidator.isValidRollNumber(sanitized)) {
            Toast.makeText(this, "Invalid roll number format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executorService.execute(() -> {
            Student student = database.studentDao().getStudentByRollNumber(sanitized);
            
            if (student == null) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Student Not Found")
                            .setMessage("Roll number '" + sanitized + "' not found in database.\n\nPlease import student list first.")
                            .setPositiveButton("OK", null)
                            .show();
                });
                return;
            }
            
            // Check for duplicate
            Attendance existing = database.attendanceDao().getAttendanceBySessionAndStudent(
                    currentSession.getId(), student.getId()
            );
            
            if (existing != null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Already marked: " + student.getName(), Toast.LENGTH_LONG).show()
                );
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
                    "manual"
            );
            
            long id = database.attendanceDao().insert(attendance);
            attendance.setId((int) id);
            
            markedAttendance.add(0, attendance);
            
            // Update session
            currentSession.setPresentCount(markedAttendance.size());
            database.attendanceSessionDao().update(currentSession);
            
            runOnUiThread(() -> {
                Toast.makeText(this, "✓ " + student.getName() + " marked present", Toast.LENGTH_SHORT).show();
                autoCompleteRollNumber.setText("");
                tvStatus.setText("");
                updateUI();
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    private void processBatchEntry() {
        String batchText = etBatchEntry.getText().toString().trim();
        
        if (batchText.isEmpty()) {
            Toast.makeText(this, "Please enter roll numbers", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Split by comma, newline, or space
        String[] rollNumbers = batchText.split("[,\\n\\s]+");
        
        List<String> validRollNumbers = new ArrayList<>();
        List<String> invalidRollNumbers = new ArrayList<>();
        
        for (String rollNumber : rollNumbers) {
            String sanitized = AttendanceValidator.sanitizeRollNumber(rollNumber);
            if (AttendanceValidator.isValidRollNumber(sanitized)) {
                validRollNumbers.add(sanitized);
            } else if (!sanitized.isEmpty()) {
                invalidRollNumbers.add(rollNumber);
            }
        }
        
        if (validRollNumbers.isEmpty()) {
            Toast.makeText(this, "No valid roll numbers found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show confirmation dialog
        String message = "Mark " + validRollNumbers.size() + " students present?";
        if (!invalidRollNumbers.isEmpty()) {
            message += "\n\n" + invalidRollNumbers.size() + " invalid entries will be skipped.";
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Batch Entry")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    processBatchRollNumbers(validRollNumbers);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void processBatchRollNumbers(List<String> rollNumbers) {
        executorService.execute(() -> {
            int marked = 0;
            int duplicates = 0;
            int notFound = 0;
            
            for (String rollNumber : rollNumbers) {
                Student student = database.studentDao().getStudentByRollNumber(rollNumber);
                
                if (student == null) {
                    notFound++;
                    continue;
                }
                
                Attendance existing = database.attendanceDao().getAttendanceBySessionAndStudent(
                        currentSession.getId(), student.getId()
                );
                
                if (existing != null) {
                    duplicates++;
                    continue;
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
                        "manual"
                );
                
                long id = database.attendanceDao().insert(attendance);
                attendance.setId((int) id);
                markedAttendance.add(0, attendance);
                marked++;
            }
            
            // Update session
            currentSession.setPresentCount(markedAttendance.size());
            database.attendanceSessionDao().update(currentSession);
            
            int finalMarked = marked;
            int finalDuplicates = duplicates;
            int finalNotFound = notFound;
            
            runOnUiThread(() -> {
                String result = "Batch Entry Complete\n\n" +
                        "Marked: " + finalMarked + "\n" +
                        "Duplicates: " + finalDuplicates + "\n" +
                        "Not Found: " + finalNotFound;
                
                new AlertDialog.Builder(this)
                        .setTitle("Results")
                        .setMessage(result)
                        .setPositiveButton("OK", null)
                        .show();
                
                etBatchEntry.setText("");
                updateUI();
                adapter.notifyDataSetChanged();
            });
        });
    }
    
    private void clearInputs() {
        autoCompleteRollNumber.setText("");
        etBatchEntry.setText("");
        tvStatus.setText("");
    }
    
    private void updateUI() {
        tvPresentCount.setText("Present: " + markedAttendance.size() + "/" + currentSession.getTotalStudents());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
