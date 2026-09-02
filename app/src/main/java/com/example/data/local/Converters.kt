package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromMovementType(type: MovementType): String = type.name

    @TypeConverter
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromPaymentType(type: PaymentType): String = type.name

    @TypeConverter
    fun toPaymentType(value: String): PaymentType = PaymentType.valueOf(value)

    @TypeConverter
    fun fromVoucherType(type: VoucherType): String = type.name

    @TypeConverter
    fun toVoucherType(value: String): VoucherType = VoucherType.valueOf(value)

    @TypeConverter
    fun fromVoucherPartnerType(type: VoucherPartnerType): String = type.name

    @TypeConverter
    fun toVoucherPartnerType(value: String): VoucherPartnerType = VoucherPartnerType.valueOf(value)
}

