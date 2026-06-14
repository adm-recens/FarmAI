package com.farmai.app.data

import android.content.Context
import com.farmai.core.data.local.AppDatabase
import com.farmai.core.data.local.entity.BrokerEntity
import com.farmai.core.data.local.entity.DeductionEntity
import com.farmai.core.data.local.entity.FarmerEntity
import com.farmai.core.data.local.entity.ReceiptEntity
import com.farmai.core.data.local.entity.ReceiptLineItemEntity
import com.farmai.core.domain.model.ReceiptStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Locale

object SampleDataSeeder {
    fun seed(context: Context) {
        val database = AppDatabase.getInstance(context)
        runBlocking {
            val farmerCount = database.farmerDao().getAllFarmers().first().size
            if (farmerCount > 0) return@runBlocking

            val broker = BrokerEntity(
                id = "BROKER_AHMED_SHARIF",
                name = "AHMED SHARIF & BROS",
                address = "DARUSHAFA X ROAD, HYDERABAD 500024",
                phone = "040-24412139, 9949333786",
                defaultCommissionPercent = 4.0
            )
            database.brokerDao().insertBroker(broker)

            val farmers = listOf(
                FarmerEntity(id = "FARMER_VK", name = "Venkatesh Kumar", code = "VK", village = "Sample Village 1"),
                FarmerEntity(id = "FARMER_UMAG", name = "Uma G", code = "UMA/G", village = "Sample Village 2"),
                FarmerEntity(id = "FARMER_DPH", name = "Dinesh P H", code = "DPH", village = "Sample Village 3"),
                FarmerEntity(id = "FARMER_GRD", name = "Ganesh R D", code = "GRD", village = "Sample Village 4"),
                FarmerEntity(id = "FARMER_TNM", name = "Thirupathi N M", code = "TNM", village = "Sample Village 5")
            )
            database.farmerDao().insertAllFarmers(farmers)

            val receipts = listOf(
                ReceiptEntity(
                    id = "RECEIPT_142",
                    farmerId = "FARMER_VK",
                    brokerId = "BROKER_AHMED_SHARIF",
                    voucherNumber = "142",
                    voucherDate = parseDate("01/01/2026"),
                    status = ReceiptStatus.CONFIRMED.name,
                    createdAt = parseDate("01/01/2026")
                ),
                ReceiptEntity(
                    id = "RECEIPT_143",
                    farmerId = "FARMER_UMAG",
                    brokerId = "BROKER_AHMED_SHARIF",
                    voucherNumber = "143",
                    voucherDate = parseDate("01/01/2026"),
                    status = ReceiptStatus.CONFIRMED.name,
                    createdAt = parseDate("01/01/2026")
                ),
                ReceiptEntity(
                    id = "RECEIPT_144",
                    farmerId = "FARMER_DPH",
                    brokerId = "BROKER_AHMED_SHARIF",
                    voucherNumber = "144",
                    voucherDate = parseDate("01/01/2026"),
                    status = ReceiptStatus.CONFIRMED.name,
                    createdAt = parseDate("01/01/2026")
                ),
                ReceiptEntity(
                    id = "RECEIPT_145",
                    farmerId = "FARMER_GRD",
                    brokerId = "BROKER_AHMED_SHARIF",
                    voucherNumber = "145",
                    voucherDate = parseDate("01/01/2026"),
                    status = ReceiptStatus.CONFIRMED.name,
                    createdAt = parseDate("01/01/2026")
                ),
                ReceiptEntity(
                    id = "RECEIPT_146",
                    farmerId = "FARMER_TNM",
                    brokerId = "BROKER_AHMED_SHARIF",
                    voucherNumber = "146",
                    voucherDate = parseDate("01/01/2026"),
                    status = ReceiptStatus.CONFIRMED.name,
                    createdAt = parseDate("01/01/2026")
                )
            )
            database.receiptDao().insertAllReceipts(receipts)

            val lineItems = listOf(
                ReceiptLineItemEntity(id = "LINE_142_1", receiptId = "RECEIPT_142", quantity = 1.0, pricePerUnit = 350.0, amount = 350.0, sortOrder = 0),
                ReceiptLineItemEntity(id = "LINE_143_1", receiptId = "RECEIPT_143", quantity = 1.0, pricePerUnit = 350.0, amount = 350.0, sortOrder = 0),
                ReceiptLineItemEntity(id = "LINE_143_2", receiptId = "RECEIPT_143", quantity = 2.0, pricePerUnit = 300.0, amount = 600.0, sortOrder = 1),
                ReceiptLineItemEntity(id = "LINE_143_3", receiptId = "RECEIPT_143", quantity = 1.0, pricePerUnit = 260.0, amount = 260.0, sortOrder = 2),
                ReceiptLineItemEntity(id = "LINE_144_1", receiptId = "RECEIPT_144", quantity = 2.0, pricePerUnit = 200.0, amount = 400.0, sortOrder = 0),
                ReceiptLineItemEntity(id = "LINE_145_1", receiptId = "RECEIPT_145", quantity = 4.0, pricePerUnit = 300.0, amount = 1200.0, sortOrder = 0),
                ReceiptLineItemEntity(id = "LINE_146_1", receiptId = "RECEIPT_146", quantity = 1.0, pricePerUnit = 400.0, amount = 400.0, sortOrder = 0)
            )
            database.receiptDao().insertLineItems(lineItems)

            val deductions = listOf(
                DeductionEntity(id = "DED_142_1", receiptId = "RECEIPT_142", type = "COMMISSION", amount = 14.0, isPercentage = true, percentageValue = 4.0),
                DeductionEntity(id = "DED_142_2", receiptId = "RECEIPT_142", type = "DAMAGES", amount = 17.0),
                DeductionEntity(id = "DED_142_3", receiptId = "RECEIPT_142", type = "UNLOADING", amount = 16.0),
                DeductionEntity(id = "DED_142_4", receiptId = "RECEIPT_142", type = "ADVANCE", amount = 120.0),
                DeductionEntity(id = "DED_143_1", receiptId = "RECEIPT_143", type = "COMMISSION", amount = 48.0, isPercentage = true, percentageValue = 4.0),
                DeductionEntity(id = "DED_143_2", receiptId = "RECEIPT_143", type = "DAMAGES", amount = 42.5),
                DeductionEntity(id = "DED_143_3", receiptId = "RECEIPT_143", type = "UNLOADING", amount = 40.0),
                DeductionEntity(id = "DED_143_4", receiptId = "RECEIPT_143", type = "ADVANCE", amount = 500.0),
                DeductionEntity(id = "DED_144_1", receiptId = "RECEIPT_144", type = "COMMISSION", amount = 16.0, isPercentage = true, percentageValue = 4.0),
                DeductionEntity(id = "DED_144_2", receiptId = "RECEIPT_144", type = "DAMAGES", amount = 90.0),
                DeductionEntity(id = "DED_144_3", receiptId = "RECEIPT_144", type = "UNLOADING", amount = 48.0),
                DeductionEntity(id = "DED_144_4", receiptId = "RECEIPT_144", type = "ADVANCE", amount = 240.0),
                DeductionEntity(id = "DED_145_1", receiptId = "RECEIPT_145", type = "COMMISSION", amount = 48.0, isPercentage = true, percentageValue = 4.0),
                DeductionEntity(id = "DED_145_2", receiptId = "RECEIPT_145", type = "DAMAGES", amount = 60.0),
                DeductionEntity(id = "DED_145_3", receiptId = "RECEIPT_145", type = "UNLOADING", amount = 32.0),
                DeductionEntity(id = "DED_145_4", receiptId = "RECEIPT_145", type = "ADVANCE", amount = 160.0),
                DeductionEntity(id = "DED_146_1", receiptId = "RECEIPT_146", type = "COMMISSION", amount = 16.0, isPercentage = true, percentageValue = 4.0),
                DeductionEntity(id = "DED_146_2", receiptId = "RECEIPT_146", type = "DAMAGES", amount = 411.5),
                DeductionEntity(id = "DED_146_3", receiptId = "RECEIPT_146", type = "UNLOADING", amount = 176.0),
                DeductionEntity(id = "DED_146_4", receiptId = "RECEIPT_146", type = "ADVANCE", amount = 1540.0)
            )
            database.receiptDao().insertDeductions(deductions)
        }
    }

    private fun parseDate(dateStr: String): Long {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis()
    }
}