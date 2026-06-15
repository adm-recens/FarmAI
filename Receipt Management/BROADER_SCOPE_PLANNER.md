# FarmAI Broader Scope Planner

**Purpose:** This document defines the detailed plan to evolve the current Android app from a local-first CRUD prototype into a functional, modular, scalable receipt OCR and receipt management platform similar to the web-based VoucherOCR system.

**Source context:** `App AI Context.md` for the Android app. `AI_CONTEXT.md` is historical web-platform context only.

**Last updated:** 2026-06-15

---

## 1. Current Application Baseline

The current Android app is a good starting foundation, but it is still mostly a static/local-first CRUD application.

Current implemented areas:

- Farmers CRUD
- Brokers CRUD
- Receipts CRUD
- Receipt list/detail/entry
- Manual line item entry
- Manual deduction entry
- Pasted OCR text parsing
- Saved image OCR text extraction
- Structured parser output with confidence scores
- OCR-to-form mapping for receipt entry
- Validation screen with original/corrected parser snapshots
- Validation status tracking
- Batch queue list/detail and manual receipt job status updates
- Smart crop UI with draggable crop rectangle and crop-box persistence
- Draft/confirmed receipt status
- Local Room database

Current key files:

| Area | File |
|---|---|
| Navigation | `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt` |
| Home | `app/src/main/java/com/farmai/app/navigation/HomeScreen.kt` |
| Receipt list | `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptListScreen.kt` |
| Receipt entry | `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt` |
| Receipt detail | `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptDetailScreen.kt` |
| Receipt repository/parser | `core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt` |
| Receipt entity | `core/data/src/main/java/com/farmai/core/data/local/entity/ReceiptEntity.kt` |
| Database | `core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt` |
| Report DAO | `core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt` |

Current known gaps:

- Camera capture is not implemented.
- `parseReceiptImage()` currently returns empty data; image OCR is currently triggered from the receipt entry UI.
- OCR orchestration should move into a dedicated OCR/background module.
- Batch queue background WorkManager crop/OCR/parse workers are not implemented yet.
- OCR workers do not yet consume stored crop boxes.
- PDF/share/export are placeholders.
- Reports exist at repository/query level but not as UI screens.
- Sync exists as a status value but no sync implementation exists.
- No batch queue, validation UI, smart crop, ML feedback loop, training hub, or learning logs exist yet.

---

## 2. Target Product Vision

The target Android application should become a mobile-first receipt OCR and farm produce sales management platform with the following capabilities:

1. Capture or import receipt images.
2. Automatically crop receipt images.
3. Extract OCR text from receipts.
4. Parse OCR text into structured financial data.
5. Let users validate and correct parsed data.
6. Save original parser output and corrected human-verified output.
7. Process multiple receipts in batches.
8. Track processing queues and job status.
9. Generate reports for farmers, brokers, deductions, and monthly trends.
10. Export receipts and reports.
11. Manage suppliers and aliases.
12. Learn from human corrections using an ML/algorithmic feedback loop.
13. Sync with the existing Flask/Postgres web platform when available.

The web app context describes these major modules:

| Web Module | Android Target |
|---|---|
| Dashboard | Android dashboard |
| Queue Upload | Camera/gallery import and batch creation |
| Queue Processor | Processing queue, crop editor, OCR status |
| Validation | Human-in-the-loop validation screen |
| Review | Read-only finalized receipt review |
| Batch Management | Batch list/detail/summary |
| Voucher Vault | Receipt vault/search/filter |
| Supplier Management | Supplier and alias management |
| Training Hub | Parser/smart crop training UI |
| Learning Logs | Learning history and model health |

---

## 3. Target Navigation Layout

Proposed Android navigation map:

```text
home
dashboard
scan
scan/camera
scan/gallery
scan/batch-select
queue
queue/{batchId}
queue/job/{jobId}/crop
queue/job/{jobId}/ocr
queue/job/{jobId}/validate
receipts
receipts/add
receipts/{receiptId}
receipts/{receiptId}/edit
receipts/{receiptId}/review
batches
batches/{batchId}
reports
reports/farmers
reports/brokers
reports/monthly
reports/deductions
suppliers
suppliers/{supplierId}
training
training/parser
training/smart-crop
learning-history
settings
sync-settings
```

