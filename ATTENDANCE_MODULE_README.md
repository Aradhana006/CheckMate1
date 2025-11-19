# Attendance Module Documentation

## Overview
This is a comprehensive attendance management system for Android, integrated into the CheckMate event management app. The module provides multiple methods for marking attendance and comprehensive reporting capabilities.

## Features

### 1. QR Code/Barcode Scanning
- **Multi-format support**: QR Code, Code 128, Code 39, EAN-13, EAN-8, UPC-A, UPC-E
- **Real-time scanning** with ZXing library integration
- **Audio/Vibration feedback** on successful scan
- **Live counter** showing Present/Total with color-coded progress
- **Duplicate prevention** with visual alerts

### 2. OCR (Optical Character Recognition)
- **ML Kit Text Recognition** for scanning physical ID cards
- **Automatic roll number extraction** from images
- **Support for gallery and camera** input
- **Multi-roll number detection** in a single image
- **Fallback to manual entry** if OCR fails

### 3. Manual Roll Number Entry
- **AutoComplete suggestions** from student database
- **Real-time validation** with visual feedback
- **Batch entry support** (comma/space/newline separated)
- **Keyboard shortcuts** for faster entry

### 4. Excel Import/Export
- **Import student lists** from Excel (.xlsx, .xls)
- **Export attendance reports** with summary statistics
- **Preview before importing**
- **Apache POI library** integration
- **Auto-save to Downloads** folder

### 5. Database Management
- **Room Database** for persistent storage
- **Three main entities**:
  - Student: Roll number, name, department, year, email
  - Attendance: Student ID, session ID, date, time, status, scan method
  - AttendanceSession: Session name, date, total/present count

### 6. Live Statistics
- **Real-time counter**: Present/Total (e.g., 24/60)
- **Progress bar** with percentage
- **Color coding**:
  - Red: < 50%
  - Orange: 50-75%
  - Green: > 75%

## Technical Architecture

### Dependencies
```gradle
// QR/Barcode Scanning
implementation 'com.google.zxing:core:3.5.1'
implementation 'com.journeyapps:zxing-android-embedded:4.3.0'

// OCR
implementation 'com.google.mlkit:text-recognition:16.0.0'

// Excel handling
implementation 'org.apache.poi:poi:5.2.3'
implementation 'org.apache.poi:poi-ooxml:5.2.3'

// Database
implementation 'androidx.room:room-runtime:2.5.2'
annotationProcessor 'androidx.room:room-compiler:2.5.2'

// File handling
implementation 'androidx.documentfile:documentfile:1.0.1'
```

### Package Structure
```
com.example.project.attendance/
├── database/
│   ├── AttendanceDatabase.java       # Room database singleton
│   ├── StudentDao.java                # Student data access object
│   ├── AttendanceDao.java             # Attendance data access object
│   └── AttendanceSessionDao.java      # Session data access object
├── model/
│   ├── Student.java                   # Student entity
│   ├── Attendance.java                # Attendance record entity
│   └── AttendanceSession.java         # Session entity
├── utils/
│   ├── ExcelImporter.java             # Excel import utility
│   ├── ExcelExporter.java             # Excel export utility
│   ├── AttendanceValidator.java       # Validation utility
│   ├── QRCodeScanner.java             # Scanner feedback utility
│   └── OCRProcessor.java              # ML Kit OCR processor
├── AttendanceMainActivity.java        # Main dashboard
├── ScanAttendanceActivity.java        # QR/Barcode scanning
├── OCRScanActivity.java               # OCR scanning
├── ManualEntryActivity.java           # Manual entry
├── ImportStudentsActivity.java        # Excel import
├── ExportReportActivity.java          # Excel export
├── AttendanceHistoryActivity.java     # History viewer
├── ScannedStudentsAdapter.java        # RecyclerView adapter
└── AttendanceHistoryAdapter.java      # History adapter
```

### Database Schema

#### Students Table
| Column     | Type   | Description                    |
|------------|--------|--------------------------------|
| id         | INT    | Primary key (auto-increment)   |
| rollNumber | TEXT   | Unique student roll number     |
| name       | TEXT   | Student name                   |
| department | TEXT   | Department/Branch              |
| year       | TEXT   | Year of study                  |
| email      | TEXT   | Email address (optional)       |

#### Attendance Table
| Column     | Type   | Description                          |
|------------|--------|--------------------------------------|
| id         | INT    | Primary key (auto-increment)         |
| studentId  | INT    | Foreign key to students table        |
| sessionId  | INT    | Foreign key to attendance_sessions   |
| date       | TEXT   | Date (YYYY-MM-DD)                    |
| time       | TEXT   | Time (HH:MM:SS)                      |
| status     | TEXT   | "present" or "absent"                |
| scanMethod | TEXT   | "qr", "barcode", "ocr", or "manual"  |

#### AttendanceSession Table
| Column        | Type   | Description                    |
|---------------|--------|--------------------------------|
| id            | INT    | Primary key (auto-increment)   |
| sessionName   | TEXT   | Name of the session            |
| date          | TEXT   | Session date                   |
| startTime     | TEXT   | Start time                     |
| endTime       | TEXT   | End time (nullable)            |
| totalStudents | INT    | Total student count            |
| presentCount  | INT    | Number of students present     |

