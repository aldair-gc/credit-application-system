package com.aldairgc.credit.application.system.dto

import com.aldairgc.credit.application.system.entity.Credit
import com.aldairgc.credit.application.system.entity.Customer
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class CreditDto(
    @field:NotNull(message = "Inform a valid value") val creditValue: BigDecimal,
    @field:NotNull(message = "Inform the date of first installment")
    @field:Future(message = "The date must be in the future") val dayFirstOfInstallment: LocalDate,
    @field:NotNull(message = "Inform the number of installments")
    @field:Positive(message = "Invalid number of installments") val numberOfInstallments: Int,
    @field:NotNull(message = "Invalid customer ID") val customerId: Long,
) {
    fun toEntity(): Credit = Credit(
        creditValue = this.creditValue,
        dayFirstInstallment = this.dayFirstOfInstallment,
        numberOfInstallments = this.numberOfInstallments,
        customer = Customer(id = this.customerId)
    )
}
