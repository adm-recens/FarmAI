package com.farmai.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.farmai.core.data.local.entity.BrokerEntity
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.FarmerEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("""
        SELECT 
            f.id as farmerId,
            f.name as farmerName,
            f.code as farmerCode,
            COALESCE(SUM(rli.quantity), 0) as totalQuantity,
            COALESCE(SUM(rli.amount), 0) as totalGrossAmount,
            COALESCE(SUM(d.amount), 0) as totalDeductions,
            COALESCE(SUM(rli.amount) - SUM(d.amount), 0) as totalNetAmount,
            CASE WHEN SUM(rli.quantity) > 0 THEN SUM(rli.amount) / SUM(rli.quantity) ELSE 0 END as averagePrice,
            COUNT(DISTINCT r.id) as receiptCount
        FROM farmers f
        LEFT JOIN receipts r ON r.farmerId = f.id AND (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
        LEFT JOIN deductions d ON d.receiptId = r.id
        GROUP BY f.id, f.name, f.code
        HAVING receiptCount > 0
        ORDER BY totalNetAmount DESC
    """)
    fun getFarmerSummaries(startDate: Long?, endDate: Long?): Flow<List<FarmerSummaryEntity>>

    @Query("""
        SELECT 
            b.id as brokerId,
            b.name as brokerName,
            COALESCE(SUM(rli.quantity), 0) as totalQuantity,
            COALESCE(SUM(rli.amount), 0) as totalGrossAmount,
            COALESCE(SUM(CASE WHEN d.type = 'COMMISSION' THEN d.amount ELSE 0 END), 0) as totalCommission,
            COALESCE(SUM(rli.amount) - SUM(d.amount), 0) as totalNetToFarmers,
            COUNT(DISTINCT r.id) as receiptCount
        FROM brokers b
        LEFT JOIN receipts r ON r.brokerId = b.id AND (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
        LEFT JOIN deductions d ON d.receiptId = r.id
        GROUP BY b.id, b.name
        HAVING receiptCount > 0
        ORDER BY totalGrossAmount DESC
    """)
    fun getBrokerSettlements(startDate: Long?, endDate: Long?): Flow<List<BrokerSettlementEntity>>

    @Query("""
        SELECT 
            strftime('%Y-%m', datetime(r.voucherDate / 1000, 'unixepoch')) as month,
            CAST(strftime('%Y', datetime(r.voucherDate / 1000, 'unixepoch')) AS INTEGER) as year,
            COALESCE(SUM(rli.quantity), 0) as totalQuantity,
            COALESCE(SUM(rli.amount), 0) as totalGrossAmount,
            COALESCE(SUM(rli.amount) - SUM(d.amount), 0) as totalNetAmount,
            COUNT(DISTINCT r.id) as receiptCount
        FROM receipts r
        LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
        LEFT JOIN deductions d ON d.receiptId = r.id
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY strftime('%Y-%m', datetime(r.voucherDate / 1000, 'unixepoch'))
        ORDER BY year DESC, month DESC
    """)
    fun getMonthlyTrends(startDate: Long?, endDate: Long?): Flow<List<MonthlyTrendEntity>>

    @Query("""
        SELECT 
            d.type as type,
            COALESCE(SUM(d.amount), 0) as totalAmount,
            COUNT(*) as count,
            COALESCE(SUM(d.amount) * 100.0 / NULLIF((SELECT SUM(amount) FROM deductions WHERE receiptId IN (SELECT id FROM receipts WHERE (:startDate IS NULL OR voucherDate >= :startDate) AND (:endDate IS NULL OR voucherDate <= :endDate))), 0), 0) as percentageOfTotal
        FROM deductions d
        JOIN receipts r ON r.id = d.receiptId
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY d.type
        ORDER BY totalAmount DESC
    """)
    fun getDeductionAnalysis(startDate: Long?, endDate: Long?): Flow<List<DeductionAnalysisEntity>>

    @Query("""
        SELECT 
            r.voucherNumber,
            date(r.voucherDate / 1000, 'unixepoch') as voucherDate,
            f.code as farmerCode,
            f.name as farmerName,
            b.name as brokerName,
            COALESCE(SUM(rli.quantity), 0) as quantity,
            COALESCE(SUM(rli.amount), 0) as grossAmount,
            COALESCE(SUM(CASE WHEN d.type = 'COMMISSION' THEN d.amount ELSE 0 END), 0) as commission,
            COALESCE(SUM(CASE WHEN d.type = 'DAMAGES' THEN d.amount ELSE 0 END), 0) as damages,
            COALESCE(SUM(CASE WHEN d.type = 'UNLOADING' THEN d.amount ELSE 0 END), 0) as unloading,
            COALESCE(SUM(CASE WHEN d.type = 'ADVANCE' THEN d.amount ELSE 0 END), 0) as advance,
            COALESCE(SUM(CASE WHEN d.type = 'OTHER' THEN d.amount ELSE 0 END), 0) as otherDeductions,
            COALESCE(SUM(rli.amount) - SUM(d.amount), 0) as netAmount
        FROM receipts r
        JOIN farmers f ON f.id = r.farmerId
        JOIN brokers b ON b.id = r.brokerId
        LEFT JOIN receipt_line_items rli ON rli.receiptId = r.id
        LEFT JOIN deductions d ON d.receiptId = r.id
        WHERE (:startDate IS NULL OR r.voucherDate >= :startDate) AND (:endDate IS NULL OR r.voucherDate <= :endDate)
        GROUP BY r.id, r.voucherNumber, r.voucherDate, f.code, f.name, b.name
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
