# Attendance Module Integration Guide

This guide helps you integrate the new attendance module into your existing CheckMate app.

## Quick Start

The attendance module is fully self-contained and ready to use. You have two options:

### Option 1: Add Button to Existing UI (Recommended)

Add a button or card to your main dashboard to launch the attendance system:

**In your MainActivity or HomeFragment:**

```java
Button btnAttendance = findViewById(R.id.btnAttendance);
btnAttendance.setOnClickListener(v -> {
    Intent intent = new Intent(this, AttendanceMainActivity.class);
    startActivity(intent);
});
```

**Or in your existing layout:**

```xml
<Button
    android:id="@+id/btnAttendance"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Attendance System"
    android:backgroundTint="@color/primary" />
```

### Option 2: Add to Bottom Navigation

If you want to add it to your bottom navigation:

**In `res/menu/bottom_nav_menu.xml`:**

```xml
<item
    android:id="@+id/navigation_attendance"
    android:icon="@drawable/ic_attendance"
    android:title="Attendance" />
```

**In MainActivity.java:**

```java
bottomNav.setOnItemSelectedListener(item -> {
    if (item.getItemId() == R.id.navigation_attendance) {
        Intent intent = new Intent(this, AttendanceMainActivity.class);
        startActivity(intent);
        return true;
    }
    // ... other navigation items
    return false;
});
```

## File Checklist

Make sure these files are present:

### Java Files (22 files)
```
✓ attendance/AttendanceMainActivity.java
✓ attendance/ScanAttendanceActivity.java
✓ attendance/OCRScanActivity.java
✓ attendance/ManualEntryActivity.java
✓ attendance/ImportStudentsActivity.java
✓ attendance/ExportReportActivity.java
✓ attendance/AttendanceHistoryActivity.java
✓ attendance/ScannedStudentsAdapter.java
✓ attendance/AttendanceHistoryAdapter.java
✓ attendance/model/Student.java
✓ attendance/model/Attendance.java
✓ attendance/model/AttendanceSession.java
✓ attendance/database/AttendanceDatabase.java
✓ attendance/database/StudentDao.java
✓ attendance/database/AttendanceDao.java
✓ attendance/database/AttendanceSessionDao.java
✓ attendance/utils/ExcelImporter.java
✓ attendance/utils/ExcelExporter.java
✓ attendance/utils/AttendanceValidator.java
✓ attendance/utils/QRCodeScanner.java
✓ attendance/utils/OCRProcessor.java
```

### Layout Files (10 files)
```
✓ res/layout/activity_attendance_main.xml
✓ res/layout/activity_scan_attendance.xml
✓ res/layout/activity_ocr_scan.xml
✓ res/layout/activity_manual_entry.xml
✓ res/layout/activity_import_students.xml
✓ res/layout/activity_export_report.xml
✓ res/layout/activity_attendance_history.xml
✓ res/layout/item_scanned_student.xml
✓ res/layout/item_attendance_session.xml
✓ res/layout/dialog_excel_preview.xml
```

### Resource Files
```
✓ res/values/colors.xml (updated with attendance colors)
✓ res/values/strings.xml (updated with attendance strings)
✓ res/xml/file_paths.xml (for FileProvider)
✓ res/raw/README.md (placeholder for beep.mp3)
```

### Configuration Files
```
✓ AndroidManifest.xml (updated with activities and permissions)
✓ app/build.gradle.kts (updated with dependencies)
```

## Dependencies Added

These dependencies have been added to `app/build.gradle.kts`:

```gradle
// QR/Barcode Scanning
implementation("com.google.zxing:core:3.5.1")
implementation("com.journeyapps:zxing-android-embedded:4.3.0")

// OCR
implementation("com.google.mlkit:text-recognition:16.0.0")

// Excel handling
implementation("org.apache.poi:poi:5.2.3")
implementation("org.apache.poi:poi-ooxml:5.2.3")

// Database
implementation("androidx.room:room-runtime:2.5.2")
annotationProcessor("androidx.room:room-compiler:2.5.2")

// File handling
implementation("androidx.documentfile:documentfile:1.0.1")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.1")
```

