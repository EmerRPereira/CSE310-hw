package com.icecream.database

import com.icecream.models.Customer
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDateTime

/**
 * Repository for Customer database operations
 */
class CustomerRepository {
    
    /**
     * Get all active customers
     */
    fun getAllCustomers(): List<Customer> {
        val customers = mutableListOf<Customer>()
        val query = "SELECT * FROM customers WHERE is_active = true ORDER BY name"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(query)
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs))
                }
            }
        }
        return customers
    }
    
    /**
     * Find customer by ID
     */
    fun findCustomerById(id: Int): Customer? {
        val query = "SELECT * FROM customers WHERE customer_id = ? AND is_active = true"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, id)
                val rs = pstmt.executeQuery()
                if (rs.next()) {
                    return mapResultSetToCustomer(rs)
                }
            }
        }
        return null
    }
    
    /**
     * Find customer by email
     */
    fun findCustomerByEmail(email: String): Customer? {
        val query = "SELECT * FROM customers WHERE email = ? AND is_active = true"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, email)
                val rs = pstmt.executeQuery()
                if (rs.next()) {
                    return mapResultSetToCustomer(rs)
                }
            }
        }
        return null
    }
    
    /**
     * Find customers by name (partial match)
     */
    fun findCustomersByName(name: String): List<Customer> {
        val customers = mutableListOf<Customer>()
        val query = "SELECT * FROM customers WHERE name ILIKE ? AND is_active = true ORDER BY name"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, "%$name%")
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    customers.add(mapResultSetToCustomer(rs))
                }
            }
        }
        return customers
    }
    
    /**
     * Insert a new customer
     */
    fun insertCustomer(customer: Customer): Int {
        val query = """
            INSERT INTO customers (name, phone, email, address, registration_date, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING customer_id
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, customer.name)
                pstmt.setString(2, customer.phone)
                pstmt.setString(3, customer.email)
                pstmt.setString(4, customer.address)
                pstmt.setTimestamp(5, java.sql.Timestamp.valueOf(customer.registrationDate))
                pstmt.setBoolean(6, customer.isActive)
                
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
     * Update customer information
     */
    fun updateCustomer(customer: Customer): Boolean {
        val query = """
            UPDATE customers 
            SET name = ?, phone = ?, email = ?, address = ?
            WHERE customer_id = ? AND is_active = true
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, customer.name)
                pstmt.setString(2, customer.phone)
                pstmt.setString(3, customer.email)
                pstmt.setString(4, customer.address)
                pstmt.setInt(5, customer.customerId!!)
                
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Soft delete customer (set inactive)
     */
    fun deleteCustomer(customerId: Int): Boolean {
        val query = "UPDATE customers SET is_active = false WHERE customer_id = ?"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, customerId)
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Hard delete customer (permanent removal)
     */
    fun hardDeleteCustomer(customerId: Int): Boolean {
        val query = "DELETE FROM customers WHERE customer_id = ?"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, customerId)
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Get customer count
     */
    fun getCustomerCount(): Int {
        val query = "SELECT COUNT(*) FROM customers WHERE is_active = true"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(query)
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        }
        return 0
    }
    
    /**
     * Maps ResultSet to Customer object
     */
    private fun mapResultSetToCustomer(rs: ResultSet): Customer {
        return Customer(
            customerId = rs.getInt("customer_id"),
            name = rs.getString("name"),
            phone = rs.getString("phone"),
            email = rs.getString("email"),
            address = rs.getString("address"),
            registrationDate = rs.getTimestamp("registration_date").toLocalDateTime(),
            isActive = rs.getBoolean("is_active")
        )
    }
}
