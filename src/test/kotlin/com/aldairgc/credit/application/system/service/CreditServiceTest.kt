package com.aldairgc.credit.application.system.service

import com.aldairgc.credit.application.system.entity.Address
import com.aldairgc.credit.application.system.entity.Credit
import com.aldairgc.credit.application.system.entity.Customer
import com.aldairgc.credit.application.system.exception.BusinessException
import com.aldairgc.credit.application.system.repository.CreditRepository
import com.aldairgc.credit.application.system.service.impl.CreditService
import com.aldairgc.credit.application.system.service.impl.CustomerService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Random
import java.util.UUID

@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CreditServiceTest {
    @MockK
    lateinit var creditRepository: CreditRepository

    @MockK
    lateinit var customerService: CustomerService

    @InjectMockKs
    lateinit var creditService: CreditService

    @Test
    fun should_create_credit() {
        //given
        val fakeCustomerId = 1L
        val fakeCustomer = buildCustomer(id = fakeCustomerId)
        val fakeCredit: Credit = buildCredit(customer = fakeCustomer)
        every { customerService.findById(fakeCustomerId) } returns fakeCustomer
        every { creditRepository.save(fakeCredit) } returns fakeCredit
        //when
        val actual: Credit = creditService.save(fakeCredit)
        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.save(fakeCredit) }
    }

    @Test
    fun should_not_create_credit_if_first_day_installment_is_invalid() {
        //given
        val invalidDayFirstInstallment: LocalDate = LocalDate.now().plusMonths(5L)
        val fakeCustomerId = 2L
        val fakeCustomer: Customer = buildCustomer(id = fakeCustomerId)
        val fakeCredit: Credit = buildCredit(dayFirstOfInstallment = invalidDayFirstInstallment, customer = fakeCustomer)
        every { customerService.findById(fakeCustomerId) } returns fakeCustomer
        every { creditRepository.save(fakeCredit) } returns fakeCredit
        //when
        Assertions.assertThatThrownBy { creditService.save(fakeCredit) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage("First day of installment must be within the next 3 months")
        //then
        verify(exactly = 0) { creditRepository.save(fakeCredit) }
    }

    @Test
    fun should_find_all_by_customer_ID() {
        //given
        val fakeCustomerId = 1L
        val fakeCreditList: List<Credit> = listOf(buildCredit(), buildCredit(), buildCredit())
        every { creditRepository.findAllByCustomerId(fakeCustomerId) } returns fakeCreditList
        //when
        val actual: List<Credit> = creditService.findAllByCustomerId(fakeCustomerId)
        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isNotEmpty
        Assertions.assertThat(actual).isSameAs(fakeCreditList)
        verify(exactly = 1) { creditRepository.findAllByCustomerId(fakeCustomerId) }
    }

    @Test
    fun should_find_by_credit_code() {
        //given
        val fakeCustomerId = 1L
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCredit: Credit = buildCredit(customer = Customer(id = fakeCustomerId))
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns fakeCredit
        //when
        val actual: Credit = creditService.findByCreditCode(fakeCustomerId, fakeCreditCode)
        //then
        Assertions.assertThat(actual).isNotNull
        Assertions.assertThat(actual).isSameAs(fakeCredit)
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    @Test
    fun should_throw_a_BusinessException_for_invalid_credit_codes() {
        //given
        val fakeCustomerId = 1L
        val invalidCreditCode: UUID = UUID.randomUUID()
        every { creditRepository.findByCreditCode(invalidCreditCode) } returns null
        //when //then
        Assertions.assertThatThrownBy { creditService.findByCreditCode(customerId = fakeCustomerId, creditCode = invalidCreditCode) }
            .isInstanceOf(BusinessException::class.java)
            .hasMessage("CreditCode $invalidCreditCode not found")
        verify(exactly = 1) { creditRepository.findByCreditCode(invalidCreditCode) }
    }

    @Test
    fun should_throw_an_IllegalArgumentException_when_credit_has_different_customer() {
        //given
        val fakeCustomerId = 1L
        val fakeCreditCode: UUID = UUID.randomUUID()
        val fakeCredit: Credit = buildCredit(customer = Customer(id = 2L))
        every { creditRepository.findByCreditCode(fakeCreditCode) } returns fakeCredit
        //when //then
        Assertions.assertThatThrownBy { creditService.findByCreditCode(customerId = fakeCustomerId, creditCode = fakeCreditCode) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Contact admin")
        verify(exactly = 1) { creditRepository.findByCreditCode(fakeCreditCode) }
    }

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(1000),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(2L),
        numberOfInstallments: Int = 5,
        customer: Customer = buildCustomer(),
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer,
    )

    private fun buildCustomer(
        firstName: String = "Aldair",
        lastName: String = "Garros",
        cpf: String = "28475934625",
        email: String = "info@aldairgc.com",
        password: String = "123654",
        zipCode: String = "65000000",
        street: String = "Amazonas",
        income: BigDecimal = BigDecimal.valueOf(10000.0),
        id: Long = Random().nextLong(),
    ): Customer = Customer(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,
        address = Address(
            zipCode = zipCode,
            street = street,
        ),
        income = income,
        id = id,
    )
}