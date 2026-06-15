# FarmAI App AI Context

**Purpose:** This is the primary AI context document for the Android FarmAI application.

**Important:** The older `AI_CONTEXT.md` in this folder describes a separate web-based VoucherOCR platform. Use it only as background inspiration for expected receipt OCR functionality. For all Android app development, use this file as the main source of truth.

**Broader scope plan:** `BROADER_SCOPE_PLANNER.md`

**Last updated:** 2026-06-15

---

## 1. Application Overview

FarmAI is a modular Android application for managing farmer, broker, and receipt workflows for Indian farm produce sales.

Current goal:

- Build a functional, modular, scalable Android application.
- Start from local-first farmer, broker, and receipt management.
- Evolve toward receipt image capture, OCR, parsing, validation, batch processing, reports, exports, supplier management, ML feedback, and optional backend sync.

The current build is a good prototype foundation, but the end goal is much broader than the current static/local CRUD implementation.

---

## 2. Current Technology Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| UI toolkit | Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Local database | Room |
| Async | Kotlin Coroutines + StateFlow |
| Serialization | kotlinx.serialization |
| Camera planned | CameraX |
| OCR planned | Google ML Kit Text Recognition |
| Background jobs planned | WorkManager |
| Export planned | PDF/Android document APIs |
| Sync planned | Retrofit/Ktor or similar backend client |

Current build configuration:

- Android Gradle Plugin: `8.13.2`
- Kotlin: `1.9.22`
- Compose BOM: `2024.05.00`
- Room: `2.6.1`
- Hilt: `2.48.1`
- Navigation Compose: `2.7.7`
- CameraX: `1.4.0-alpha02`
- ML Kit Text Recognition: `16.0.0`
- Compile SDK: `34`
- Target SDK: `34`
- Min SDK: `24`
- JVM target: `17`

---

## 3. Current Module Structure

```text
:app
:core:domain
:core:data
:feature:farmer
:feature:broker
:feature:receipt
```

### `:app`

Responsibilities:

- Application entry point
- MainActivity
- Compose navigation host
- Hilt application graph
- Sample data seeding

Key files:

- `app/src/main/java/com/farmai/app/MainActivity.kt`
- `app/src/main/java/com/farmai/app/FarmAIApplication.kt`
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `app/src/main/java/com/farmai/app/navigation/HomeScreen.kt`
- `app/src/main/java/com/farmai/app/data/SampleDataSeeder.kt`
- `app/src/main/java/com/farmai/app/di/AppModule.kt`

### `:core:domain`

Responsibilities:

- Domain models
- Repository interfaces
- Use cases
- Business rules
- Receipt parser and parsed-data mapper

Key folders:

- `core/domain/src/main/java/com/farmai/core/domain/model`
- `core/domain/src/main/java/com/farmai/core/domain/repository`
- `core/domain/src/main/java/com/farmai/core/domain/usecase`

### `:core:data`

Responsibilities:

- Room database
- DAOs
- Entities
- Repository implementations
- Current OCR text parser

Key files:

- `core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReceiptDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/FarmerDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/BrokerDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/ReportRepositoryImpl.kt`

### `:feature:farmer`

Responsibilities:

- Farmer list UI
- Farmer detail UI
- Farmer ViewModel

Key files:

- `feature/farmer/src/main/java/com/farmai/feature/farmer/ui/FarmerListScreen.kt`
- `feature/farmer/src/main/java/com/farmai/feature/farmer/ui/FarmerDetailScreen.kt`
- `feature/farmer/src/main/java/com/farmai/feature/farmer/viewmodel/FarmerViewModel.kt`

### `:feature:broker`

Responsibilities:

- Broker list UI
- Broker detail UI
- Broker ViewModel

Key files:

- `feature/broker/src/main/java/com/farmai/feature/broker/ui/BrokerListScreen.kt`
- `feature/broker/src/main/java/com/farmai/feature/broker/ui/BrokerDetailScreen.kt`
- `feature/broker/src/main/java/com/farmai/feature/broker/viewmodel/BrokerViewModel.kt`

### `:feature:receipt`

Responsibilities:

- Receipt list UI
- Receipt entry UI
- Receipt detail UI
- Receipt ViewModels
- Current OCR parser integration point

