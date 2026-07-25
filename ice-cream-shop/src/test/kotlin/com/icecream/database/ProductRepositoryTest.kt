package com.icecream.database

import com.icecream.models.Product
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes para o ProductRepository
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductRepositoryTest {
    
    private lateinit var repository: ProductRepository
    private var testProductId: Int = -1
    
    @BeforeAll
    fun setup() {
        repository = ProductRepository()
        println("🧪 Iniciando testes do ProductRepository...")
    }
    
    @BeforeEach
    fun setupEach() {
        // Criar um produto de teste
        val product = Product(
            name = "Test Product ${System.currentTimeMillis()}",
            description = "Description for test",
            price = 9.99,
            categoryId = 1,
            isAvailable = true
        )
        testProductId = repository.insertProduct(product)
        println("✅ Produto de teste criado: ID $testProductId")
    }
    
    @AfterEach
    fun cleanupEach() {
        if (testProductId > 0) {
            repository.deleteProduct(testProductId)
            println("🧹 Produto de teste removido: ID $testProductId")
        }
    }
    
    @Test
    fun `should insert product successfully`() {
        // Given
        val product = Product(
            name = "Chocolate Deluxe",
            description = "Premium chocolate ice cream",
            price = 12.99,
            categoryId = 1,
            isAvailable = true
        )
        
        // When
        val id = repository.insertProduct(product)
        
        // Then
        assertTrue(id > 0)
        
        // Verify
        val saved = repository.findProductById(id)
        assertNotNull(saved)
        assertEquals("Chocolate Deluxe", saved?.name)
        
        // Cleanup
        repository.deleteProduct(id)
    }
    
    @Test
    fun `should find product by id`() {
        // When
        val found = repository.findProductById(testProductId)
        
        // Then
        assertNotNull(found)
        assertEquals(testProductId, found?.productId)
        assertTrue(found?.name?.startsWith("Test Product") == true)
    }
    
    @Test
    fun `should get all available products`() {
        // When
        val products = repository.getAllAvailableProducts()
        
        // Then
        assertNotNull(products)
        assertTrue(products.isNotEmpty())
        
        // All products should be available
        products.forEach { product ->
            assertTrue(product.isAvailable)
        }
        
        // Should contain our test product
        assertTrue(products.any { it.productId == testProductId })
    }
    
    @Test
    fun `should get all products including unavailable`() {
        // Given - create an unavailable product
        val product = Product(
            name = "Unavailable Product",
            description = "This product is not available",
            price = 5.99,
            categoryId = 1,
            isAvailable = false
        )
        val id = repository.insertProduct(product)
        
        // When
        val allProducts = repository.getAllProducts()
        
        // Then
        assertNotNull(allProducts)
        assertTrue(allProducts.isNotEmpty())
        
        // Should contain both available and unavailable
        val found = allProducts.find { it.productId == id }
        assertNotNull(found)
        assertFalse(found?.isAvailable == true)
        
        // Cleanup
        repository.deleteProduct(id)
    }
    
    @Test
    fun `should find products by category`() {
        // When
        val products = repository.findProductsByCategory(1)
        
        // Then
        assertNotNull(products)
        assertTrue(products.isNotEmpty())
        
        // All products should be in category 1
        products.forEach { product ->
            assertEquals(1, product.categoryId)
        }
    }
    
    @Test
    fun `should search products by name`() {
        // When
        val results = repository.findProductsByName("Test Product")
        
        // Then
        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.productId == testProductId })
    }
    
    @Test
    fun `should update product availability`() {
        // When
        val success = repository.updateProductAvailability(testProductId, false)
        
        // Then
        assertTrue(success)
        
        // Verify
        val updated = repository.findProductById(testProductId)
        assertNotNull(updated)
        assertFalse(updated?.isAvailable == true)
        
        // Toggle back
        repository.updateProductAvailability(testProductId, true)
    }
    
    @Test
    fun `should update product completely`() {
        // Given
        val product = repository.findProductById(testProductId)
        assertNotNull(product)
        
        val updatedProduct = product!!.copy(
            name = "Updated Product Name",
            description = "Updated description",
            price = 19.99,
            isAvailable = false
        )
        
        // When
        val success = repository.updateProduct(updatedProduct)
        
        // Then
        assertTrue(success)
        
        // Verify
        val saved = repository.findProductById(testProductId)
        assertNotNull(saved)
        assertEquals("Updated Product Name", saved?.name)
        assertEquals(19.99, saved?.price)
        assertFalse(saved?.isAvailable == true)
    }
    
    @Test
    fun `should return correct product count`() {
        // When
        val count = repository.getProductCount()
        
        // Then
        assertTrue(count > 0)
    }
}
