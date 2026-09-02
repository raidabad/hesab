package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VoucherType(val arabicName: String) {
    RECEIPT("سند قبض"),
    PAYMENT("سند صرف")
}

enum class VoucherPartnerType(val arabicName: String) {
    CUSTOMER("عميل"),
    SUPPLIER("مورد"),
    GENERAL_ACCOUNT("حساب عام / مصروف")
}

@Entity(tableName = "vouchers")
data class Voucher(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherNumber: String, // 1, 2, 3...
    val type: VoucherType, // RECEIPT, PAYMENT
    val date: Long,
    val amount: Double,
    val paymentType: PaymentType = PaymentType.CASH, // CASH or BANK
    val partnerType: VoucherPartnerType = VoucherPartnerType.CUSTOMER,
    val partnerId: Long? = null,
    val partnerName: String = "",
    val accountId: Long? = null, // specific account if general or cash/bank
    val accountName: String = "",
    val notes: String = "",
    val journalEntryId: Long? = null
)