Key files:

- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptListScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptDetailScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptViewModel.kt`

---

## 4. Current App Features

### 4.1 Farmer Management

Implemented:

- List farmers
- Search farmers
- Add farmer
- Edit farmer route exists but needs verification
- Delete farmer

### 4.2 Broker Management

Implemented:

- List brokers
- Search brokers
- Add broker
- Edit broker route exists but needs verification
- Delete broker

### 4.3 Receipt Management

Implemented:

- Receipt list
- Receipt search
- Add receipt
- Manual line item entry
- Manual deduction entry
- Paste OCR text and parse it
- Receipt detail
- Confirm draft receipt
- Delete receipt

Current limitations:

- Receipt entry stores `farmerId = farmerCode` and `brokerId = brokerName`. This should be improved later by selecting saved farmers and brokers by ID.
- Receipt edit route is referenced from the list screen but is not currently registered in navigation.
- Image OCR is implemented for saved receipt images, but camera capture is not implemented.
- OCR text parsing now produces structured data with confidence scores and can be applied to the receipt form, but OCR orchestration is still in the UI layer.
- Batch queue list/detail and receipt job status updates are implemented, but background WorkManager crop/OCR/parse workers are not implemented yet.
- Smart crop UI and crop-box storage are implemented, but OCR workers do not consume cropped images yet.
- Export UI, supplier management, and sync are not implemented.

---

## 5. Current Navigation

Current navigation is defined in:

```text
app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt
```

Current routes:

```text
home
farmers
farmer/add
farmer/edit/{farmerId}
brokers
broker/add
broker/edit/{brokerId}
receipts
receipt/add
receipt/{receiptId}
```

Known navigation gaps:

- `FarmerListScreen` navigates to `farmer/{farmer.id}`, but that route is not defined.
- `BrokerListScreen` navigates to `broker/{broker.id}`, but that route is not defined.
- `ReceiptListScreen` navigates to `receipt/edit/{receipt.id}`, but no receipt edit route exists.

Future routes should be added through the broader scope planner.

---

## 6. Current Database

Room database name:

```text
farmai_database
```

Database file:

```text
core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt
```

Current tables:

- `farmers`
- `brokers`
- `receipts`
- `receipt_line_items`
- `deductions`

Current schema summary:

```text
farmers (
  id PK,
  code,
  name,
  phone,
  address,
  createdAt,
  updatedAt
)

brokers (
  id PK,
  name,
  address,
  phone,
  createdAt,
  updatedAt
)

receipts (
  id PK,
  farmerId FK cascade,
  brokerId FK cascade,
  voucherNumber,
  voucherDate,
  imagePathsJson,
  ocrRawText,
  status,
  validationStatus,
  createdAt,
  updatedAt
)

receipt_line_items (
  id PK,
  receiptId FK cascade,
  quantity,
  pricePerUnit,
  amount,
  grade,
  sortOrder
)

deductions (
  id PK,
  receiptId FK cascade,
  type,
  amount,
  description,
  isPercentage,
  percentageValue
)

