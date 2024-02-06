package com.aldairgc.credit.application.system.controller

import com.aldairgc.credit.application.system.dto.CreditDto
import com.aldairgc.credit.application.system.entity.Address
import com.aldairgc.credit.application.system.entity.Credit
import com.aldairgc.credit.application.system.entity.Customer
import com.aldairgc.credit.application.system.repository.CreditRepository
import com.aldairgc.credit.application.system.repository.CustomerRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var creditRepository: CreditRepository

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()

    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

    private fun buildCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(1000.0),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(2L),
        numberOfInstallments: Int = 5,
        customerId: Long = 1L,
    ): CreditDto = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId,
    )

    private fun buildCustomer() = (
        Customer(
            firstName = "Aldair",
            lastName = "Garros",
            cpf = "28475934625",
            email = "info@aldairgc.com",
            password = "123654",
            address = Address(
                zipCode = "65000-000",
                street = "Rua Amazonas",
            ),
            income = BigDecimal.valueOf(10000.0),
            id = 1L,
        )
    )

    @Test
    fun should_create_a_credit_and_return_201_status() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().string(Matchers.matchesRegex("""Credit [a-fA-F0-9\-]{36} - Customer ${customer.firstName} saved!""")))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_when_credit_value_is_not_positive() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, creditValue = BigDecimal.valueOf(-1))
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details['creditValue']").value("Credit value must be positive"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_when_day_first_of_installment_is_not_in_the_future() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, dayFirstOfInstallment = LocalDate.now().minusDays(1))
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details['dayFirstOfInstallment']").value("The date must be in the future"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_when_day_first_of_installment_is_after_3_months() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, dayFirstOfInstallment = LocalDate.now().plusMonths(3).plusDays(1))
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details['dayFirstOfInstallment']").value("First day of installment must be within the next 3 months"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_when_number_of_installments_is_less_than_1() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, numberOfInstallments = 0)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details['numberOfInstallments']").value("Invalid number of installments"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_when_number_of_installments_is_greater_than_48() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val creditDto: CreditDto = buildCreditDto(customerId = customer.id!!, numberOfInstallments = 49)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class org.springframework.web.bind.MethodArgumentNotValidException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details['numberOfInstallments']").value("Not greater than 48"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_find_all_credits_by_customer_id_and_return_200_status() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val fakeCreditList: List<Credit> = listOf(
            buildCreditDto(customerId = customer.id!!).toEntity(),
            buildCreditDto(customerId = customer.id!!).toEntity(),
            buildCreditDto(customerId = customer.id!!).toEntity(),
        )
        val expectedCreditCodeList: List<String> = fakeCreditList.map { it.creditCode.toString() }
        creditRepository.saveAll(fakeCreditList)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[*].creditCode", Matchers.containsInAnyOrder(*expectedCreditCodeList.toTypedArray())))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_find_credit_by_credit_code() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val fakeCredit: Credit = buildCreditDto(customerId = customer.id!!).toEntity()
        creditRepository.save(fakeCredit)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${fakeCredit.creditCode}?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value(fakeCredit.creditCode.toString()))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_not_find_credit_and_return_400_status_and_BusinessException_when_credit_code_is_invalid() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val fakeCredit: Credit = buildCreditDto(customerId = customer.id!!).toEntity()
        creditRepository.save(fakeCredit)
        val fakeCreditCode: UUID = UUID.randomUUID()
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${fakeCreditCode}?customerId=${customer.id}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class com.aldairgc.credit.application.system.exception.BusinessException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").value("CreditCode $fakeCreditCode not found"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun should_return_400_status_and_IllegalArgumentException_when_credit_code_is_invalid() {
        //given
        val customer: Customer = customerRepository.save(buildCustomer())
        val fakeCredit: Credit = buildCreditDto(customerId = customer.id!!).toEntity()
        creditRepository.save(fakeCredit)
        //when //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${fakeCredit.creditCode}?customerId=${Random().nextLong()}")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.exception")
                    .value("class java.lang.IllegalArgumentException")
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").value("Contact admin"))
            .andDo(MockMvcResultHandlers.print())
    }
}