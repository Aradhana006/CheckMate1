package com.example.project.attendance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.Student;
import com.example.project.attendance.utils.ExcelImporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Import student list from Excel file
 * Supports .xlsx and .xls formats with preview
 */
public class ImportStudentsActivity extends AppCompatActivity {
    
    private static final int PICK_EXCEL_FILE = 1;
    
    private Button btnSelectFile, btnImport, btnClearDatabase;
    private TextView tvFileName, tvStatus, tvStudentCount;
    private RecyclerView recyclerViewPreview;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    
    private Uri selectedFileUri;
    private List<Student> previewedStudents;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_students);
        
        initializeDatabase();
        initializeViews();
        loadCurrentCount();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnImport = findViewById(R.id.btnImport);
        btnClearDatabase = findViewById(R.id.btnClearDatabase);
        tvFileName = findViewById(R.id.tvFileName);
        tvStatus = findViewById(R.id.tvStatus);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        recyclerViewPreview = findViewById(R.id.recyclerViewPreview);
        
        recyclerViewPreview.setLayoutManager(new LinearLayoutManager(this));
        
        btnSelectFile.setOnClickListener(v -> selectExcelFile());
        btnImport.setOnClickListener(v -> importStudents());
        btnClearDatabase.setOnClickListener(v -> confirmClearDatabase());
        
        btnImport.setEnabled(false);
    }
    
    private void loadCurrentCount() {
        executorService.execute(() -> {
            int count = database.studentDao().getStudentCount();
            runOnUiThread(() -> 
                tvStudentCount.setText("Current database: " + count + " students")
            );
        });
    }
    
    private void selectExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                tvFileName.setText("File: " + selectedFileUri.getLastPathSegment());
                previewExcelFile();
            }
        }
    }
    
    private void previewExcelFile() {
        tvStatus.setText("Loading preview...");
        btnImport.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                List<String[]> preview = ExcelImporter.previewExcel(this, selectedFileUri, 10);
                
                runOnUiThread(() -> {
                    if (preview.isEmpty()) {
                        tvStatus.setText("No data found in file");
                        return;
                    }
                    
                    // Show preview in dialog
                    showPreviewDialog(preview);
                    
                    tvStatus.setText("Preview loaded. Click 'Import' to proceed.");
                    btnImport.setEnabled(true);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Error: " + e.getMessage());
                    Toast.makeText(this, "Failed to read Excel file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void showPreviewDialog(List<String[]> preview) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_excel_preview, null);
        TextView tvPreviewContent = dialogView.findViewById(R.id.tvPreviewContent);
        
        StringBuilder sb = new StringBuilder();
        sb.append("First ").append(Math.min(10, preview.size())).append(" rows:\n\n");
        
        for (int i = 0; i < preview.size(); i++) {
            String[] row = preview.get(i);
            if (i == 0) {
                sb.append("HEADER:\n");
            } else {
                sb.append("Row ").append(i).append(":\n");
            }
            sb.append("Roll: ").append(row[0]).append("\n");
            sb.append("Name: ").append(row[1]).append("\n");
            sb.append("Dept: ").append(row[2]).append("\n");
            sb.append("Year: ").append(row[3]).append("\n");
            sb.append("Email: ").append(row[4]).append("\n\n");
        }
        
        tvPreviewContent.setText(sb.toString());
        
        new AlertDialog.Builder(this)
                .setTitle("Excel Preview")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }
    
    private void importStudents() {
        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Import Students")
                .setMessage("This will import students from the selected Excel file.\n\nDuplicate roll numbers will be updated.")
                .setPositiveButton("Import", (dialog, which) -> performImport())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performImport() {
        tvStatus.setText("Importing...");
        btnImport.setEnabled(false);
        btnSelectFile.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                List<Student> students = ExcelImporter.importStudents(this, selectedFileUri);
                
                if (students.isEmpty()) {
                    runOnUiThread(() -> {
                        tvStatus.setText("No students found in file");
                        btnImport.setEnabled(true);
                        btnSelectFile.setEnabled(true);
                        Toast.makeText(this, "No students to import", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Insert into database
                database.studentDao().insertAll(students);
                
                int totalCount = database.studentDao().getStudentCount();
                
                runOnUiThread(() -> {
                    tvStatus.setText("Import completed successfully!");
                    tvStudentCount.setText("Current database: " + totalCount + " students");
                    btnImport.setEnabled(true);
                    btnSelectFile.setEnabled(true);
                    
                    new AlertDialog.Builder(this)
                            .setTitle("Import Successful")
                            .setMessage("Imported " + students.size() + " students.\n\n" +
                                    "Total in database: " + totalCount)
                            .setPositiveButton("OK", null)
                            .show();
                    
                    // Clear selection
                    selectedFileUri = null;
                    tvFileName.setText("No file selected");
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Import failed: " + e.getMessage());
                    btnImport.setEnabled(true);
                    btnSelectFile.setEnabled(true);
                    
                    new AlertDialog.Builder(this)
                            .setTitle("Import Failed")
                            .setMessage("Error: " + e.getMessage() + "\n\nPlease check:\n" +
                                    "• File format is .xlsx or .xls\n" +
                                    "• First row contains headers\n" +
                                    "• Columns: Roll Number, Name, Department, Year, Email")
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }
    
    private void confirmClearDatabase() {
        executorService.execute(() -> {
            int count = database.studentDao().getStudentCount();
            
            runOnUiThread(() -> {
                if (count == 0) {
                    Toast.makeText(this, "Database is already empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                new AlertDialog.Builder(this)
                        .setTitle("Clear Database")
                        .setMessage("Delete all " + count + " students from database?\n\nThis action cannot be undone!")
                        .setPositiveButton("Delete All", (dialog, which) -> clearDatabase())
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }
    
    private void clearDatabase() {
        executorService.execute(() -> {
            database.studentDao().deleteAll();
            
            runOnUiThread(() -> {
                tvStudentCount.setText("Current database: 0 students");
                tvStatus.setText("Database cleared");
                Toast.makeText(this, "All students deleted", Toast.LENGTH_SHORT).show();
            });
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