validation_snapshots (
  id PK,
  receiptId FK cascade,
  originalJson,
  correctedJson,
  createdAt,
  createdBy,
  source
)
```

Important database rule:

- Do not use destructive migrations for future production-facing changes.
- Add explicit Room migrations for schema changes.
- Preserve existing user data.

---

## 7. Current Domain Models

Main domain models:

| Model | File |
|---|---|
| `Farmer` | `core/domain/src/main/java/com/farmai/core/domain/model/Farmer.kt` |
| `Broker` | `core/domain/src/main/java/com/farmai/core/domain/model/Broker.kt` |
| `Receipt` | `core/domain/src/main/java/com/farmai/core/domain/model/Receipt.kt` |
| `ReceiptLineItem` | `core/domain/src/main/java/com/farmai/core/domain/model/ReceiptLineItem.kt` |
| `Deduction` | `core/domain/src/main/java/com/farmai/core/domain/model/Deduction.kt` |
| `ParsedReceiptData` | `core/domain/src/main/java/com/farmai/core/domain/model/ParsedReceiptData.kt` |
| `ValidationSnapshot` | `core/domain/src/main/java/com/farmai/core/domain/model/ValidationSnapshot.kt` |
| `CropBox` | `core/domain/src/main/java/com/farmai/core/domain/model/CropBox.kt` |
| Report models | `core/domain/src/main/java/com/farmai/core/domain/model/ReportModels.kt` |

Receipt statuses:

```text
DRAFT
CONFIRMED
SYNCED
```

Current note:

- `SYNCED` exists in the domain model, but no sync implementation currently exists.

---

## 8. Current OCR and Image Processing Status

Current OCR behavior:

- `parseReceiptImage()` still returns an empty `ParsedReceiptData()` placeholder.
- Smart crop can generate and persist a crop box for receipt jobs, but OCR has not yet been wired to use the crop box.
- `parseReceiptText(rawText)` parses pasted OCR text and image OCR text into structured fields.
- Parser output now includes confidence scores and field-level confidence.
- Receipt entry can apply parsed OCR data to voucher, date, farmer code, broker, line items, and deductions.
- Parsed OCR data is not yet stored as an immutable original/corrected validation snapshot.

Current parser file:

```text
core/domain/src/main/java/com/farmai/core/domain/parser/ReceiptOcrParser.kt
```

Core:data delegates parsing to:

```text
core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt
```

Current parser behavior:

- Parses pasted OCR text.
- Attempts to extract broker name/address/phone.
- Attempts to extract voucher number/date.
- Attempts to extract supplier/farmer code.
- Attempts to extract line items.
- Attempts to extract commission, damages, unloading, advance, and other deductions.
- Calculates parser confidence and field confidence.

Current gaps:

- No camera capture.
- No image OCR orchestration in a dedicated OCR/background module.
- OCR workers do not yet consume crop boxes.
- Export UI, supplier management, and sync are not implemented.

---

## 9. Current Reports Status

Report queries, repository, use cases, and UI now exist in:

```text
core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt
core/data/src/main/java/com/farmai/core/data/repository/ReportRepositoryImpl.kt
core/domain/src/main/java/com/farmai/core/domain/usecase/report/ReportUseCases.kt
feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReportsScreen.kt
feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReportsViewModel.kt
```

Implemented at data/domain/UI level:

- Farmer summaries
- Broker settlements
- Monthly trends
- Deduction analysis
- Export row queries
- Reports screen with date filters and report type switching
- Home/navigation entry point for reports

Important fix:

- Report queries now aggregate line items and deductions per receipt before joining, preventing deduction totals from being multiplied on multi-line receipts.

Not implemented:

- PDF export
- Excel export
- CSV export
- Share sheet

---

## 10. Current Permissions

Manifest:

```text
app/src/main/AndroidManifest.xml
```

Declared permissions:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
```

Current limitation:

- Runtime camera permission handling is not implemented yet.

---

## 11. Target Future Architecture

The app should evolve into a cleaner modular architecture.

Recommended future module layout:

```text
:app
:core:domain
:core:data
:core:ocr
:core:parser
:core:image-processing
:core:sync
:core:export
:feature:dashboard
:feature:scan
:feature:queue
:feature:validation
:feature:receipt
:feature:batches
:feature:reports
:feature:suppliers
:feature:training
:feature:settings
```

### Recommended responsibilities

| Module | Responsibility |
|---|---|
| `core:domain` | Models, repository interfaces, use cases, business rules |
| `core:data` | Room database, DAOs, entities, migrations, repository implementations |
| `core:ocr` | CameraX, ML Kit OCR, OCR models, OCR confidence |
| `core:parser` | Receipt parsing, parser confidence, parser tests |
| `core:image-processing` | Smart crop, crop box storage, image resize/compression |
| `core:sync` | Backend API client, sync outbox, conflict handling |
| `core:export` | PDF, Excel, CSV, share sheet |
| `feature:*` | Compose UI, ViewModels, navigation |

---

## 12. Target Future Screens

The broader scope planner defines the full future screen layout.

Planned screens include:

- Dashboard
- Scan/camera
- Gallery import
- Batch selection
- Processing queue
- Smart crop
- OCR preview
- Validation
- Review
- Receipt vault
- Receipt detail
- Batch management
- Reports
- Supplier management
- Training hub
- Learning logs
- Sync settings
- App settings

---

## 13. Target Processing Pipeline

