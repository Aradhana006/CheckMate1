package com.example.project.attendance;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.AttendanceSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * View attendance history and past sessions
 */
public class AttendanceHistoryActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView tvNoData;
    
    private AttendanceDatabase database;
    private ExecutorService executorService;
    private List<AttendanceSession> sessions;
    private AttendanceHistoryAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);
        
        initializeDatabase();
        initializeViews();
        loadHistory();
    }
    
    private void initializeDatabase() {
        database = AttendanceDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewHistory);
        tvNoData = findViewById(R.id.tvNoData);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessions = new ArrayList<>();
    }
    
    private void loadHistory() {
        executorService.execute(() -> {
            sessions = database.attendanceSessionDao().getAllSessions();
            
            runOnUiThread(() -> {
                if (sessions.isEmpty()) {
                    tvNoData.setVisibility(android.view.View.VISIBLE);
                    recyclerView.setVisibility(android.view.View.GONE);
                } else {
                    tvNoData.setVisibility(android.view.View.GONE);
                    recyclerView.setVisibility(android.view.View.VISIBLE);
                    
                    adapter = new AttendanceHistoryAdapter(this, sessions, database);
                    recyclerView.setAdapter(adapter);
                }
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
