package com.icecream.database

import com.icecream.models.Product
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types
import java.time.LocalDateTime

/**
 * Repository for Product database operations
 */
class ProductRepository {
    
    /**
     * Get all available products with category name
     */
    fun getAllAvailableProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val query = """
            SELECT p.*, c.category_name 
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            WHERE p.is_available = true
            ORDER BY p.category_id, p.name
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(query)
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs))
                }
            }
        }
        return products
    }
    
    /**
     * Get all products (including unavailable) with category name
     */
    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val query = """
            SELECT p.*, c.category_name 
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            ORDER BY p.is_available DESC, p.category_id, p.name
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(query)
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs))
                }
            }
        }
        return products
    }
    
    /**
     * Find product by ID
     */
    fun findProductById(id: Int): Product? {
        val query = """
            SELECT p.*, c.category_name 
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            WHERE p.product_id = ?
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, id)
                val rs = pstmt.executeQuery()
                if (rs.next()) {
                    return mapResultSetToProduct(rs)
                }
            }
        }
        return null
    }
    
    /**
     * Find products by category
     */
    fun findProductsByCategory(categoryId: Int): List<Product> {
        val products = mutableListOf<Product>()
        val query = """
            SELECT p.*, c.category_name 
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            WHERE p.category_id = ? AND p.is_available = true 
            ORDER BY p.name
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, categoryId)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs))
                }
            }
        }
        return products
    }
    
    /**
     * Find products by name (partial match)
     */
    fun findProductsByName(name: String): List<Product> {
        val products = mutableListOf<Product>()
        val query = """
            SELECT p.*, c.category_name 
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.category_id
            WHERE p.name ILIKE ? AND p.is_available = true
            ORDER BY p.name
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, "%$name%")
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs))
                }
            }
        }
        return products
    }
    
    /**
     * Insert a new product - CORRIGIDO
     */
    fun insertProduct(product: Product): Int {
        val query = """
            INSERT INTO products (name, description, price, category_id, is_available)
            VALUES (?, ?, ?, ?, ?)
            RETURNING product_id
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS).use { pstmt ->
                pstmt.setString(1, product.name)
                pstmt.setString(2, product.description)
                pstmt.setDouble(3, product.price)
                
                // CORREÇÃO: Verificar se categoryId é null
                if (product.categoryId != null) {
                    pstmt.setInt(4, product.categoryId)
                } else {
                    pstmt.setNull(4, Types.INTEGER)
                }
                
                pstmt.setBoolean(5, product.isAvailable)
                
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
     * Update product
     */
    fun updateProduct(product: Product): Boolean {
        val query = """
            UPDATE products 
            SET name = ?, description = ?, price = ?, category_id = ?, is_available = ?
            WHERE product_id = ?
        """.trimIndent()
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setString(1, product.name)
                pstmt.setString(2, product.description)
                pstmt.setDouble(3, product.price)
                
                if (product.categoryId != null) {
                    pstmt.setInt(4, product.categoryId)
                } else {
                    pstmt.setNull(4, Types.INTEGER)
                }
                
                pstmt.setBoolean(5, product.isAvailable)
                pstmt.setInt(6, product.productId!!)
                
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Update product availability
     */
    fun updateProductAvailability(productId: Int, available: Boolean): Boolean {
        val query = "UPDATE products SET is_available = ? WHERE product_id = ?"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setBoolean(1, available)
                pstmt.setInt(2, productId)
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Delete product (hard delete)
     */
    fun deleteProduct(productId: Int): Boolean {
        val query = "DELETE FROM products WHERE product_id = ?"
        
        DatabaseConnection.getConnection().use { conn ->
            conn.prepareStatement(query).use { pstmt ->
                pstmt.setInt(1, productId)
                return pstmt.executeUpdate() > 0
            }
        }
    }
    
    /**
     * Get product count
     */
    fun getProductCount(): Int {
        val query = "SELECT COUNT(*) FROM products WHERE is_available = true"
        
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
     * Maps ResultSet to Product object
     */
    private fun mapResultSetToProduct(rs: ResultSet): Product {
        return Product(
            productId = rs.getInt("product_id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            price = rs.getDouble("price"),
            categoryId = rs.getObject("category_id")?.let { rs.getInt("category_id") },
            categoryName = rs.getString("category_name"),
            isAvailable = rs.getBoolean("is_available"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
        )
    }
}