Future receipt flow:

```text
Capture / Import
    ↓
Create batch job
    ↓
Smart crop
    ↓
OCR
    ↓
Parse OCR text
    ↓
Validation screen
    ↓
Save receipt
    ↓
Store original + corrected parser data
    ↓
Add ML training snapshot
    ↓
Reports update
    ↓
Export / Sync
```

The first major milestone should be:

```text
Capture/import image → OCR → parse → validation screen → save receipt
```

---

## 14. Development Rules

### 14.1 Context rules

- Read this file before Android app work.
- Read `BROADER_SCOPE_PLANNER.md` when working on broader OCR, validation, batch, reports, ML, or sync features.
- Treat `AI_CONTEXT.md` as historical web-platform context only.
- Do not copy web-only backend assumptions into Android without adapting them.

### 14.2 Architecture rules

- Keep feature modules focused on UI and ViewModels.
- Put business rules in `core:domain`.
- Put persistence and external integrations in `core:data` or future dedicated core modules.
- Prefer repository interfaces in domain and implementations in data.
- Prefer StateFlow in ViewModels.
- Avoid direct database access from UI.
- Avoid direct API calls from UI.
- Use WorkManager for long-running background jobs such as OCR, parsing, smart crop, training, and sync.

### 14.3 Database rules

- Add Room migrations for schema changes.
- Do not use destructive migrations for production-facing changes.
- Preserve user data.
- Store raw OCR text and parser snapshots when OCR features are added.
- Store original parser output and corrected output for validation.
- Use soft delete where auditability is important.

### 14.4 OCR/parser rules

- Do not hardcode supplier-specific parsing logic.
- Use data-driven parsing, aliases, fuzzy matching, and correction history.
- Store parser confidence.
- Store OCR confidence.
- Allow users to correct low-confidence fields.
- Never silently overwrite user-corrected data with parser guesses.

### 14.5 UI rules

- Use Jetpack Compose.
- Use Material 3.
- Keep UI responsive during background work.
- Show loading, empty, error, and retry states.
- Show confidence warnings for OCR/parser fields.
- Use safe navigation and deep-link-safe routes where appropriate.

### 14.6 Security and privacy rules

- Do not log raw OCR text unless explicitly required for debugging and properly redacted.
- Do not expose receipt images outside app-controlled storage.
- Do not sync data without explicit user consent.
- Do not store backend tokens or secrets in source code.
- Do not hardcode backend URLs or credentials.

### 14.7 Testing rules

- Add parser tests before changing parser behavior.
- Add repository tests where feasible.
- Add use-case tests for business rules.
- Add Compose UI tests for critical flows when practical.
- Run build and tests after meaningful changes.

---

## 15. Build and Verification Commands

Recommended commands:

```powershell
.\gradlew :app:assembleDebug
```

Clean build:

```powershell
.\gradlew clean :app:assembleDebug --rerun-tasks --no-build-cache
```

Run tests:

```powershell
.\gradlew test
```

Run instrumented tests with connected device/emulator:

```powershell
.\gradlew connectedAndroidTest
```

---

## 16. Broader Scope Progress Log

Update this section after every successful iteration.

### Iteration 0 — App AI Context Created

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Created Android-specific AI context document and separated it from the historical web-platform context.  
**Summary:**
- Defined this file as the primary source of truth for Android development.
- Documented current app architecture.
- Documented current features and limitations.
- Documented future architecture and processing pipeline.
- Added development rules for Android app work.
- Linked broader scope planner for future improvements.

**Files touched:**
- `Receipt Management\App AI Context.md`

**Next iteration:**
- Phase 1 — Architecture cleanup, migration planning, and navigation gap fixes.

### Iteration 1 — Navigation Gaps and Receipt Edit Support

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Fixed missing farmer/broker detail routes, added receipt edit route, and added basic receipt edit loading/saving support.  
**Summary:**
- Added `farmer/{farmerId}` route so farmer list detail navigation works.
- Added `broker/{brokerId}` route so broker list detail navigation works.
- Added `receipt/edit/{receiptId}` route so receipt list edit navigation works.
- Farmer and broker detail screens now load existing records when opened by ID.
- Receipt entry screen now supports loading existing receipt data for edit mode.
- Receipt edit saves the existing receipt ID, current image paths, existing OCR text fallback, current status, and created timestamp.
- Voucher date text is now parsed into epoch time when saving receipts.
- Replaced deprecated default arrow-back icons with auto-mirrored icons.
- Removed an unused parser result variable warning.