Current navigation is limited to basic CRUD routes in:

```text
app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt
```

---

## 4. Screen-by-Screen Detailed Layout

### 4.1 Home / Dashboard

Purpose: Replace the current static home with an operational dashboard.

Layout:

- Summary cards:
  - Total receipts
  - Draft receipts
  - Confirmed receipts
  - Pending queue
  - Failed OCR jobs
  - Sync pending count
- Quick actions:
  - Scan receipt
  - Import from gallery
  - Create manual receipt
  - Open processing queue
  - Open reports
- Recent batches
- Recent receipts
- ML/parser health:
  - Last training run
  - Parser confidence trend
  - Smart crop confidence trend

---

### 4.2 Scan / Capture Screen

Purpose: Capture receipts from camera.

Layout:

- Camera preview
- Capture button
- Flash toggle
- Gallery import button
- Batch selector dropdown
- OCR language selector
- Auto-crop toggle
- Permission error state
- Quality hints:
  - Move closer
  - Too blurry
  - Receipt not detected

Implementation:

- CameraX
- ML Kit Text Recognition
- Android image picker
- Runtime camera/storage permissions

---

### 4.3 Import / Gallery Selection Screen

Purpose: Allow bulk import from device storage.

Layout:

- Multi-select image picker
- Batch name input
- Import selected images button
- Preview thumbnails
- Duplicate detection warning
- Import progress

---

### 4.4 Batch Selection / New Batch Screen

Purpose: Group multiple receipts together.

Layout:

- Create new batch
- Select existing open batch
- Batch metadata:
  - Batch name
  - Supplier/farmer optional
  - Date range optional
  - Notes
- Batch status:
  - Draft
  - Processing
  - Needs validation
  - Completed
  - Exported
  - Synced

---

### 4.5 Processing Queue Screen

Purpose: Show all receipts/images waiting for crop, OCR, parse, or validation.

Layout:

- Queue status chips:
  - Queued
  - Cropping
  - OCR running
  - Parsed
  - Needs validation
  - Validated
  - Failed
- Job cards:
  - Thumbnail
  - Batch name
  - Voucher number guess
  - Confidence score
  - Status
  - Error message
- Actions:
  - Retry OCR
  - Open crop editor
  - Open validation
  - Delete job
  - Mark failed

---

### 4.6 Smart Crop Screen

Purpose: Let users adjust receipt boundaries.

Layout:

- Full image preview
- Cropped receipt preview
- Adjustable crop rectangle
- Zoom controls
- Rotate controls
- Auto-crop confidence badge
- Buttons:
  - Auto crop
  - Retake
  - Save crop
  - Skip crop
- Optional original/cropped comparison

Implementation:

- OpenCV Android or custom Compose crop overlay
- Store crop box as JSON:
  - `x`
  - `y`
  - `width`
  - `height`
  - rotation
  - confidence
  - manual override flag

---

### 4.7 OCR Processing Screen / OCR Result Preview

Purpose: Run OCR and show raw extracted text.

Layout:

- Cropped receipt preview
- Raw OCR text box
- Detected text blocks with coordinates
- OCR confidence
- Retry OCR
- Save OCR text
- Continue to validation

Implementation:

- ML Kit Text Recognition
- Store raw OCR text
- Store block-level OCR JSON
- Store image path
- Store crop box JSON

---

### 4.8 Validation Screen

Purpose: Human-in-the-loop correction before saving receipt.

Layout:

- Top section:
  - Cropped receipt image
  - OCR text snippet
- Middle section:
  - Broker
  - Voucher number
  - Voucher date
  - Farmer/supplier code
  - Line items
  - Deductions
  - Grand total
- Confidence badges:
  - High
  - Medium
  - Low
- Validation actions:
  - Save as confirmed
  - Save as draft
  - Mark needs review
  - Retake image
  - Re-run OCR
  - Re-run parser

Important:

This screen must store both:

- `original_json`: parser guess
- `corrected_json`: human truth

This is critical for the ML feedback loop.

---

### 4.9 Review Screen

Purpose: Read-only finalized receipt review.

