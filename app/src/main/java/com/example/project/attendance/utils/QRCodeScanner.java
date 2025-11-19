package com.example.project.attendance.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.example.project.R;

/**
 * Utility class for QR/Barcode scanning feedback
 * Provides audio and vibration feedback on successful scans
 */
public class QRCodeScanner {
    
    /**
     * Provide haptic and audio feedback for successful scan
     * 
     * @param context Application context
     */
    public static void provideScanFeedback(Context context) {
        // Vibration feedback
        provideVibrationFeedback(context);
        
        // Audio feedback
        provideAudioFeedback(context);
    }
    
    /**
     * Provide vibration feedback
     */
    private static void provideVibrationFeedback(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }
    
    /**
     * Provide audio feedback (beep sound)
     */
    private static void provideAudioFeedback(Context context) {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.beep);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Ignore if beep sound is not available
        }
    }
    
    /**
     * Parse scanned data to extract roll number
     * Supports different QR code formats
     * 
     * @param scannedData Raw scanned data
     * @return Extracted roll number
     */
    public static String parseRollNumber(String scannedData) {
        if (scannedData == null || scannedData.isEmpty()) {
            return "";
        }
        
        // Try to parse JSON format: {"rollNumber": "ABC123"}
        if (scannedData.trim().startsWith("{")) {
            try {
                // Simple JSON parsing for roll number
                String[] parts = scannedData.split(":");
                if (parts.length > 1) {
                    String rollPart = parts[1].trim();
                    return rollPart.replaceAll("[^A-Za-z0-9]", "");
                }
            } catch (Exception e) {
                // Fall through to other parsing methods
            }
        }
        
        // Try to parse key-value format: ROLL:ABC123 or RollNumber=ABC123
        if (scannedData.contains(":") || scannedData.contains("=")) {
            String[] parts = scannedData.split("[:|=]");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        
        // If no special format, assume the entire scanned data is the roll number
        return scannedData.trim();
    }
}
