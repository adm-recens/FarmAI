package com.farmai.core.domain.parser

import com.farmai.core.domain.model.DeductionType
import com.farmai.core.domain.model.ParsedDeduction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiptOcrParserTest {
    @Test
    fun parsesVoucherBrokerSupplierLineItemsAndDeductions() {
        val parsed = ReceiptOcrParser.parse(
            """
            AHMED SHARIF & BROS
            DARUSHAFA X ROAD, HYDERABAD 500024
            Phone: 040-24412139
            Voucher No: 142
            Voucher Date: 01/01/2026
            Supplier Code: VK

            Qty Price Amount
            1 350 350
            2 300 600

            Commission 4%
            Damage 17
            Unloading 16
            Advance 120
            """.trimIndent()
        )

        assertEquals("142", parsed.voucherNumber)
        assertEquals(parseDate("01/01/2026"), parsed.voucherDate)
        assertEquals("VK", parsed.supplierCode)
        assertNotNull(parsed.brokerName)
        assertEquals(2, parsed.lineItems.size)
        assertEquals(4.0, parsed.commissionPercent)
        assertEquals(17.0, parsed.damagesAmount)
        assertEquals(16.0, parsed.unloadingAmount)
        assertEquals(120.0, parsed.advanceAmount)
        assertTrue(parsed.confidenceScore > 0.7)
    }

    @Test
    fun mapsParsedDataToReceiptLineItemsAndDeductions() {
        val parsed = ReceiptOcrParser.parse(
            """
            Broker Name
            Voucher No: 201
            Date: 02/02/2026
            Farmer Code: DPH
            Qty Rate Amount
            4 200 800
            Commission 4%
            Less For Damage 25
            """.trimIndent()
        )

        val deductions = parsed.otherDeductions + listOfNotNull(
            parsed.commissionPercent?.let { ParsedDeduction(DeductionType.COMMISSION, 0.0, isPercentage = true, percentageValue = it) },
            parsed.damagesAmount?.let { ParsedDeduction(DeductionType.DAMAGES, it) }
        )

        assertTrue(parsed.lineItems.single().quantity == 4.0)
        assertTrue(deductions.any { it.type == DeductionType.COMMISSION && it.isPercentage && it.percentageValue == 4.0 })
        assertTrue(deductions.any { it.type == DeductionType.DAMAGES && it.amount == 25.0 })
    }

    private fun parseDate(date: String): Long {
        return SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).parse(date)?.time ?: 0L
    }
}
