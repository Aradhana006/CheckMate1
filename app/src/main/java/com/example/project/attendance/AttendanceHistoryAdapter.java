package com.example.project.attendance;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.attendance.database.AttendanceDatabase;
import com.example.project.attendance.model.AttendanceSession;

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying attendance session history
 */
public class AttendanceHistoryAdapter extends RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder> {
    
    private Context context;
    private List<AttendanceSession> sessions;
    private AttendanceDatabase database;
    
    public AttendanceHistoryAdapter(Context context, List<AttendanceSession> sessions, AttendanceDatabase database) {
        this.context = context;
        this.sessions = sessions;
        this.database = database;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_attendance_session, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceSession session = sessions.get(position);
        
        holder.tvSessionName.setText(session.getSessionName());
        holder.tvDate.setText("Date: " + session.getDate());
        holder.tvTime.setText("Time: " + session.getStartTime() + 
                (session.getEndTime() != null ? " - " + session.getEndTime() : ""));
        
        holder.tvPresentCount.setText(String.format(Locale.US, 
                "%d / %d Present", session.getPresentCount(), session.getTotalStudents()));
        
        holder.tvPercentage.setText(String.format(Locale.US, 
                "%.1f%%", session.getAttendancePercentage()));
        
        // Color code based on percentage
        double percentage = session.getAttendancePercentage();
        int color;
        if (percentage < 50) {
            color = context.getResources().getColor(android.R.color.holo_red_dark);
        } else if (percentage < 75) {
            color = context.getResources().getColor(android.R.color.holo_orange_dark);
        } else {
            color = context.getResources().getColor(android.R.color.holo_green_dark);
        }
        holder.tvPercentage.setTextColor(color);
        
        // Click to view details (could open a detail activity)
        holder.cardView.setOnClickListener(v -> {
            // Future enhancement: open session details
        });
    }
    
    @Override
    public int getItemCount() {
        return sessions.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvSessionName, tvDate, tvTime, tvPresentCount, tvPercentage;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvSessionName = itemView.findViewById(R.id.tvSessionName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPresentCount = itemView.findViewById(R.id.tvPresentCount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
        }
    }
}