**Files touched:**
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `feature/farmer/src/main/java/com/farmai/feature/farmer/ui/FarmerDetailScreen.kt`
- `feature/broker/src/main/java/com/farmai/feature/broker/ui/BrokerDetailScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptViewModel.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- Build passed successfully.

**Next iteration:**
- Phase 2 — Camera/gallery capture foundation and image storage.

### Iteration 2 — Phase 1 Database Migration and WorkManager Foundation

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Added Room migration infrastructure, database version 2, batch/job schema foundation, DAOs, and WorkManager dependency.  
**Summary:**
- Added explicit Room migration `1 -> 2`.
- Removed destructive migration fallback from `AppDatabase`.
- Added `batches` table for future bulk receipt processing.
- Added `receipt_jobs` table for future crop/OCR/parse/validation queue jobs.
- Added `BatchEntity` and `ReceiptJobEntity`.
- Added `BatchDao` and `ReceiptJobDao`.
- Registered new entities in `AppDatabase`.
- Added WorkManager dependency to `core:data` for future background crop, OCR, parsing, training, and sync jobs.

**Files touched:**
- `core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt`
- `core/data/src/main/java/com/farmai/core/data/local/migration/DatabaseMigrations.kt`
- `core/data/src/main/java/com/farmai/core/data/local/entity/BatchEntity.kt`
- `core/data/src/main/java/com/farmai/core/data/local/entity/ReceiptJobEntity.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/BatchDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReceiptJobDao.kt`
- `core/data/build.gradle.kts`
- `gradle/libs.versions.toml`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- Build passed successfully.

**Next iteration:**
- Phase 3 — OCR text extraction foundation.

### Iteration 3 — Phase 2 Gallery Import and Image Storage

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Added gallery image import and app-controlled image storage to receipt entry/edit flow.  
**Summary:**
- Added image picker to `ReceiptEntryScreen`.
- Copied selected gallery images into app-specific `receipt_images` storage.
- Stored selected image paths in the receipt `imagePaths` list.
- Added receipt image preview using Coil.
- Preserved existing image paths when editing receipts.
- Added strings for receipt image import/preview.

**Files touched:**
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- Build passed successfully.

**Next iteration:**
- Phase 4 — Parser improvement and OCR-to-form mapping.

### Iteration 4 — Phase 3 ML Kit Image OCR

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Added ML Kit image OCR from saved receipt images into receipt entry/edit flow.  
**Summary:**
- Added `Run OCR on Image` action for saved receipt images.
- Used ML Kit Latin text recognizer to extract text from the selected image.
- Populated the OCR text field with extracted image OCR text.
- Added OCR error display for failed OCR runs.
- Preserved existing OCR text while editing receipts.
- Kept OCR execution in the UI layer for this phase; future phases should move OCR orchestration into a dedicated OCR/background module.

**Files touched:**
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- Build passed successfully.

**Next iteration:**
- Phase 4/5 — Parser improvement and OCR-to-form mapping.

### Iteration 5 — Phase 4/5 Parser Improvement and OCR-to-Form Mapping

**Date:** 2026-06-14
**Status:** Completed
**Scope:** Moved receipt parsing into `core:domain`, improved parser extraction, added parser confidence, added parsed-data mapping, and wired OCR parsing into the receipt entry form.
**Summary:**
- Moved `ReceiptOcrParser` from `core:data` into `core:domain:parser`.
- Added parser confidence and field-level confidence to `ParsedReceiptData`.
- Improved parsing for broker details, voucher number/date, supplier/farmer code, line items, and deductions.
- Added `ParsedReceiptMapper` to convert parsed data into receipt, line item, and deduction domain objects.
- Added parser unit tests in `core:domain`.
- Updated receipt entry to parse OCR text, show parsed summary, and explicitly apply parsed data to the editable form.
- Kept parser application explicit so user-corrected fields are not silently overwritten.

**Files touched:**
- `core/domain/src/main/java/com/farmai/core/domain/model/ParsedReceiptData.kt`
- `core/domain/src/main/java/com/farmai/core/domain/parser/ReceiptOcrParser.kt`
- `core/domain/src/main/java/com/farmai/core/domain/mapper/ParsedReceiptMapper.kt`
- `core/domain/build.gradle.kts`
- `core/domain/src/test/java/com/farmai/core/domain/parser/ReceiptOcrParserTest.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptViewModel.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :core:domain:testDebugUnitTest`
- `.\gradlew :app:assembleDebug`
- Both commands passed successfully.

**Next iteration:**
- Phase 6 — Validation Screen.

### Iteration 6 — Phase 6 Validation Screen and Validation Snapshots

**Date:** 2026-06-14
**Status:** Completed
**Scope:** Added validation persistence, validation status, and a receipt validation screen that saves original and corrected parser snapshots.
**Summary:**
- Added Room database version 3 with `validation_snapshots` and `receipts.validationStatus`.
- Added explicit migration `2 -> 3`.
- Added `ValidationSnapshot`, `ValidationStatus`, entity, DAO, repository methods, and use cases.
- Added `ReceiptValidationScreen` for image/OCR review, editable parsed fields, line items, deductions, re-parse/re-OCR actions, and draft/confirmed validation saves.
- Added receipt detail navigation to the validation screen.
- Stored original parser JSON from OCR text and corrected parser JSON from the validation form.

**Files touched:**
- `core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt`
- `core/data/src/main/java/com/farmai/core/data/local/migration/DatabaseMigrations.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReceiptDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ValidationSnapshotDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/entity/ReceiptEntity.kt`
- `core/data/src/main/java/com/farmai/core/data/local/entity/ValidationSnapshotEntity.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt`
- `core/domain/src/main/java/com/farmai/core/domain/model/ParsedReceiptData.kt`
- `core/domain/src/main/java/com/farmai/core/domain/model/Receipt.kt`
- `core/domain/src/main/java/com/farmai/core/domain/model/ValidationSnapshot.kt`
- `core/domain/src/main/java/com/farmai/core/domain/repository/ReceiptRepository.kt`
- `core/domain/src/main/java/com/farmai/core/domain/usecase/receipt/ReceiptUseCases.kt`
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptDetailScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptValidationScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptValidationViewModel.kt`
- `feature/receipt/src/main/res/values/strings.xml`
- `feature/receipt/build.gradle.kts`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- `.\gradlew :core:domain:testDebugUnitTest`
- Both commands passed successfully.

