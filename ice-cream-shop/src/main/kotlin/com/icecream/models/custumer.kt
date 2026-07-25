package com.icecream.models

import java.time.LocalDateTime

/**
 * Represents a customer in the ice cream shop
 */
data class Customer(
    val customerId: Int? = null,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val registrationDate: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
) {
    override fun toString(): String {
        return "Customer #$customerId: $name ($email)"
    }
}
