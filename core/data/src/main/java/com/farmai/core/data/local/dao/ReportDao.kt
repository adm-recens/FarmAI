package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("""
        WITH receipt_amounts AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(rli.quantity), 0) AS totalQuantity,
                COALESCE(SUM(rli.amount), 0) AS totalGrossAmount
            FROM receipts r
            LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        ), receipt_deductions AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(d.amount), 0) AS totalDeductions
            FROM receipts r
            LEFT JOIN deductions d ON d.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        )
        SELECT
            f.id as farmerId,
            f.name as farmerName,
            f.code as farmerCode,
            COALESCE(SUM(ra.totalQuantity), 0) as totalQuantity,
            COALESCE(SUM(ra.totalGrossAmount), 0) as totalGrossAmount,
            COALESCE(SUM(rd.totalDeductions), 0) as totalDeductions,
            COALESCE(SUM(ra.totalGrossAmount) - SUM(rd.totalDeductions), 0) as totalNetAmount,
            CASE WHEN SUM(ra.totalQuantity) > 0 THEN SUM(ra.totalGrossAmount) / SUM(ra.totalQuantity) ELSE 0 END as averagePrice,
            COUNT(DISTINCT r.id) as receiptCount
        FROM farmers f
        JOIN receipts r ON r.farmerId = f.id AND (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        LEFT JOIN receipt_amounts ra ON ra.receiptId = r.id
        LEFT JOIN receipt_deductions rd ON rd.receiptId = r.id
        GROUP BY f.id, f.name, f.code
        HAVING COUNT(DISTINCT r.id) > 0
        ORDER BY totalNetAmount DESC
    """)
    fun getFarmerSummaries(startDate: Long?, endDate: Long?): Flow<List<FarmerSummaryEntity>>

    @Query("""
        WITH receipt_amounts AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(rli.quantity), 0) AS totalQuantity,
                COALESCE(SUM(rli.amount), 0) AS totalGrossAmount
            FROM receipts r
            LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        ), receipt_deductions AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(d.amount), 0) AS totalDeductions,
                COALESCE(SUM(CASE WHEN d.type = 'COMMISSION' THEN d.amount ELSE 0 END), 0) AS totalCommission
            FROM receipts r
            LEFT JOIN deductions d ON d.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        )
        SELECT
            b.id as brokerId,
            b.name as brokerName,
            COALESCE(SUM(ra.totalQuantity), 0) as totalQuantity,
            COALESCE(SUM(ra.totalGrossAmount), 0) as totalGrossAmount,
            COALESCE(SUM(rd.totalCommission), 0) as totalCommission,
            COALESCE(SUM(ra.totalGrossAmount) - SUM(rd.totalDeductions), 0) as totalNetToFarmers,
            COUNT(DISTINCT r.id) as receiptCount
        FROM brokers b
        JOIN receipts r ON r.brokerId = b.id AND (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        LEFT JOIN receipt_amounts ra ON ra.receiptId = r.id
        LEFT JOIN receipt_deductions rd ON rd.receiptId = r.id
        GROUP BY b.id, b.name
        HAVING COUNT(DISTINCT r.id) > 0
        ORDER BY totalGrossAmount DESC
    """)
    fun getBrokerSettlements(startDate: Long?, endDate: Long?): Flow<List<BrokerSettlementEntity>>

    @Query("""
        WITH receipt_amounts AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(rli.quantity), 0) AS totalQuantity,
                COALESCE(SUM(rli.amount), 0) AS totalGrossAmount
            FROM receipts r
            LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        ), receipt_deductions AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(d.amount), 0) AS totalDeductions
            FROM receipts r
            LEFT JOIN deductions d ON d.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        )
        SELECT
            strftime('%Y-%m', datetime(r.voucherDate / 1000, 'unixepoch')) as month,
            CAST(strftime('%Y', datetime(r.voucherDate / 1000, 'unixepoch')) AS INTEGER) as year,
            COALESCE(SUM(ra.totalQuantity), 0) as totalQuantity,
            COALESCE(SUM(ra.totalGrossAmount), 0) as totalGrossAmount,
            COALESCE(SUM(ra.totalGrossAmount) - SUM(rd.totalDeductions), 0) as totalNetAmount,
            COUNT(DISTINCT r.id) as receiptCount
        FROM receipts r
        LEFT JOIN receipt_amounts ra ON ra.receiptId = r.id
        LEFT JOIN receipt_deductions rd ON rd.receiptId = r.id
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY strftime('%Y-%m', datetime(r.voucherDate / 1000, 'unixepoch'))
        ORDER BY year DESC, month DESC
    """)
    fun getMonthlyTrends(startDate: Long?, endDate: Long?): Flow<List<MonthlyTrendEntity>>

    @Query("""
        WITH totalDeductions AS (
            SELECT COALESCE(SUM(d.amount), 0) AS amount
            FROM deductions d
            JOIN receipts r ON r.id = d.receiptId
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        )
        SELECT
            d.type as type,
            COALESCE(SUM(d.amount), 0) as totalAmount,
            COUNT(*) as count,
            COALESCE(SUM(d.amount) * 100.0 / NULLIF((SELECT amount FROM totalDeductions), 0), 0) as percentageOfTotal
        FROM deductions d
        JOIN receipts r ON r.id = d.receiptId
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY d.type
        ORDER BY totalAmount DESC
    """)
    fun getDeductionAnalysis(startDate: Long?, endDate: Long?): Flow<List<DeductionAnalysisEntity>>

    @Query("""
        WITH receipt_amounts AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(rli.quantity), 0) AS totalQuantity,
                COALESCE(SUM(rli.amount), 0) AS totalGrossAmount
            FROM receipts r
            LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        ), receipt_deductions AS (
            SELECT
                r.id AS receiptId,
                COALESCE(SUM(d.amount), 0) AS totalDeductions,
                COALESCE(SUM(CASE WHEN d.type = 'COMMISSION' THEN d.amount ELSE 0 END), 0) AS commission,
                COALESCE(SUM(CASE WHEN d.type = 'DAMAGES' THEN d.amount ELSE 0 END), 0) AS damages,
                COALESCE(SUM(CASE WHEN d.type = 'UNLOADING' THEN d.amount ELSE 0 END), 0) AS unloading,
                COALESCE(SUM(CASE WHEN d.type = 'ADVANCE' THEN d.amount ELSE 0 END), 0) AS advance,
                COALESCE(SUM(CASE WHEN d.type = 'OTHER' THEN d.amount ELSE 0 END), 0) AS otherDeductions
            FROM receipts r
            LEFT JOIN deductions d ON d.receiptId = r.id
            WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
            GROUP BY r.id
        )
        SELECT
            r.voucherNumber,
            date(r.voucherDate / 1000, 'unixepoch') as voucherDate,
            f.code as farmerCode,
            f.name as farmerName,
            b.name as brokerName,
            COALESCE(ra.totalQuantity, 0) as quantity,
            COALESCE(ra.totalGrossAmount, 0) as grossAmount,
            COALESCE(rd.commission, 0) as commission,
            COALESCE(rd.damages, 0) as damages,
            COALESCE(rd.unloading, 0) as unloading,
            COALESCE(rd.advance, 0) as advance,
            COALESCE(rd.otherDeductions, 0) as otherDeductions,
            COALESCE(ra.totalGrossAmount - rd.totalDeductions, 0) as netAmount
        FROM receipts r
        JOIN farmers f ON f.id = r.farmerId
        JOIN brokers b ON b.id = r.brokerId
        LEFT JOIN receipt_amounts ra ON ra.receiptId = r.id
        LEFT JOIN receipt_deductions rd ON rd.receiptId = r.id
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY r.id, r.voucherNumber, r.voucherDate, f.code, f.name, b.name, ra.totalQuantity, ra.totalGrossAmount, rd.totalDeductions, rd.commission, rd.damages, rd.unloading, rd.advance, rd.otherDeductions
        ORDER BY r.voucherDate DESC
    """)
    fun getAllReceiptsForExport(startDate: Long?, endDate: Long?): Flow<List<ReceiptExportRowEntity>>
}

data class FarmerSummaryEntity(
    val farmerId: String,
    val farmerName: String,
    val farmerCode: String,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalDeductions: Double,
    val totalNetAmount: Double,
    val averagePrice: Double,
    val receiptCount: Int
)

data class BrokerSettlementEntity(
    val brokerId: String,
    val brokerName: String,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalCommission: Double,
    val totalNetToFarmers: Double,
    val receiptCount: Int
)

data class MonthlyTrendEntity(
    val month: String,
    val year: Int,
    val totalQuantity: Double,
    val totalGrossAmount: Double,
    val totalNetAmount: Double,
    val receiptCount: Int
)

data class DeductionAnalysisEntity(
    val type: String,
    val totalAmount: Double,
    val count: Int,
    val percentageOfTotal: Double
)

data class ReceiptExportRowEntity(
    val voucherNumber: String,
    val voucherDate: String,
    val farmerCode: String,
    val farmerName: String,
    val brokerName: String,
    val quantity: Double,
    val grossAmount: Double,
    val commission: Double,
    val damages: Double,
    val unloading: Double,
    val advance: Double,
    val otherDeductions: Double,
    val netAmount: Double
)
