package com.farmai.core.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS batches (
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    status TEXT NOT NULL,
                    totalImages INTEGER NOT NULL DEFAULT 0,
                    processedCount INTEGER NOT NULL DEFAULT 0,
                    validatedCount INTEGER NOT NULL DEFAULT 0,
                    failedCount INTEGER NOT NULL DEFAULT 0,
                    notes TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_batches_status ON batches(status)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_batches_createdAt ON batches(createdAt)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_batches_updatedAt ON batches(updatedAt)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS receipt_jobs (
                    id TEXT NOT NULL,
                    batchId TEXT NOT NULL,
                    receiptId TEXT,
                    status TEXT NOT NULL,
                    imagePath TEXT,
                    cropBoxJson TEXT,
                    ocrRawText TEXT,
                    ocrLayoutJson TEXT,
                    parserJson TEXT,
                    confidenceScore REAL NOT NULL DEFAULT 0.0,
                    error TEXT,
                    attemptCount INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(id),
                    FOREIGN KEY(batchId) REFERENCES batches(id) ON DELETE CASCADE,
                    FOREIGN KEY(receiptId) REFERENCES receipts(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_receipt_jobs_batchId ON receipt_jobs(batchId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_receipt_jobs_receiptId ON receipt_jobs(receiptId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_receipt_jobs_status ON receipt_jobs(status)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_receipt_jobs_createdAt ON receipt_jobs(createdAt)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE receipts ADD COLUMN validationStatus TEXT NOT NULL DEFAULT 'PENDING'")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_receipts_validationStatus ON receipts(validationStatus)")
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS validation_snapshots (
                    id TEXT NOT NULL,
                    receiptId TEXT NOT NULL,
                    originalJson TEXT NOT NULL,
                    correctedJson TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    createdBy TEXT NOT NULL DEFAULT 'local-user',
                    source TEXT NOT NULL DEFAULT 'manual-validation',
                    PRIMARY KEY(id),
                    FOREIGN KEY(receiptId) REFERENCES receipts(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            database.execSQL("CREATE INDEX IF NOT EXISTS index_validation_snapshots_receiptId ON validation_snapshots(receiptId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_validation_snapshots_createdAt ON validation_snapshots(createdAt)")
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
