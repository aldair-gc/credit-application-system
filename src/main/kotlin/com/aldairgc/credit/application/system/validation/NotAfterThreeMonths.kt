package com.aldairgc.credit.application.system.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [NotAfterThreeMonthsValidator::class])
annotation class NotAfterThreeMonths(
    val message: String = "Must be not after three months from now",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NotAfterThreeMonthsValidator : ConstraintValidator<NotAfterThreeMonths, LocalDate> {
    override fun initialize(constraintAnnotation: NotAfterThreeMonths) {}

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        val currentDate = LocalDate.now()
        val futureDateLimit = currentDate.plusMonths(3)

        return !value.isAfter(futureDateLimit)
    }
}