**Next iteration:**
- Phase 7 — Batch and Queue System.

### Iteration 7 — Phase 7 Batch and Queue System Foundation

**Date:** 2026-06-14
**Status:** Completed
**Scope:** Added batch and queue repository/use-case layer, batch list/detail screens, receipt job queue actions, and navigation from home.
**Summary:**
- Added domain `Batch`, `BatchStatus`, `ReceiptJob`, and `ReceiptJobStatus` models.
- Added `BatchRepository` with batch/job observation, batch creation, receipt job creation, status updates, failure marking, and delete actions.
- Added `BatchRepositoryImpl` and Hilt binding.
- Added batch use cases for observing, creating, adding receipt jobs, updating job status, marking failures, and deleting.
- Added `BatchListScreen` and `BatchDetailScreen` for creating batches, adding receipt images to a queue, and manually advancing job statuses.
- Added home navigation to batch management.
- Kept WorkManager worker implementation for a future phase.

**Files touched:**
- `core/domain/src/main/java/com/farmai/core/domain/model/Batch.kt`
- `core/domain/src/main/java/com/farmai/core/domain/model/ReceiptJob.kt`
- `core/domain/src/main/java/com/farmai/core/domain/repository/BatchRepository.kt`
- `core/domain/src/main/java/com/farmai/core/domain/usecase/batch/BatchUseCases.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/BatchRepositoryImpl.kt`
- `core/data/src/main/java/com/farmai/core/data/di/DatabaseModule.kt`
- `app/src/main/java/com/farmai/app/navigation/HomeScreen.kt`
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `app/src/main/res/values/strings.xml`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/BatchListViewModel.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/BatchDetailViewModel.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/BatchListScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/BatchDetailScreen.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :app:assembleDebug`
- `.\gradlew :core:domain:testDebugUnitTest`
- Both commands passed successfully.

**Next iteration:**
- Phase 8 — Smart Crop.

### Iteration 8 — Phase 8 Smart Crop

**Date:** 2026-06-15
**Status:** Completed
**Scope:** Added smart crop model, crop UI, crop-box persistence, and batch queue crop status tracking.
**Summary:**
- Added `CropBox` and `SmartCropProfile` domain models with JSON encode/decode helpers.
- Added crop-box use cases for observing jobs, generating auto-crop boxes, and persisting saved crop boxes.
- Added `ReceiptJobStatus.CROPPED` and persisted crop-box JSON/confidence on receipt jobs.
- Added `SmartCropScreen` with image preview, draggable crop rectangle, auto-crop, skip crop, crop values, and save action.
- Added `SmartCropViewModel` with image dimension reading and lightweight content-based auto-crop heuristic.
- Added batch count refresh after job add/update/delete, including `totalImages`, processed, validated, failed, and derived batch status.
- Added queue job crop navigation from `BatchDetailScreen`.
- Added crop-box unit tests.
- Kept WorkManager OCR/crop workers and actual cropped-image output for a future phase.

**Files touched:**
- `core/domain/src/main/java/com/farmai/core/domain/model/CropBox.kt`
- `core/domain/src/main/java/com/farmai/core/domain/model/ReceiptJob.kt`
- `core/domain/src/main/java/com/farmai/core/domain/repository/BatchRepository.kt`
- `core/domain/src/main/java/com/farmai/core/domain/usecase/crop/CropUseCases.kt`
- `core/domain/src/test/java/com/farmai/core/domain/model/CropBoxTest.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/BatchDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReceiptJobDao.kt`
- `core/data/src/main/java/com/farmai/core/data/local/entity/ReceiptJobEntity.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/BatchRepositoryImpl.kt`
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/BatchDetailScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/SmartCropScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/SmartCropViewModel.kt`
- `feature/receipt/src/main/res/values/strings.xml`

