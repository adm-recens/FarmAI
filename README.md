# FarmAI

FarmAI is a modular Android application for managing farmer, broker, and receipt workflows for Indian farm produce sales. The current build focuses on local-first receipt and ledger management with Room storage, Jetpack Compose UI, Hilt dependency injection, and Clean Architecture-style domain/data separation.

## Project status

The debug APK currently builds successfully:

```powershell
.\gradlew :app:assembleDebug --rerun-tasks --no-build-cache
```

Result:

```text
BUILD SUCCESSFUL
```

The app is usable for local farmer, broker, and receipt CRUD flows, but several planned capabilities are still incomplete or backend-only.

## Modules

The project is a multi-module Gradle project.

| Module | Purpose |
|---|---|
| `:app` | Application entry point, Compose navigation, Hilt app graph, sample data seeding |
| `:core:domain` | Domain models, repository interfaces, use cases |
| `:core:data` | Room database, DAOs, entities, repository implementations, OCR text parser |
| `:feature:farmer` | Farmer list/detail/edit UI and ViewModels |
| `:feature:broker` | Broker list/detail/edit UI and ViewModels |
| `:feature:receipt` | Receipt list/detail/entry UI, ViewModels, OCR parsing integration points |

Defined in `settings.gradle.kts:19-24`.

## Build setup

Important versions are centralized in `gradle/libs.versions.toml`.

| Item | Version |
|---|---|
| Android Gradle Plugin | `8.13.2` |
| Kotlin | `1.9.22` |
| Compose BOM | `2024.05.00` |
| Compose compiler | `1.5.10` |
| Room | `2.6.1` |
| Hilt | `2.48.1` |
| Navigation Compose | `2.7.7` |
| CameraX | `1.4.0-alpha02` |
| ML Kit Text Recognition | `16.0.0` |

SDK targets:

| Item | Value |
|---|---|
| `compileSdk` | `34` |
| `targetSdk` | `34` |
| `minSdk` | `24` |
| JVM target | `17` |

## Architecture

### Layers

| Layer | Location | Notes |
|---|---|---|
| Presentation | `feature/*/src/main/java/com/farmai/feature/.../ui` and `.../viewmodel` | Jetpack Compose, Material 3, StateFlow-backed ViewModels |
| Domain | `core/domain/src/main/java/com/farmai/core/domain` | Models, repository interfaces, use cases |
| Data | `core/data/src/main/java/com/farmai/core/data` | Room, DAOs, repository implementations |
| App | `app/src/main/java/com/farmai/app` | Navigation host, MainActivity, Hilt application, sample data |

### Dependency injection

- App Hilt entry point: `app/src/main/java/com/farmai/app/FarmAIApplication.kt`
- Database and repository bindings: `core/data/src/main/java/com/farmai/core/data/di/DatabaseModule.kt`
- Use cases use constructor injection with `@Inject`, so the feature DI modules are intentionally empty placeholders:
  - `feature/farmer/.../di/FarmerFeatureModule.kt`
  - `feature/broker/.../di/BrokerFeatureModule.kt`
  - `feature/receipt/.../di/ReceiptFeatureModule.kt`

Note: feature modules currently depend on `core:data`, so the architecture is modular but not strictly layered.

## Navigation

Navigation is defined in `app/src/main/java/com/farmai/app/navigation/FarmAINavHost.kt`.

| Route | Screen | Status |
|---|---|---|
| `home` | `HomeScreen` | Start destination |
| `farmers` | `FarmerListScreen` | Implemented |
| `farmer/add` | `FarmerDetailScreen` | Implemented |
| `farmer/edit/{farmerId}` | `FarmerDetailScreen` | Route exists |
| `brokers` | `BrokerListScreen` | Implemented |
| `broker/add` | `BrokerDetailScreen` | Implemented |
| `broker/edit/{brokerId}` | `BrokerDetailScreen` | Route exists |
| `receipts` | `ReceiptListScreen` | Implemented |
| `receipt/add` | `ReceiptEntryScreen` | Implemented |
| `receipt/{receiptId}` | `ReceiptDetailScreen` | Implemented |

Known navigation gaps:

- `FarmerListScreen` navigates to `farmer/{farmer.id}`, but that route is not defined.
- `BrokerListScreen` navigates to `broker/{broker.id}`, but that route is not defined.
- `ReceiptListScreen` navigates to `receipt/edit/{receipt.id}`, but no receipt edit route exists.

## Current features

### Farmer management

Implemented:

- List farmers
- Search farmers
- Add farmer
- Edit farmer route
- Delete farmer

Key files:

- `feature/farmer/src/main/java/com/farmai/feature/farmer/ui/FarmerListScreen.kt`
- `feature/farmer/src/main/java/com/farmai/feature/farmer/ui/FarmerDetailScreen.kt`
- `feature/farmer/src/main/java/com/farmai/feature/farmer/viewmodel/FarmerViewModel.kt`

### Broker management

Implemented:

- List brokers
- Search brokers
- Add broker
- Edit broker route
- Delete broker

Key files:

- `feature/broker/src/main/java/com/farmai/feature/broker/ui/BrokerListScreen.kt`
- `feature/broker/src/main/java/com/farmai/feature/broker/ui/BrokerDetailScreen.kt`
- `feature/broker/src/main/java/com/farmai/feature/broker/viewmodel/BrokerViewModel.kt`

### Receipt management

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

Key files:

- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptListScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptEntryScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/ui/ReceiptDetailScreen.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptViewModel.kt`

Receipt entry stores `farmerId = farmerCode` and `brokerId = brokerName`. This should be improved later by selecting saved farmers and brokers by ID.

### Reports

Implemented in the data/domain layer:

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

Key files:

- `core/data/src/main/java/com/farmai/core/data/local/dao/ReportDao.kt`
- `core/data/src/main/java/com/farmai/core/data/repository/ReportRepositoryImpl.kt`
- `core/domain/src/main/java/com/farmai/core/domain/usecase/report/ReportUseCases.kt`

Important limitation: report queries join receipts, line items, and deductions directly. For multi-line receipts, deduction sums can be multiplied by the number of line items.

## OCR and image processing status

The project includes CameraX and ML Kit dependencies, but runtime camera capture and image OCR are not wired yet.

Current OCR behavior:

- `parseReceiptImage(imagePath)` returns an empty `ParsedReceiptData()` placeholder.
- `parseReceiptText(rawText)` parses pasted receipt text.
- Parsed OCR data is not yet converted into the receipt form fields.

Key files:

- `core/data/src/main/java/com/farmai/core/data/repository/ReceiptRepositoryImpl.kt`
- `feature/receipt/src/main/java/com/farmai/feature/receipt/viewmodel/ReceiptViewModel.kt`

## Data model

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

- `DRAFT`
- `CONFIRMED`
- `SYNCED`

`SYNCED` exists in the domain model, but no sync/network implementation currently exists.

## Database

Room database: `farmai_database`

Database configuration:

- Version: `1`
- Export schema: disabled
- Destructive migration fallback enabled

Key file: `core/data/src/main/java/com/farmai/core/data/local/AppDatabase.kt`

Tables:

| Table | Primary entity |
|---|---|
| `farmers` | `FarmerEntity` |
| `brokers` | `BrokerEntity` |
| `receipts` | `ReceiptEntity` |
| `receipt_line_items` | `ReceiptLineItemEntity` |
| `deductions` | `DeductionEntity` |

Receipt schema summary:

```text
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
```

Line item schema summary:

```text
receipt_line_items (
  id PK,
  receiptId FK cascade,
  quantity,
  pricePerUnit,
  amount,
  grade,
  sortOrder
)
```

Deduction schema summary:

```text
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

## Sample data

The app seeds sample data on startup if no farmers exist.

Seeded data:

- 1 broker: `AHMED SHARIF & BROS`
- 5 farmers: `VK`, `UMA/G`, `DPH`, `GRD`, `TNM`
- 5 sample receipts: voucher numbers `142` to `146`

Key file: `app/src/main/java/com/farmai/app/data/SampleDataSeeder.kt`

## Receipt domain context

Receipts are based on lemon sale payment receipts from Hyderabad-style broker transactions.

Common fields:

| Field | Example |
|---|---|
| Broker | AHMED SHARIF & BROS |
| Voucher number | 142, 143, 144 |
| Voucher date | 01/01/2026 |
| Farmer code | VK, UMA/G, DPH, GRD, TNM |
| Line item | Qty x Price = Amount |
| Commission | 4% |
| Damages | Deduction |
| Unloading | Deduction |
| Advance / cash | Deduction |
| Grand total | Net payable |

## Known limitations

- Camera capture is not implemented.
- Image OCR is not implemented.
- Pasted OCR text parsing does not auto-fill the receipt form.
- PDF export button is a placeholder.
- Share button is a placeholder.
- Excel export is not implemented.
- Reports dashboard is not implemented.
- Sync is not implemented.
- Localization currently only has default English strings.
- Unit and instrumented tests are not implemented.
- CI/CD workflow is not implemented.
- Receipt entry does not validate required fields before saving.
- Receipt date text is not parsed before saving; save currently uses `System.currentTimeMillis()`.
- Report deduction totals may be inflated for multi-line receipts.

## Getting started

### Open

```bash
cd FarmAI
```

Open the project in Android Studio.

### Build debug APK

```powershell
.\gradlew :app:assembleDebug
```

### Clean build

```powershell
.\gradlew clean :app:assembleDebug --rerun-tasks --no-build-cache
```

### Run tests

```powershell
.\gradlew test
```

### Run instrumented tests

Requires a connected device or emulator:

```powershell
.\gradlew connectedAndroidTest
```

## Requirements

- Android Studio Ladybug (`2024.2.1`) or later
- JDK 17
- Android SDK 34
- Physical Android device recommended for future CameraX testing

## Permissions

The manifest declares camera and storage permissions for planned capture/export flows:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

Runtime camera permission handling is not implemented yet.

## Key source paths

```text
app/src/main/java/com/farmai/app/
  MainActivity.kt
  FarmAIApplication.kt
  navigation/FarmAINavHost.kt
  navigation/HomeScreen.kt
  data/SampleDataSeeder.kt
  di/AppModule.kt

core/domain/src/main/java/com/farmai/core/domain/
  model/
  repository/
  usecase/

core/data/src/main/java/com/farmai/core/data/
  local/AppDatabase.kt
  local/dao/
  local/entity/
  di/DatabaseModule.kt
  repository/

feature/farmer/src/main/java/com/farmai/feature/farmer/
  ui/
  viewmodel/

feature/broker/src/main/java/com/farmai/feature/broker/
  ui/
  viewmodel/

feature/receipt/src/main/java/com/farmai/feature/receipt/
  ui/
  viewmodel/
```

## Contribution notes

- Keep feature modules focused on UI and ViewModels.
- Put business rules in `core/domain`.
- Put persistence and external integrations in `core/data`.
- Prefer StateFlow in ViewModels.
- Add use-case tests before changing repository behavior.
- Update this README when adding major routes, schemas, or feature limitations.

## Build variants

- `debug` - development build
- `release` - release build; minification is currently disabled

---

Last updated: June 2026
