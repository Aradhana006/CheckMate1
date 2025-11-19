package com.example.project.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.AttendanceSession;
import com.example.project.attendance.model.Student;
import com.example.project.attendance.utils.AttendanceValidator;
import com.example.project.attendance.utils.OCRProcessor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OCR-based attendance scanning activity
 * Uses ML Kit to extract roll numbers from ID card images
 */
public class OCRScanActivity extends AppCompatActivity {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST = 100;
    
    private ImageView ivPreview;
    private TextView tvExtractedText, tvStatus;
    private Button btnSelectImage, btnCapture, btnProcess;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    private Uri selectedImageUri;
    private AttendanceSession currentSession;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_scan);
        
        initializeDatabase();
        initializeViews();
        checkPermissions();
        loadOrCreateSession();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        ivPreview = findViewById(R.id.ivPreview);
        tvExtractedText = findViewById(R.id.tvExtractedText);
        tvStatus = findViewById(R.id.tvStatus);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnCapture = findViewById(R.id.btnCapture);
        btnProcess = findViewById(R.id.btnProcess);
        
        btnSelectImage.setOnClickListener(v -> selectImageFromGallery());
        btnCapture.setOnClickListener(v -> captureImageFromCamera());
        btnProcess.setOnClickListener(v -> processImage());
        
        btnProcess.setEnabled(false);
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    PERMISSION_REQUEST);
        }
    }
    
    private void loadOrCreateSession() {
        executorService.execute(() -> {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            
            currentSession = new AttendanceSession("OCR Attendance " + date, date, time);
            currentSession.setTotalStudents(database.studentDao().getStudentCount());
            
            long sessionId = database.attendanceSessionDao().insert(currentSession);
            currentSession.setId((int) sessionId);
        });
    }
    
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void captureImageFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                displayImage(selectedImageUri);
            } else if (requestCode == CAMERA_REQUEST) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        ivPreview.setImageBitmap(imageBitmap);
                        btnProcess.setEnabled(true);
                        
                        // Process bitmap directly
                        processBitmap(imageBitmap);
                    }
                }
            }
        }
    }
    
    private void displayImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ivPreview.setImageBitmap(bitmap);
            btnProcess.setEnabled(true);
            tvStatus.setText("Image loaded. Click 'Process Image' to extract roll numbers.");
        } catch (IOException e) {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void processImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        tvStatus.setText("Processing image...");
        btnProcess.setEnabled(false);
        
        OCRProcessor.extractRollNumbersFromImage(this, selectedImageUri, new OCRProcessor.OCRCallback() {
            @Override
            public void onSuccess(List<String> extractedRollNumbers) {
                runOnUiThread(() -> {
                    if (extractedRollNumbers.isEmpty()) {
                        tvExtractedText.setText("No roll numbers detected.\n\nPlease try:\n• Better lighting\n• Clearer image\n• Manual entry");
                        tvStatus.setText("No roll numbers found");
                        btnProcess.setEnabled(true);
                    } else {
                        displayExtractedRollNumbers(extractedRollNumbers);
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    tvExtractedText.setText("OCR failed: " + e.getMessage());
                    tvStatus.setText("Processing failed");
                    btnProcess.setEnabled(true);
                    Toast.makeText(OCRScanActivity.this, "OCR failed. Try manual entry.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void processBitmap(Bitmap bitmap) {
        tvStatus.setText("Processing image...");
        btnProcess.setEnabled(false);
        
        OCRProcessor.extractRollNumbersFromBitmap(bitmap, new OCRProcessor.OCRCallback() {
            @Override
            public void onSuccess(List<String> extractedRollNumbers) {
                runOnUiThread(() -> {
                    if (extractedRollNumbers.isEmpty()) {
                        tvExtractedText.setText("No roll numbers detected");
                        tvStatus.setText("No roll numbers found");
                        btnProcess.setEnabled(true);
                    } else {
                        displayExtractedRollNumbers(extractedRollNumbers);
                    }
                });
            }
            
            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    tvExtractedText.setText("OCR failed: " + e.getMessage());
                    tvStatus.setText("Processing failed");
                    btnProcess.setEnabled(true);
                });
            }
        });
    }
    
    private void displayExtractedRollNumbers(List<String> rollNumbers) {
        StringBuilder sb = new StringBuilder("Detected Roll Numbers:\n\n");
        for (int i = 0; i < rollNumbers.size(); i++) {
            sb.append((i + 1)).append(". ").append(rollNumbers.get(i)).append("\n");
        }
        tvExtractedText.setText(sb.toString());
        tvStatus.setText(rollNumbers.size() + " roll number(s) detected");
        
        // Show confirmation dialog
        showConfirmationDialog(rollNumbers);
    }
    
    private void showConfirmationDialog(List<String> rollNumbers) {
        String[] rollNumberArray = rollNumbers.toArray(new String[0]);
        boolean[] selectedItems = new boolean[rollNumbers.size()];
        for (int i = 0; i < selectedItems.length; i++) {
            selectedItems[i] = true;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Mark Attendance")
                .setMultiChoiceItems(rollNumberArray, selectedItems, (dialog, which, isChecked) -> {
                    selectedItems[which] = isChecked;
                })
                .setPositiveButton("Mark Present", (dialog, which) -> {
                    for (int i = 0; i < rollNumbers.size(); i++) {
                        if (selectedItems[i]) {
                            markAttendance(rollNumbers.get(i));
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void markAttendance(String rollNumber) {
        String sanitizedRollNumber = AttendanceValidator.sanitizeRollNumber(rollNumber);
        
        executorService.execute(() -> {
            Student student = database.studentDao().getStudentByRollNumber(sanitizedRollNumber);
            
            if (student == null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, "Student not found: " + rollNumber, Toast.LENGTH_SHORT).show()
                );
                return;
            }
            
            // Check for duplicate
            Attendance existing = database.attendanceDao().getAttendanceBySessionAndStudent(
                    currentSession.getId(), student.getId()
            );
            
            if (existing != null) {
                runOnUiThread(() -> 
                    Toast.makeText(this, student.getName() + " already marked", Toast.LENGTH_SHORT).show()
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
                    "ocr"
            );
            
            database.attendanceDao().insert(attendance);
            
            // Update session count
            int presentCount = database.attendanceDao().getPresentCountBySession(currentSession.getId());
            currentSession.setPresentCount(presentCount);
            database.attendanceSessionDao().update(currentSession);
            
            runOnUiThread(() -> 
                Toast.makeText(this, "✓ " + student.getName(), Toast.LENGTH_SHORT).show()
            );
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
