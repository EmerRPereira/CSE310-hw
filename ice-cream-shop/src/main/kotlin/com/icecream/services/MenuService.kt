package com.icecream.services

import com.icecream.database.ProductRepository
import com.icecream.models.Product

/**
 * Service layer for menu operations
 * Handles business logic for product/menu management
 */
class MenuService {
    private val productRepo = ProductRepository()
    
    /**
     * Get all available products grouped by category
     */
    fun getMenuByCategory(): Map<String, List<Product>> {
        val products = productRepo.getAllAvailableProducts()
        return products.groupBy { it.categoryName ?: "Uncategorized" }
    }
    
    /**
     * Get menu as formatted string
     */
    fun getMenuString(): String {
        val menuByCategory = getMenuByCategory()
        
        return buildString {
            appendLine("\n🍦 ICE CREAM MENU 🍦")
            appendLine("=".repeat(40))
            
            menuByCategory.forEach { (category, products) ->
                appendLine("\n📌 ${category.uppercase()}:")
                products.forEach { product ->
                    appendLine("  ${product.productId ?: "?"}. ${product.name} - $${"%.2f".format(product.price)}")
                }
            }
            
            appendLine("\n" + "=".repeat(40))
        }
    }
    
    /**
     * Find product by ID with validation
     */
    fun findProductById(id: Int): Product? {
        if (id <= 0) {
            println("❌ Invalid product ID!")
            return null
        }
        
        val product = productRepo.findProductById(id)
        if (product == null) {
            println("❌ Product not found!")
            return null
        }
        
        if (!product.isAvailable) {
            println("❌ Product is currently unavailable!")
            return null
        }
        
        return product
    }
    
    /**
     * Search products by name
     */
    fun searchProducts(query: String): List<Product> {
        if (query.length < 2) {
            println("❌ Search query must be at least 2 characters!")
            return emptyList()
        }
        
        return productRepo.findProductsByName(query)
    }
    
    /**
     * Get product details as formatted string
     */
    fun getProductDetails(product: Product): String {
        return buildString {
            appendLine("\n📦 PRODUCT DETAILS")
            appendLine("=".repeat(30))
            appendLine("  ID: ${product.productId}")
            appendLine("  Name: ${product.name}")
            appendLine("  Description: ${product.description ?: "No description"}")
            appendLine("  Price: $${"%.2f".format(product.price)}")
            appendLine("  Category: ${product.categoryName ?: "Uncategorized"}")
            appendLine("  Status: ${if (product.isAvailable) "✅ Available" else "❌ Unavailable"}")
            appendLine("=".repeat(30))
        }
    }
    
    /**
     * Add new product
     */
    fun addProduct(name: String, price: Double, categoryId: Int? = null, description: String? = null): Boolean {
        if (name.isBlank()) {
            println("❌ Product name cannot be empty!")
            return false
        }
        
        if (price <= 0) {
            println("❌ Price must be greater than zero!")
            return false
        }
        
        val product = Product(
            name = name.trim(),
            description = description,
            price = price,
            categoryId = categoryId,
            isAvailable = true
        )
        
        val id = productRepo.insertProduct(product)
        if (id > 0) {
            println("✅ Product added successfully! ID: $id")
            return true
        } else {
            println("❌ Failed to add product!")
            return false
        }
    }
    
    /**
     * Update product availability
     */
    fun toggleProductAvailability(productId: Int): Boolean {
        val product = productRepo.findProductById(productId) ?: return false
        
        val newStatus = !product.isAvailable
        val success = productRepo.updateProductAvailability(productId, newStatus)
        
        if (success) {
            println("✅ Product availability updated: ${if (newStatus) "Available" else "Unavailable"}")
            return true
        } else {
            println("❌ Failed to update product availability!")
            return false
        }
    }
    
    /**
     * Get product count
     */
    fun getProductCount(): Int {
        return productRepo.getProductCount()
    }
}