Layout:

- Receipt image
- OCR text
- Structured fields
- Line items
- Deductions
- Final totals
- Audit trail:
  - Created by
  - Validated by
  - Synced at
  - Exported at

---

### 4.10 Receipt Vault

Purpose: Searchable repository of every receipt.

Layout:

- Filters:
  - Date range
  - Batch
  - Farmer
  - Broker
  - Status
  - Confidence
  - Sync status
- Search:
  - Voucher number
  - Farmer code
  - Broker name
  - OCR text
- Sort:
  - Date
  - Amount
  - Status
  - Batch
- Actions:
  - Open receipt
  - Export
  - Revalidate
  - Re-OCR
  - Delete/archive

---

### 4.11 Receipt Detail Screen

Purpose: Expand current receipt detail into a full receipt record.

Layout:

- Basic info:
  - Voucher number
  - Voucher date
  - Farmer
  - Broker
  - Status
- Image section:
  - Original image
  - Cropped image
- OCR section:
  - Raw OCR text
  - OCR confidence
- Parser section:
  - Original parser result
  - Corrected result
- Line items
- Deductions
- Totals:
  - Gross
  - Commission
  - Damages
  - Unloading
  - Advance
  - Net payable
- Actions:
  - Edit
  - Validate again
  - Export PDF
  - Share
  - Sync now

---

### 4.12 Batch Management

Screens:

1. Batch list
2. Batch detail
3. Batch summary

Batch detail layout:

- Total images
- Processed count
- Validated count
- Failed count
- Export status
- Sync status
- Receipt list
- Error list
- Retry failed jobs
- Export batch
- Sync batch

---

### 4.13 Reports Screen

Subscreens:

1. Farmer summaries
2. Broker settlements
3. Monthly trends
4. Deduction analysis
5. Export rows

Layout:

- Date range selector
- Summary cards
- Charts/tables
- Export buttons:
  - PDF
  - Excel
  - CSV
  - Share

Important fix:

Current report queries can inflate deduction totals for multi-line receipts. This must be fixed before reports are trusted.

---

### 4.14 Supplier Management

Purpose: Manage suppliers/farmers and aliases.

Layout:

- Supplier list
- Supplier detail
- Alias management
- OCR name variants
- Confidence threshold
- Merge suggestions
- Manual alias creation

Rule:

Do not hardcode supplier-specific parsing logic. Use aliases, fuzzy matching, and learned correction data.

---

### 4.15 Training Hub

Subscreens:

1. Parser training
2. Smart crop training
3. Training history
4. Model status

Layout:

- Last trained timestamp
- Training data count
- Accuracy before/after
- Start training button
- Stop training button
- Progress logs
- Model confidence chart

Decision:

- Lightweight training may run on-device.
- Heavy training should run on the backend/server if the Flask/Postgres web app remains the training engine.

---

### 4.16 Learning Logs

Purpose: Show what the ML system learned.

Layout:

- Timeline of learning events
- Parser corrections
- Smart crop adjustments
- Character confusion matrix
- Anchor pattern improvements
- Accuracy gains over time

---

### 4.17 Sync Screen

Purpose: Sync local data with backend/web app.

Layout:

- Sync status:
  - Never synced
  - Synced
  - Pending
  - Failed
- Pending outbox count
- Last successful sync
- Manual sync button
- Conflict list
- Retry failed sync
- Server connection status

---

### 4.18 Settings

Sections:

- OCR settings:
  - OCR language
  - Auto-crop enabled
  - OCR confidence threshold
- Sync settings:
  - Backend URL
  - Sync on Wi-Fi only
  - Auto-sync
- Storage:
  - Image retention
  - Cache size
  - Export folder
- Privacy:
  - Clear OCR cache
  - Clear images
  - Export backup
- Developer:
  - Parser test samples
  - Logs
  - Database diagnostics

---

## 5. Target Android Architecture

### 5.1 Recommended Module Layout

Current modules:

```text
:app
:core:domain
:core:data
:feature:farmer
:feature:broker
:feature:receipt
```

Recommended future modules:

