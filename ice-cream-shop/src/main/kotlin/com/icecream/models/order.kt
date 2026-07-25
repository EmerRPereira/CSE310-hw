package com.icecream.models

import java.time.LocalDateTime

/**
 * Represents an order in the ice cream shop
 */
data class Order(
    val orderId: Int? = null,
    val customerId: Int,
    val orderDate: LocalDateTime = LocalDateTime.now(),
    val status: String = "Pending",
    val totalAmount: Double = 0.0,
    val paymentTypeId: Int? = null,
    val paymentTypeName: String? = null,
    val deliveryAddress: String? = null,
    val specialInstructions: String? = null
) {
    override fun toString(): String {
        return "Order #$orderId - Status: $status - Total: $${"%.2f".format(totalAmount)}"
    }
}

/**
 * Represents an item within an order
 */
data class OrderItem(
    val orderItemId: Int? = null,
    val orderId: Int,
    val productId: Int,
    val productName: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val specialNotes: String? = null
) {
    override fun toString(): String {
        return "${productName ?: "Product #$productId"} x$quantity = $${"%.2f".format(subtotal)}"
    }
}

/**
 * Represents the current cart/order in progress
 */
class ShoppingCart {
    private val items = mutableListOf<Pair<Product, Int>>()
    
    fun addItem(product: Product, quantity: Int) {
        val existing = items.find { it.first.productId == product.productId }
        if (existing != null) {
            val index = items.indexOf(existing)
            items[index] = existing.first to (existing.second + quantity)
        } else {
            items.add(product to quantity)
        }
    }
    
    fun removeItem(productId: Int) {
        items.removeAll { it.first.productId == productId }
    }
    
    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        val existing = items.find { it.first.productId == productId }
        if (existing != null) {
            val index = items.indexOf(existing)
            items[index] = existing.first to quantity
        }
    }
    
    fun calculateTotal(): Double {
        return items.sumOf { it.first.price * it.second }
    }
    
    fun getItems(): List<Pair<Product, Int>> {
        return items.toList()
    }
    
    fun getItemCount(): Int {
        return items.sumOf { it.second }
    }
    
    fun clear() {
        items.clear()
    }
    
    fun isEmpty(): Boolean = items.isEmpty()
    
    override fun toString(): String {
        if (isEmpty()) return "Cart is empty"
        return buildString {
            appendLine("🛒 Shopping Cart:")
            items.forEach { (product, quantity) ->
                appendLine("  ${product.name} x$quantity = $${"%.2f".format(product.price * quantity)}")
            }
            appendLine("  Total: $${"%.2f".format(calculateTotal())}")
            appendLine("  Items: ${getItemCount()} items")
        }
    }
}
