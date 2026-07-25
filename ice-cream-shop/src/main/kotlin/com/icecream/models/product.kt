package com.icecream.models

import java.time.LocalDateTime

/**
 * Represents a product in the ice cream shop
 */
data class Product(
    val productId: Int? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val isAvailable: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "${if (isAvailable) "🟢" else "🔴"} $name - $${"%.2f".format(price)}"
    }
    
    fun getDisplayString(): String {
        return "${productId ?: "?"}. $name - $${"%.2f".format(price)} ${if (categoryName != null) "($categoryName)" else ""}"
    }
}
