package com.aldairgc.credit.application.system.dto

import com.aldairgc.credit.application.system.entity.Address
import com.aldairgc.credit.application.system.entity.Customer
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.br.CPF
import java.math.BigDecimal

data class CustomerDto(
    @field:NotBlank(message = "First name must be fulfilled") val firstName: String,
    @field:NotBlank(message = "Last name must be fulfilled") val lastName: String,
    @field:NotBlank(message = "CPF must be fulfilled") @field:CPF(message = "Invalid CPF number") val cpf: String,
    @field:NotNull(message = "Income must be informed") val income: BigDecimal,
    @field:NotBlank(message = "Email must be fulfilled") @field:Email(message = "Invalid email") val email: String,
    @field:NotBlank(message = "Password must be fulfilled") val password: String,
    @field:NotBlank(message = "Zipcode must be fulfilled") val zipCode: String,
    @field:NotBlank(message = "Street must be fulfilled") val street: String,
) {
    fun toEntity(): Customer = Customer(
        firstName = this.firstName,
        lastName = this.lastName,
        cpf = this.cpf,
        income = this.income,
        email = this.email,
        password = this.password,
        address = Address(
            zipCode = this.zipCode,
            street = this.street,
        )
    )
}