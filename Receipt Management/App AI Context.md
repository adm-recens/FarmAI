# FarmAI App AI Context

**Purpose:** This is the primary AI context document for the Android FarmAI application.

**Important:** The older `AI_CONTEXT.md` in this folder describes a separate web-based VoucherOCR platform. Use it only as background inspiration for expected receipt OCR functionality. For all Android app development, use this file as the main source of truth.

**Broader scope plan:** `BROADER_SCOPE_PLANNER.md`

**Last updated:** 2026-06-14

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
- Receipt date is saved as current time instead of parsed voucher date.
- Pasted OCR text parsing does not auto-fill receipt form fields.
- Image OCR is not implemented.
- Camera capture is not implemented.

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

- `parseReceiptImage(imagePath)` returns an empty `ParsedReceiptData()` placeholder.
- `parseReceiptText(rawText)` parses pasted receipt text.
- Parsed OCR data is not converted into receipt form fields.

Current parser file:

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

Current gaps:

- No camera capture.
- No image OCR.
- No crop UI.
- No smart crop.
- No OCR confidence.
- No parser confidence.
- No validation screen.
- No original/corrected parser snapshot.

---

## 9. Current Reports Status

Report queries exist in:

```text
core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt
```

Report repository exists in:

```text
core/data/src/main/java/com/farmai/core/data/repository/ReportRepositoryImpl.kt
```

Report use cases exist in:

```text
core/domain/src/main/java/com/farmai/core/domain/usecase/report/ReportUseCases.kt
```

Implemented at data/domain level:

- Farmer summaries
- Broker settlements
- Monthly trends
- Deduction analysis
- Export row queries

Not implemented:

- Reports screen
- Reports navigation
- PDF export
- Excel export
- CSV export
- Share sheet

Important limitation:

- Report queries join receipts, line items, and deductions directly.
- For multi-line receipts, deduction sums can be multiplied by the number of line items.
- This must be fixed before reports are trusted.

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
