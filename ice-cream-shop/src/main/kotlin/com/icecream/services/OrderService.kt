package com.icecream.services

import com.icecream.database.OrderRepository
import com.icecream.models.Order
import com.icecream.models.OrderItem
import com.icecream.models.ShoppingCart
import com.icecream.models.Product

/**
 * Service layer for order operations
 * Handles business logic for orders
 */
class OrderService {
    private val orderRepo = OrderRepository()
    
    /**
     * Create a new order from a shopping cart
     */
    fun createOrderFromCart(
        customerId: Int,
        cart: ShoppingCart,
        deliveryAddress: String? = null,
        specialInstructions: String? = null,
        paymentTypeId: Int? = null
    ): OrderResult {
        try {
            val items = cart.getItems()
            if (items.isEmpty()) {
                return OrderResult.Error("Cart is empty!")
            }
            
            // Calculate total
            val total = cart.calculateTotal()
            
            // Create order object
            val order = Order(
                customerId = customerId,
                status = "Pending",
                totalAmount = total,
                paymentTypeId = paymentTypeId,
                deliveryAddress = deliveryAddress,
                specialInstructions = specialInstructions
            )
            
            // Insert order
            val orderId = orderRepo.insertOrder(order)
            if (orderId <= 0) {
                return OrderResult.Error("Failed to create order!")
            }
            
            // Create order items
            val orderItems = items.map { (product, quantity) ->
                OrderItem(
                    orderId = orderId,
                    productId = product.productId!!,
                    quantity = quantity,
                    unitPrice = product.price,
                    subtotal = product.price * quantity
                )
            }
            
            // Insert order items
            val success = orderRepo.insertOrderItems(orderId, orderItems)
            if (!success) {
                // Rollback would be handled by transaction
                return OrderResult.Error("Failed to add items to order!")
            }
            
            // Update order total (if not already calculated)
            orderRepo.updateOrderTotal(orderId)
            
            // Clear the cart
            cart.clear()
            
            // Get order summary
            val summary = orderRepo.getOrderSummary(orderId)
            
            return OrderResult.Success(orderId, summary)
            
        } catch (e: Exception) {
            return OrderResult.Error("Error creating order: ${e.message}")
        }
    }
    
    /**
     * Get order details with items
     */
    fun getOrderWithItems(orderId: Int): OrderWithItems? {
        try {
            val order = orderRepo.findOrderById(orderId) ?: return null
            val items = orderRepo.getOrderItems(orderId)
            return OrderWithItems(order, items)
        } catch (e: Exception) {
            println("Error getting order: ${e.message}")
            return null
        }
    }
    
    /**
     * Update order status with validation
     */
    fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        val validStatuses = listOf("Pending", "Preparing", "Ready", "Delivered", "Cancelled")
        
        if (newStatus !in validStatuses) {
            println("❌ Invalid status: $newStatus")
            return false
        }
        
        return orderRepo.updateOrderStatus(orderId, newStatus)
    }
    
    /**
     * Cancel an order
     */
    fun cancelOrder(orderId: Int): Boolean {
        return orderRepo.cancelOrder(orderId)
    }
    
    /**
     * Get customer's order history
     */
    fun getCustomerOrderHistory(customerId: Int): List<Order> {
        return orderRepo.getOrdersByCustomer(customerId)
    }
}

/**
 * Result of an order operation
 */
sealed class OrderResult {
    data class Success(val orderId: Int, val summary: Map<String, Any?>?) : OrderResult()
    data class Error(val message: String) : OrderResult()
}

/**
 * Order with its items
 */
data class OrderWithItems(
    val order: Order,
    val items: List<OrderItem>
) {
    override fun toString(): String {
        return buildString {
            appendLine("📋 Order #${order.orderId}")
            appendLine("  Status: ${order.status}")
            appendLine("  Date: ${order.orderDate}")
            appendLine("  Total: $${"%.2f".format(order.totalAmount)}")
            appendLine("  Items:")
            items.forEach { item ->
                appendLine("    ${item.productName} x${item.quantity} = $${"%.2f".format(item.subtotal)}")
            }
        }
    }
}
