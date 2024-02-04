package com.aldairgc.credit.application.system.dto

import com.aldairgc.credit.application.system.entity.Customer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CustomerUpdateDto(
    @field:NotBlank(message = "First name must be fulfilled") val firstName: String,
    @field:NotBlank(message = "Last name must be fulfilled") val lastName: String,
    @field:NotNull(message = "Income must be informed") val income: BigDecimal,
    @field:NotBlank(message = "Zipcode must be fulfilled") val zipCode: String,
    @field:NotBlank(message = "Street must be fulfilled") val street: String,
    @field:NotBlank(message = "Email must be fulfilled") val email: String,
) {
    fun toEntity(customer: Customer): Customer {
        customer.firstName = this.firstName
        customer.lastName = this.lastName
        customer.income = this.income
        customer.address.zipCode = this.zipCode
        customer.address.street = this.street
        customer.email = this.email

        return customer
    }
}