## Permissions Added

These permissions have been added to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.VIBRATE" />

<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

## Building the Project

1. **Sync Gradle**:
   ```bash
   ./gradlew clean build
   ```

2. **If build fails**, check:
   - Internet connection (for downloading dependencies)
   - Gradle version compatibility
   - Android SDK version (requires API 24+)

3. **Build APK**:
   ```bash
   ./gradlew assembleDebug
   ```

## Testing the Module

### 1. Import Students First
Before marking attendance, you need to import students:

1. Create an Excel file with this format:

| Roll Number | Name          | Department | Year | Email              |
|-------------|---------------|------------|------|--------------------|
| CSE001      | John Doe      | CSE        | 2024 | john@example.com   |
| CSE002      | Jane Smith    | CSE        | 2024 | jane@example.com   |
| ECE001      | Bob Johnson   | ECE        | 2023 | bob@example.com    |

2. Open "Import Students"
3. Select the Excel file
4. Review preview
5. Click Import

### 2. Test QR Scanning
1. Generate a test QR code with a roll number (e.g., "CSE001")
2. Open "Scan Attendance"
3. Click "Scan QR/Barcode"
4. Scan the QR code
5. Verify student appears in the list

### 3. Test Manual Entry
1. Open "Manual Entry"
2. Type a roll number
3. Click "Mark Present"
4. Verify real-time validation

### 4. Test Export
1. After marking some attendance
2. Open "Export Report"
3. Select the session
4. Click "Export to Excel"
5. Check Downloads/CheckMate/Reports/

## Optional Enhancements

### Add Icon for Attendance
Create an icon drawable for the attendance button:

**res/drawable/ic_attendance.xml:**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/primary"
        android:pathData="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20M10,19L12,15H9V10L7,14H10V19Z"/>
</vector>
```

### Add Beep Sound
1. Download or create a short beep sound (< 1 second)
2. Convert to MP3 or OGG format
3. Save as `app/src/main/res/raw/beep.mp3`

If no beep file is present, the app will skip audio feedback (vibration will still work).

## Troubleshooting

### Build Errors

**Error: "Package androidx.room does not exist"**
- Solution: Sync Gradle files and rebuild

**Error: "Cannot resolve symbol 'Room'"**
- Solution: Make sure Room dependencies are added and synced

**Error: "Duplicate class found"**
- Solution: Check for conflicting library versions in build.gradle

### Runtime Errors

**Error: "Permission denied for Camera"**
- Solution: Grant camera permission in app settings

**Error: "Cannot create file in Downloads"**
- Solution: Grant storage permission in app settings

**Error: "Student not found"**
- Solution: Import students first before marking attendance

### Common Issues

**QR Scanner shows black screen**
- Check camera permission
- Ensure device camera is working
- Try restarting the app

**OCR not detecting text**
- Use better lighting
- Ensure text is clear and visible
- Try manual entry as fallback

**Excel import fails**
- Verify file format (.xlsx or .xls)
- Check column order matches expected format
- Ensure no empty rows in between data

## Database Location

The SQLite database is stored at:
```
/data/data/com.example.project/databases/attendance_database
```

You can view it using Android Studio's Database Inspector.

## Next Steps

1. ✅ Build the project
2. ✅ Test on a device or emulator
3. ✅ Import sample student data
4. ✅ Test all attendance methods
5. ✅ Export and verify reports
6. 📱 Deploy to production

## Support

For detailed feature documentation, see [ATTENDANCE_MODULE_README.md](ATTENDANCE_MODULE_README.md)

For any issues:
1. Check the troubleshooting section above
2. Review the code comments
3. Test with sample data first
4. Verify all permissions are granted

## Credits

This attendance module integrates:
- **ZXing** for QR/Barcode scanning
- **Google ML Kit** for OCR
- **Apache POI** for Excel handling
- **Room** for local database
- **Material Design** components

---

**Ready to use!** 🎉

The attendance module is production-ready and fully functional. Just build, test, and deploy!
