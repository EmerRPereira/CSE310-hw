package com.icecream.database

import com.icecream.models.Order
import com.icecream.models.OrderItem
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * Repository for Order database operations
 */
class OrderRepository {

    /**
     * Get all orders for a specific customer
     */
    fun getOrdersByCustomer(customerId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        val query = """
            SELECT * FROM orders 
            WHERE customer_id = ? 
            ORDER BY order_date DESC
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, customerId)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs))
                }
            }
        }
        return orders
    }

    /**
     * Get all orders with status filter
     */
    fun getOrdersByStatus(status: String): List<Order> {
        val orders = mutableListOf<Order>()
        val query = """
            SELECT o.*, pt.type_name as payment_type_name
            FROM orders o
            LEFT JOIN payment_types pt ON o.payment_type_id = pt.payment_type_id
            WHERE o.status = ? 
            ORDER BY o.order_date DESC
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, status)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs))
                }
            }
        }
        return orders
    }

    /**
     * Get all orders (with optional limit)
     */
    fun getAllOrders(limit: Int = 100): List<Order> {
        val orders = mutableListOf<Order>()
        val query = """
            SELECT o.*, pt.type_name as payment_type_name
            FROM orders o
            LEFT JOIN payment_types pt ON o.payment_type_id = pt.payment_type_id
            ORDER BY o.order_date DESC 
            LIMIT ?
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, limit)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs))
                }
            }
        }
        return orders
    }

    /**
     * Find order by ID with all details
     */
    fun findOrderById(orderId: Int): Order? {
        val query = """
            SELECT o.*, pt.type_name as payment_type_name
            FROM orders o
            LEFT JOIN payment_types pt ON o.payment_type_id = pt.payment_type_id
            WHERE o.order_id = ?
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, orderId)
                val rs = pstmt.executeQuery()
                if (rs.next()) {
                    return mapResultSetToOrder(rs)
                }
            }
        }
        return null
    }

    /**
     * Get order items for a specific order
     */
    fun getOrderItems(orderId: Int): List<OrderItem> {
        val items = mutableListOf<OrderItem>()
        val query = """
            SELECT oi.*, p.name as product_name 
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            WHERE oi.order_id = ?
            ORDER BY oi.order_item_id
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, orderId)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    items.add(mapResultSetToOrderItem(rs))
                }
            }
        }
        return items
    }

/**
 * Insert a new order
 * Returns the generated order ID
 */
fun insertOrder(order: Order): Int {
    val query = """
        INSERT INTO orders (
            customer_id, 
            status, 
            total_amount, 
            payment_type_id, 
            delivery_address, 
            special_instructions
        ) VALUES (?, ?, ?, ?, ?, ?)
    """.trimIndent()

    DatabaseConnection.getConnection().use { conn ->
        conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
            pstmt.setInt(1, order.customerId)
            pstmt.setString(2, order.status)
            pstmt.setDouble(3, order.totalAmount)
            
            if (order.paymentTypeId != null) {
                pstmt.setInt(4, order.paymentTypeId)
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER)
            }
            
            pstmt.setString(5, order.deliveryAddress)
            pstmt.setString(6, order.specialInstructions)

            val affectedRows = pstmt.executeUpdate()
            
            if (affectedRows > 0) {
                val rs = pstmt.generatedKeys
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        }
    }
    return -1
}
    /**
     * Insert multiple order items (batch insert)
     */
    fun insertOrderItems(orderId: Int, items: List<OrderItem>): Boolean {
        val query = """
            INSERT INTO order_items (
                order_id, 
                product_id, 
                quantity, 
                unit_price, 
                subtotal, 
                special_notes
            ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                items.forEach { item ->
                    pstmt.setInt(1, orderId)
                    pstmt.setInt(2, item.productId)
                    pstmt.setInt(3, item.quantity)
                    pstmt.setDouble(4, item.unitPrice)
                    pstmt.setDouble(5, item.subtotal)
                    pstmt.setString(6, item.specialNotes)
                    pstmt.addBatch()
                }
                val results = pstmt.executeBatch()
                return results.all { it >= 0 }
            }
        }
    }

    /**
     * Update order status
     */
    fun updateOrderStatus(orderId: Int, newStatus: String): Boolean {
        val query = "UPDATE orders SET status = ? WHERE order_id = ?"
        val validStatuses = listOf("Pending", "Preparing", "Ready", "Delivered", "Cancelled")

        if (newStatus !in validStatuses) {
            println("❌ Invalid status: $newStatus. Valid statuses: $validStatuses")
            return false
        }

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, newStatus)
                pstmt.setInt(2, orderId)
                return pstmt.executeUpdate() > 0
            }
        }
    }

    /**
     * Update order total amount
     */
    fun updateOrderTotal(orderId: Int): Boolean {
        val query = """
            UPDATE orders 
            SET total_amount = (
                SELECT COALESCE(SUM(subtotal), 0)
                FROM order_items
                WHERE order_id = ?
            )
            WHERE order_id = ?
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, orderId)
                pstmt.setInt(2, orderId)
                return pstmt.executeUpdate() > 0
            }
        }
    }

    /**
     * Cancel order (soft delete - just update status)
     */
    fun cancelOrder(orderId: Int): Boolean {
        return updateOrderStatus(orderId, "Cancelled")
    }

    /**
     * Delete order and its items (hard delete)
     */
    fun deleteOrder(orderId: Int): Boolean {
        val query = "DELETE FROM orders WHERE order_id = ?"

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, orderId)
                return pstmt.executeUpdate() > 0
            }
        }
    }

    /**
     * Get order summary with customer details
     */
    fun getOrderSummary(orderId: Int): Map<String, Any?>? {
        val query = """
            SELECT 
                o.order_id,
                o.order_date,
                o.status,
                o.total_amount,
                c.name AS customer_name,
                c.phone AS customer_phone,
                c.email AS customer_email,
                COUNT(oi.order_item_id) AS item_count,
                SUM(oi.quantity) AS total_quantity
            FROM orders o
            JOIN customers c ON o.customer_id = c.customer_id
            LEFT JOIN order_items oi ON o.order_id = oi.order_id
            WHERE o.order_id = ?
            GROUP BY o.order_id, c.name, c.phone, c.email
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, orderId)
                val rs = pstmt.executeQuery()
                if (rs.next()) {
                    return mapOf(
                        "orderId" to rs.getInt("order_id"),
                        "orderDate" to rs.getTimestamp("order_date").toLocalDateTime(),
                        "status" to rs.getString("status"),
                        "totalAmount" to rs.getDouble("total_amount"),
                        "customerName" to rs.getString("customer_name"),
                        "customerPhone" to rs.getString("customer_phone"),
                        "customerEmail" to rs.getString("customer_email"),
                        "itemCount" to rs.getInt("item_count"),
                        "totalQuantity" to rs.getInt("total_quantity")
                    )
                }
            }
        }
        return null
    }

    /**
     * Get revenue report by period
     */
    fun getRevenueReport(startDate: LocalDateTime, endDate: LocalDateTime): List<Map<String, Any>> {
        val report = mutableListOf<Map<String, Any>>()
        val query = """
            SELECT 
                DATE(o.order_date) AS order_date,
                COUNT(o.order_id) AS order_count,
                SUM(o.total_amount) AS total_revenue,
                AVG(o.total_amount) AS average_order_value,
                COUNT(DISTINCT o.customer_id) AS unique_customers
            FROM orders o
            WHERE o.order_date BETWEEN ? AND ?
                AND o.status != 'Cancelled'
            GROUP BY DATE(o.order_date)
            ORDER BY order_date
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setTimestamp(1, Timestamp.valueOf(startDate))
                pstmt.setTimestamp(2, Timestamp.valueOf(endDate))
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    report.add(
                        mapOf(
                            "date" to rs.getDate("order_date").toLocalDate(),
                            "orderCount" to rs.getInt("order_count"),
                            "totalRevenue" to rs.getDouble("total_revenue"),
                            "averageOrderValue" to rs.getDouble("average_order_value"),
                            "uniqueCustomers" to rs.getInt("unique_customers")
                        )
                    )
                }
            }
        }
        return report
    }

    /**
     * Get most popular products
     */
    fun getMostPopularProducts(limit: Int = 10): List<Map<String, Any>> {
        val products = mutableListOf<Map<String, Any>>()
        val query = """
            SELECT 
                p.product_id,
                p.name AS product_name,
                SUM(oi.quantity) AS total_sold,
                SUM(oi.subtotal) AS total_revenue,
                COUNT(DISTINCT o.order_id) AS order_count,
                AVG(oi.unit_price) AS avg_price
            FROM order_items oi
            JOIN products p ON oi.product_id = p.product_id
            JOIN orders o ON oi.order_id = o.order_id
            WHERE o.status != 'Cancelled'
            GROUP BY p.product_id, p.name
            ORDER BY total_sold DESC
            LIMIT ?
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, limit)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    products.add(
                        mapOf(
                            "productId" to rs.getInt("product_id"),
                            "productName" to rs.getString("product_name"),
                            "totalSold" to rs.getInt("total_sold"),
                            "totalRevenue" to rs.getDouble("total_revenue"),
                            "orderCount" to rs.getInt("order_count"),
                            "averagePrice" to rs.getDouble("avg_price")
                        )
                    )
                }
            }
        }
        return products
    }

    /**
     * Get top customers by total spending
     */
    fun getTopCustomers(limit: Int = 10): List<Map<String, Any>> {
        val customers = mutableListOf<Map<String, Any>>()
        val query = """
            SELECT 
                c.customer_id,
                c.name AS customer_name,
                c.email,
                c.phone,
                COUNT(o.order_id) AS total_orders,
                SUM(o.total_amount) AS total_spent,
                AVG(o.total_amount) AS avg_order_value,
                MAX(o.order_date) AS last_order_date
            FROM customers c
            JOIN orders o ON c.customer_id = o.customer_id
            WHERE o.status != 'Cancelled'
            GROUP BY c.customer_id, c.name, c.email, c.phone
            HAVING COUNT(o.order_id) > 0
            ORDER BY total_spent DESC
            LIMIT ?
        """.trimIndent()

        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, limit)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    customers.add(
                        mapOf(
                            "customerId" to rs.getInt("customer_id"),
                            "customerName" to rs.getString("customer_name"),
                            "email" to rs.getString("email"),
                            "phone" to rs.getString("phone"),
                            "totalOrders" to rs.getInt("total_orders"),
                            "totalSpent" to rs.getDouble("total_spent"),
                            "averageOrderValue" to rs.getDouble("avg_order_value"),
                            "lastOrderDate" to rs.getTimestamp("last_order_date").toLocalDateTime()
                        )
                    )
                }
            }
        }
        return customers
    }

    /**
     * Maps ResultSet to Order object
     */
    private fun mapResultSetToOrder(rs: ResultSet): Order {
        return Order(
            orderId = rs.getInt("order_id"),
            customerId = rs.getInt("customer_id"),
            orderDate = rs.getTimestamp("order_date").toLocalDateTime(),
            status = rs.getString("status"),
            totalAmount = rs.getDouble("total_amount"),
            paymentTypeId = rs.getObject("payment_type_id")?.let { rs.getInt("payment_type_id") },
            paymentTypeName = rs.getString("payment_type_name"),
            deliveryAddress = rs.getString("delivery_address"),
            specialInstructions = rs.getString("special_instructions")
        )
    }

    /**
     * Maps ResultSet to OrderItem object
     */
    private fun mapResultSetToOrderItem(rs: ResultSet): OrderItem {
        return OrderItem(
            orderItemId = rs.getInt("order_item_id"),
            orderId = rs.getInt("order_id"),
            productId = rs.getInt("product_id"),
            productName = rs.getString("product_name"),
            quantity = rs.getInt("quantity"),
            unitPrice = rs.getDouble("unit_price"),
            subtotal = rs.getDouble("subtotal"),
            specialNotes = rs.getString("special_notes")
        )
    }
}