## Usage Guide

### 1. Setting Up Student Database
1. Open **Import Students** from the main menu
2. Click **Select Excel File**
3. Choose your student list Excel file
4. Preview the data
5. Click **Import** to load into database

**Excel Format Requirements:**
- Column 1: Roll Number (required)
- Column 2: Name (required)
- Column 3: Department
- Column 4: Year
- Column 5: Email

### 2. Marking Attendance

#### Option A: QR/Barcode Scanning
1. Open **Scan Attendance**
2. Click **Scan QR/Barcode**
3. Point camera at student ID
4. Audio/vibration confirms successful scan
5. Student appears in the list immediately
6. Live counter updates in real-time

#### Option B: OCR Scanning
1. Open **OCR Scan**
2. Choose **Select Image** or **Capture**
3. Select/capture ID card image
4. Click **Process Image**
5. Review detected roll numbers
6. Select which ones to mark present

#### Option C: Manual Entry
1. Open **Manual Entry**
2. Start typing roll number (autocomplete suggestions appear)
3. Press **Mark Present**
4. For batch: Enter multiple roll numbers separated by commas/spaces/newlines
5. Click **Submit Batch**

### 3. Exporting Reports
1. Open **Export Report**
2. Select a session from the dropdown
3. Click **Export to Excel**
4. File is saved to Downloads/CheckMate/Reports/
5. Option to open or share immediately

**Export includes:**
- Session details (name, date, time)
- Summary statistics (total, present, absent, percentage)
- Complete student list with:
  - Roll number
  - Name
  - Department
  - Year
  - Status (✓/✗)
  - Time marked
  - Scan method

### 4. Viewing History
1. Open **Attendance History**
2. View all past sessions
3. Each card shows:
   - Session name and date
   - Time range
   - Present/Total count
   - Attendance percentage (color-coded)

## Permissions Required

The app requires the following permissions:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.VIBRATE" />
```

Permissions are requested at runtime when accessing the attendance module.

## Validation & Error Handling

### Roll Number Validation
- Format: 3-20 alphanumeric characters (e.g., ABC123, CSE2024001)
- Automatic sanitization (uppercase, remove spaces)
- Real-time feedback (green ✓ for valid, red ✗ for invalid)

### Duplicate Prevention
- Checks database before marking attendance
- Shows dialog with existing entry time
- Option to override if needed
- All attempts are logged

### Error Scenarios
1. **Student not found**: Shows alert with suggestion to import students
2. **OCR fails**: Offers manual entry as fallback
3. **Excel import errors**: Shows detailed error message with format requirements
4. **Network/storage issues**: Graceful degradation with user feedback

## File Management

### Import Files
- Supported formats: .xlsx, .xls
- Can be selected from any location on device

### Export Files
- **Android 10+**: Uses MediaStore API
- **Older versions**: Direct file access
- Location: Downloads/CheckMate/Reports/
- Filename format: Attendance_SessionName_YYYYMMDD_HHMMSS.xlsx

### Sharing
- Files can be shared via:
  - Email
  - WhatsApp
  - Google Drive
  - Any app that accepts Excel files

## Performance Considerations

### Threading
- All database operations run on background threads using ExecutorService
- UI updates happen on main thread
- No ANR (Application Not Responding) issues

### Database Optimization
- Indexed columns for fast lookups
- Foreign key constraints for data integrity
- Efficient queries with Room DAOs

### Memory Management
- Images for OCR are properly disposed
- Database connections are managed by Room
- RecyclerViews use ViewHolder pattern

## Testing Recommendations

### Unit Testing
- Test AttendanceValidator.java methods
- Test Excel import/export with sample data
- Test database operations

### Integration Testing
- Test QR scanning with test QR codes
- Test OCR with sample ID cards
- Test full attendance marking flow

### UI Testing
- Test navigation between activities
- Test RecyclerView scrolling and updates
- Test dialog interactions

## Troubleshooting

### Common Issues

**1. QR Scanner not opening**
- Check camera permission
- Ensure device has a camera
- Try restarting the app

**2. OCR not detecting text**
- Ensure good lighting
- Try higher resolution image
- Make sure text is clear and unobstructed
- Use manual entry as fallback

**3. Excel import fails**
- Verify file format (.xlsx or .xls)
- Check first row has headers
- Ensure roll numbers are in first column
- Verify file is not corrupted

**4. Export not saving**
- Check storage permission
- Ensure sufficient storage space
- Check if Downloads folder is accessible

## Future Enhancements

Potential additions:
- Cloud sync for multi-device support
- Biometric attendance (fingerprint/face)
- GPS-based attendance (location verification)
- Bluetooth/NFC attendance
- Analytics and reporting dashboard
- PDF report generation
- Email notifications
- Integration with school management systems

## Credits

Built with:
- ZXing for barcode scanning
- Google ML Kit for OCR
- Apache POI for Excel handling
- Room for local database
- Material Design components

## License

This module is part of the CheckMate application.

## Support

For issues or questions:
- Check the troubleshooting section
- Review the code documentation
- Contact the development team
