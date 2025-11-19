package com.example.project.attendance.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for OCR processing using ML Kit
 * Extracts text from images and identifies roll numbers
 */
public class OCRProcessor {
    
    private static final String TAG = "OCRProcessor";
    
    // Pattern to match common roll number formats
    private static final Pattern ROLL_NUMBER_PATTERN = Pattern.compile("\\b[A-Z]{2,4}[0-9]{2,8}\\b");
    
    public interface OCRCallback {
        void onSuccess(List<String> extractedRollNumbers);
        void onFailure(Exception e);
    }
    
    /**
     * Process image and extract roll numbers using OCR
     * 
     * @param context Application context
     * @param imageUri URI of the image to process
     * @param callback Callback for results
     */
    public static void extractRollNumbersFromImage(Context context, Uri imageUri, OCRCallback callback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            processImage(image, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            callback.onFailure(e);
        }
    }
    
    /**
     * Process bitmap and extract roll numbers using OCR
     * 
     * @param bitmap Bitmap to process
     * @param callback Callback for results
     */
    public static void extractRollNumbersFromBitmap(Bitmap bitmap, OCRCallback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        processImage(image, callback);
    }
    
    /**
     * Process image using ML Kit Text Recognition
     */
    private static void processImage(InputImage image, OCRCallback callback) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        List<String> rollNumbers = extractRollNumbers(visionText.getText());
                        callback.onSuccess(rollNumbers);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OCR failed: " + e.getMessage());
                        callback.onFailure(e);
                    }
                });
    }
    
    /**
     * Extract roll numbers from recognized text
     * 
     * @param text Recognized text
     * @return List of potential roll numbers
     */
    private static List<String> extractRollNumbers(String text) {
        List<String> rollNumbers = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return rollNumbers;
        }
        
        // Remove common OCR noise characters
        String cleanedText = text.replaceAll("[^A-Za-z0-9\\s]", "").toUpperCase();
        
        // Find all matches of roll number pattern
        Matcher matcher = ROLL_NUMBER_PATTERN.matcher(cleanedText);
        while (matcher.find()) {
            String rollNumber = matcher.group();
            if (AttendanceValidator.isValidRollNumber(rollNumber)) {
                rollNumbers.add(rollNumber);
            }
        }
        
        // If no pattern matches, try to extract alphanumeric sequences
        if (rollNumbers.isEmpty()) {
            String[] words = cleanedText.split("\\s+");
            for (String word : words) {
                if (word.length() >= 5 && word.length() <= 20) {
                    // Check if it has both letters and numbers
                    if (word.matches(".*[A-Z].*") && word.matches(".*[0-9].*")) {
                        if (AttendanceValidator.isValidRollNumber(word)) {
                            rollNumbers.add(word);
                        }
                    }
                }
            }
        }
        
        return rollNumbers;
    }
    
    /**
     * Extract all text from image (for debugging/preview)
     * 
     * @param context Application context
     * @param imageUri URI of the image
     * @param callback Callback for results
     */
    public static void extractAllText(Context context, Uri imageUri, OnSuccessListener<String> callback, OnFailureListener errorCallback) {
        try {
            InputImage image = InputImage.fromFilePath(context, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> callback.onSuccess(visionText.getText()))
                    .addOnFailureListener(errorCallback);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            errorCallback.onFailure(e);
        }
    }
}