```text
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

### 5.2 Module Responsibilities

| Module | Responsibility |
|---|---|
| `core:domain` | Domain models, repository interfaces, use cases |
| `core:data` | Room database, DAOs, entities, migrations, repository implementations |
| `core:ocr` | CameraX, ML Kit OCR, OCR result models, OCR confidence |
| `core:parser` | Receipt parsing rules, parser confidence, parser tests |
| `core:image-processing` | Smart crop, crop box storage, image resize/compress |
| `core:sync` | Sync outbox, backend API client, conflict handling |
| `core:export` | PDF, Excel, CSV, share sheet integration |
| `feature:*` | Compose UI, ViewModels, navigation |

---

## 6. New Database Schema Plan

Current database tables:

- `farmers`
- `brokers`
- `receipts`
- `receipt_line_items`
- `deductions`

New tables/entities to add:

### 6.1 `batches`

Status: Implemented in database version 2.

Fields:

- `id`
- `name`
- `status`
- `createdAt`
- `updatedAt`
- `totalImages`
- `processedCount`
- `validatedCount`
- `failedCount`
- `notes`

### 6.2 `receipt_jobs`

Status: Implemented in database version 2.

Fields:

- `id`
- `batchId`
- `receiptId`
- `status`
- `imagePath`
- `cropBoxJson`
- `ocrRawText`
- `ocrLayoutJson`
- `parserJson`
- `confidenceScore`
- `error`
- `attemptCount`
- `createdAt`
- `updatedAt`

### 6.3 `validation_snapshots`

Status: Implemented in database version 3.

Fields:

- `id`
- `receiptId`
- `originalJson`
- `correctedJson`
- `createdAt`
- `createdBy`
- `source`

### 6.4 `suppliers`

Fields:

- `id`
- `name`
- `aliasesJson`
- `confidenceThreshold`
- `createdAt`
- `updatedAt`

### 6.5 `ml_character_matrix`

Fields:

- `id`
- `modelJson`
- `updatedAt`

### 6.6 `ml_fuzzy_anchors`

Fields:

- `id`
- `labelPattern`
- `targetField`
- `relativeOffset`
- `confidence`
- `correctedCount`
- `updatedAt`

### 6.7 `smart_crop_models`

Fields:

- `id`
- `thresholdsJson`
- `updatedAt`

### 6.8 `learning_events`

Fields:

- `id`
- `eventType`
- `beforeJson`
- `afterJson`
- `metricsJson`
- `createdAt`

### 6.9 `sync_outbox`

Fields:

- `id`
- `entityType`
- `entityId`
- `operation`
- `payloadJson`
- `status`
- `retryCount`
- `lastError`
- `createdAt`
- `updatedAt`

### 6.10 `sync_state`

Fields:

- `id`
- `lastSyncAt`
- `lastSuccessAt`
- `lastError`
- `pendingCount`
- `updatedAt`

---

## 7. Target Processing Pipeline

The app should follow this pipeline:

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

---

## 8. Detailed Implementation Phases

### Phase 0 — Clarify Product Decisions

Questions to resolve:

1. Should Android sync with the existing Flask/Postgres web backend?
2. Should ML training run on-device, on backend, or both?
3. Should the Android app remain offline-first?
4. Which OCR languages are needed?
   - English only
   - English + Telugu
   - English + Hindi
   - English + Urdu
5. Should smart crop be local-only or backend-assisted?
6. Should receipts be editable after confirmation?
7. Should deleted receipts be soft-deleted instead of permanently deleted?

---

### Phase 1 — Architecture Cleanup

Goal: Make the app modular and scalable.

Tasks:

- Add Room migrations.
- Stop using destructive migrations.
- Add WorkManager for background jobs.
- Split OCR/image/parser logic out of `core:data`.
- Create clean repository contracts.
- Improve current navigation gaps:
  - Add farmer detail route.
  - Add broker detail route.
  - Add receipt edit route.
- Add parser test infrastructure.
- Add sample OCR text fixtures.

Cleanup targets:

- `ReceiptRepositoryImpl` currently mixes persistence and parsing.
- `ReceiptEntryViewModel` parses text but does not map it into form fields.
- `ReportDao` needs query fixes for multi-line receipts.

---

### Phase 2 — Camera, Gallery Import, and Image Storage

Goal: Make receipt capture real.

Tasks:

- Add camera permission runtime handling.
- Add CameraX preview.
- Add capture button.
- Add gallery picker.
- Store images in app-specific storage.
- Save image path to receipt/job.
- Add thumbnail generation.
- Add image compression.
- Add image deletion/cleanup.

Deliverable:

User can capture or import a receipt image and see it in the app.

---

### Phase 3 — OCR Pipeline

Goal: Extract raw text from images.

Tasks:

- Implement ML Kit OCR.
- Store raw OCR text.
- Store OCR block data.
- Store OCR confidence.
- Add retry OCR.
- Add OCR error handling.
- Add OCR progress state.

Deliverable:

Captured/imported image produces raw OCR text.

---

### Phase 4 — Parser Upgrade

Goal: Convert OCR text into structured receipt data.

Tasks:

- Improve date parsing.
- Improve voucher number parsing.
- Improve broker parsing.
- Improve farmer/supplier parsing.
- Improve line item parsing.
- Improve deduction parsing:
  - Commission
  - Damages
  - Unloading
  - Advance/cash
  - Other deductions
- Add confidence scores.
- Add parser test suite.

Deliverable:

OCR text produces structured `ParsedReceiptData`.

---

### Phase 5 — Mapping Parser Output to Receipt Form

Goal: Auto-fill receipt entry.

Tasks:

- Convert `ParsedReceiptData` into:
  - `Receipt`
  - `ReceiptLineItem`
  - `Deduction`
- Add manual override UI.
- Add confidence warnings.
- Add low-confidence field highlighting.
- Save original parser JSON.

Deliverable:

User captures receipt → OCR → parser → editable receipt form.

---

### Phase 6 — Validation Screen

Goal: Implement human-in-the-loop workflow.

Tasks:

- Show image + OCR text + parsed fields.
- Allow correction of all fields.
- Save original parser result.
- Save corrected result.
- Store validation snapshot.
- Add validation status.
- Add retry/re-OCR/re-parse actions.

Deliverable:

App behaves like the web app's validation workflow.

---

### Phase 7 — Batch and Queue System

Goal: Support bulk processing.

Tasks:

- Add `BatchEntity`.
- Add `ReceiptJobEntity`.
- Add job statuses.
- Add WorkManager workers:
  - Crop job
  - OCR job
  - Parse job
- Add queue screen.
- Add retry/delete/reprocess actions.
- Add batch summary.

Deliverable:

User can import multiple receipts and process them as a queue.

---

### Phase 8 — Smart Crop

Goal: Remove background/noise from receipt images.

Tasks:

- Add crop box model.
- Add crop UI with draggable rectangle.
- Add auto-crop algorithm.
- Store manual crop corrections.
- Train smart crop thresholds from corrections.
- Add confidence score.

Implemented in this iteration:

- Added `CropBox` and `SmartCropProfile` domain models.
- Added crop use cases and JSON persistence helpers.
- Added `SmartCropScreen` and `SmartCropViewModel`.
- Added lightweight content-based auto-crop heuristic and manual crop override persistence.
- Added `ReceiptJobStatus.CROPPED`, crop-box JSON, crop confidence, and batch count refreshes.

Still pending:

- WorkManager crop worker.
- Actual cropped image file output and OCR consumption of crop boxes.
- Persistent smart-crop threshold learning from manual corrections.

Deliverable:

App can crop receipt images before OCR.

---

### Phase 9 — Reports

Goal: Add operational reporting.

Tasks:

- Fix multi-line deduction duplication.
- Add report use cases.
- Add report UI:
  - Farmer summaries
  - Broker settlements
  - Monthly trends
  - Deduction analysis
- Add date range filters.
- Add export actions.

Deliverable:

User can view meaningful business reports.

---

### Phase 10 — Export/Share

Goal: Enable PDF/Excel/CSV/share.

Tasks:

- Add PDF receipt export.
- Add batch PDF export.
- Add Excel/CSV export.
- Add Android share sheet.
- Add export history.
- Add export templates.

Deliverable:

User can export finalized receipts and reports.

---

### Phase 11 — Supplier Management

Goal: Support supplier aliases and fuzzy matching.

Tasks:

- Add supplier entity.
- Add aliases.
- Add supplier search.
- Add alias suggestions.
- Add OCR supplier name matching.
- Add merge suggestions.
- Avoid hardcoded supplier parsing rules.

Deliverable:

Supplier names from OCR can be matched reliably.

---

### Phase 12 — ML Feedback Loop

Goal: Learn from user corrections.

Tasks:

- Store parser original/corrected snapshots.
- Build character confusion matrix.
- Build fuzzy anchor rules.
- Store smart crop corrections.
- Add training workers.
- Add learning history.
- Add training dashboard.

Rules:

- Keep ML rules data-driven.
- Do not hardcode supplier-specific logic.
- Store model artifacts as JSON.
- Run heavy training only when safe:
  - Wi-Fi
  - Charging
  - Idle device

Deliverable:

Parser improves over time based on corrections.

---

### Phase 13 — Sync With Backend/Web App

Goal: Sync Android with the web app.

Tasks:

- Add backend API client.
- Add sync outbox.
- Add sync status.
- Add conflict handling.
- Sync:
  - Batches
  - Receipts
  - Validation snapshots
  - Supplier aliases
  - ML corrections
- Add manual sync.
- Add retry failed sync.

Deliverable:

Android becomes an offline mobile extension of the web platform.

---

## 9. Priority Order

Recommended implementation order:

1. Architecture cleanup and migrations
2. Camera/gallery import
3. OCR text extraction
4. Parser improvement
5. Auto-fill receipt form
6. Validation screen
7. Batch queue
8. Smart crop
9. Reports/export
10. Supplier management
11. ML feedback loop
12. Backend sync

Highest-value first milestone:

```text
Capture/import image → OCR → parse → validation screen → save receipt
```

This milestone transforms the app from static CRUD into a functional receipt OCR product.

---

## 10. Success Criteria for Iterations

An iteration is considered successful only when:

1. Intended feature code is implemented.
2. UI flow is usable in the Android emulator or device.
3. Build passes.
4. Relevant tests pass, if tests exist for the changed area.
5. No destructive data-loss migration is introduced.
6. Progress log in this document is updated.

---

## 11. Progress Log

Update this section after every successful iteration.

### Iteration 0 — Baseline Planner Created

**Date:** 2026-06-14  
**Status:** Completed  
**Scope:** Created broader scope planner for Android app.  
**Summary:**
- Documented current app baseline.
- Documented target navigation layout.
- Documented screen-by-screen layout.
- Documented proposed module architecture.
- Documented new database schema plan.
- Documented implementation phases and priority order.

**Files touched:**
- `Receipt Management\BROADER_SCOPE_PLANNER.md`

**Next iteration:**
- Phase 1 — Architecture cleanup and migration planning.

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
**Scope:** Improved receipt parser, added parser confidence, added parsed-data mapping, and wired parsed OCR output into the editable receipt form.
**Summary:**
- Moved receipt parsing into `core:domain:parser`.
- Added confidence scores and field-level confidence to `ParsedReceiptData`.
- Improved extraction of broker details, voucher number/date, supplier/farmer code, line items, and deductions.
- Added `ParsedReceiptMapper` for converting parsed data into receipt, line item, and deduction domain objects.
- Added parser unit tests in `core:domain`.
- Updated receipt entry UI to show parsed summary and explicitly apply parsed data to form fields.
- Kept parsed-data application explicit to avoid silently overwriting user-corrected fields.

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

---

## 12. Instructions for Future AI Development Sessions

Before starting work:

1. Read this planner.
2. Read `App AI Context.md`.
3. Check the current progress log below.
4. Continue from the latest completed iteration.
5. Do not skip architectural cleanup if it affects the current task.

After each successful iteration:

1. Update the **Progress Log** section.
2. Add:
   - Iteration number
   - Date
   - Status
   - Scope
   - Summary
   - Files touched
   - Verification performed
   - Next iteration
3. If the iteration changes architecture, routes, schema, or major endpoints, update both:
   - This planner
   - `Receipt Management\AI_CONTEXT.md`

Do not update the progress log for failed or abandoned attempts. Only update it after a successful, buildable iteration.
