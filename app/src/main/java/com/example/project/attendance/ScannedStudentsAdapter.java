package com.example.project.attendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.Attendance;
import com.example.project.attendance.model.Student;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter for displaying scanned students in RecyclerView
 */
public class ScannedStudentsAdapter extends RecyclerView.Adapter<ScannedStudentsAdapter.ViewHolder> {
    
    private Context context;
    private List<Attendance> attendanceList;
    private AttendanceDatabase database;
    private ExecutorService executorService;
    
    public ScannedStudentsAdapter(Context context, List<Attendance> attendanceList, AttendanceDatabase database) {
        this.context = context;
        this.attendanceList = attendanceList;
        this.database = database;
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scanned_student, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        
        executorService.execute(() -> {
            Student student = database.studentDao().getStudentById(attendance.getStudentId());
            
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (student != null) {
                    holder.tvRollNumber.setText(student.getRollNumber());
                    holder.tvName.setText(student.getName());
                    holder.tvDepartment.setText(student.getDepartment() + " - " + student.getYear());
                } else {
                    holder.tvRollNumber.setText("Unknown");
                    holder.tvName.setText("Student not found");
                    holder.tvDepartment.setText("");
                }
                
                holder.tvTime.setText(attendance.getTime());
                holder.tvMethod.setText(attendance.getScanMethod().toUpperCase());
            });
        });
    }
    
    @Override
    public int getItemCount() {
        return attendanceList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRollNumber, tvName, tvDepartment, tvTime, tvMethod;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRollNumber = itemView.findViewById(R.id.tvRollNumber);
            tvName = itemView.findViewById(R.id.tvName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMethod = itemView.findViewById(R.id.tvMethod);
        }
    }
}