**Verification performed:**
- `.\gradlew :core:domain:testDebugUnitTest`
- `.\gradlew :app:assembleDebug`
- Both commands passed successfully.

**Next iteration:**
- Phase 9 — Reports.

### Iteration 9 — Phase 9 Reports

**Date:** 2026-06-15
**Status:** Completed
**Scope:** Added reports UI, report navigation, date filtering, and fixed multi-line deduction aggregation.
**Summary:**
- Added `ReportsScreen` with farmer, broker, monthly, deduction, and export-row report views.
- Added `ReportsViewModel` with date-range parsing, report type selection, loading/error states, and report data state.
- Added home navigation entry for reports.
- Fixed `ReportDao` queries to aggregate line items and deductions per receipt before joining, preventing deduction duplication on multi-line receipts.
- Added report UI strings for filters, report sections, and metric cards.
- Kept PDF/Excel/CSV/share export implementation for the next phase.

**Files touched:**
- `core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt`
- `app/src/main/java/com/farmai/app/navigation/HomeScreen.kt`
- `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`
- `app/src/main/res/values/strings.xml`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReportsScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReportsViewModel.kt`
- `feature/receipt/src/main/res/values/strings.xml`
- `Receipt Management\App AI Context.md`
- `Receipt Management\BROADER_SCOPE_PLANNER.md`

**Verification performed:**
- `.\gradlew :core:domain:testDebugUnitTest`
- `.\gradlew :app:assembleDebug`
- Both commands passed successfully.

**Next iteration:**
- Phase 10 — Export/Share.

---

## 17. Instructions for Future AI Sessions

Before starting Android development:

1. Read this file.
2. Read `BROADER_SCOPE_PLANNER.md`.
3. Read only the relevant sections of the historical web `AI_CONTEXT.md` if OCR/validation/ML concepts are needed.
4. Confirm the latest progress log entry.
5. Continue from the latest completed iteration.

After every successful iteration:

1. Update the progress log in this file.
2. Update `BROADER_SCOPE_PLANNER.md` if the iteration changes roadmap, architecture, schema, routes, or major features.
3. Run build/tests where applicable.
4. Do not update progress for failed or abandoned attempts.
