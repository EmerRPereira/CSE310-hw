package com.icecream.database

import com.icecream.models.Order
import com.icecream.models.OrderItem
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes para o OrderRepository
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRepositoryTest {
    
    private lateinit var orderRepo: OrderRepository
    private lateinit var customerRepo: CustomerRepository
    private lateinit var productRepo: ProductRepository
    
    private var testCustomerId: Int = -1
    private var testOrderId: Int = -1
    private var testProductId: Int = -1
    
    @BeforeAll
    fun setup() {
        orderRepo = OrderRepository()
        customerRepo = CustomerRepository()
        productRepo = ProductRepository()
        println("🧪 Iniciando testes do OrderRepository...")
    }
    
    @BeforeEach
    fun setupEach() {
        // Criar um cliente de teste
        val customer = Customer(
            name = "Test Customer ${System.currentTimeMillis()}",
            phone = "(11) 99999-9999",
            email = "test.customer.${System.currentTimeMillis()}@email.com",
            address = "Rua Teste, 123"
        )
        testCustomerId = customerRepo.insertCustomer(customer)
        
        // Criar um produto de teste
        val product = Product(
            name = "Test Product ${System.currentTimeMillis()}",
            description = "Test description",
            price = 9.99,
            categoryId = 1,
            isAvailable = true
        )
        testProductId = productRepo.insertProduct(product)
        
        println("✅ Cliente de teste: $testCustomerId, Produto: $testProductId")
    }
    
    @AfterEach
    fun cleanupEach() {
        // Limpar ordem de teste
        if (testOrderId > 0) {
            orderRepo.deleteOrder(testOrderId)
        }
        // Limpar cliente e produto
        if (testCustomerId > 0) {
            customerRepo.hardDeleteCustomer(testCustomerId)
        }
        if (testProductId > 0) {
            productRepo.deleteProduct(testProductId)
        }
        println("🧹 Limpeza concluída")
    }
    
    @Test
    fun `should insert order successfully`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98,
            deliveryAddress = "Rua Teste, 123"
        )
        
        // When
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // Then
        assertTrue(orderId > 0)
        
        // Verify
        val saved = orderRepo.findOrderById(orderId)
        assertNotNull(saved)
        assertEquals(testCustomerId, saved?.customerId)
        assertEquals("Pending", saved?.status)
    }
    
    @Test
    fun `should insert order items`() {
        // Given - criar ordem primeiro
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        assertTrue(orderId > 0)
        
        val items = listOf(
            OrderItem(
                orderId = orderId,
                productId = testProductId,
                quantity = 2,
                unitPrice = 9.99,
                subtotal = 19.98
            )
        )
        
        // When
        val success = orderRepo.insertOrderItems(orderId, items)
        
        // Then
        assertTrue(success)
        
        // Verify
        val savedItems = orderRepo.getOrderItems(orderId)
        assertNotNull(savedItems)
        assertEquals(1, savedItems.size)
        assertEquals(testProductId, savedItems[0].productId)
        assertEquals(2, savedItems[0].quantity)
        assertEquals(19.98, savedItems[0].subtotal)
    }
    
    @Test
    fun `should get orders by customer`() {
        // Given - criar uma ordem
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // When
        val orders = orderRepo.getOrdersByCustomer(testCustomerId)
        
        // Then
        assertNotNull(orders)
        assertTrue(orders.isNotEmpty())
        assertTrue(orders.any { it.orderId == orderId })
    }
    
    @Test
    fun `should get orders by status`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // When
        val pendingOrders = orderRepo.getOrdersByStatus("Pending")
        
        // Then
        assertNotNull(pendingOrders)
        assertTrue(pendingOrders.isNotEmpty())
        assertTrue(pendingOrders.any { it.orderId == orderId })
    }
    
    @Test
    fun `should update order status`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // When
        val success = orderRepo.updateOrderStatus(orderId, "Delivered")
        
        // Then
        assertTrue(success)
        
        // Verify
        val updated = orderRepo.findOrderById(orderId)
        assertNotNull(updated)
        assertEquals("Delivered", updated?.status)
    }
    
    @Test
    fun `should cancel order`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // When
        val success = orderRepo.cancelOrder(orderId)
        
        // Then
        assertTrue(success)
        
        // Verify
        val cancelled = orderRepo.findOrderById(orderId)
        assertNotNull(cancelled)
        assertEquals("Cancelled", cancelled?.status)
    }
    
    @Test
    fun `should update order total`() {
        // Given - criar ordem com itens
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 0.0
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        val items = listOf(
            OrderItem(
                orderId = orderId,
                productId = testProductId,
                quantity = 3,
                unitPrice = 9.99,
                subtotal = 29.97
            )
        )
        orderRepo.insertOrderItems(orderId, items)
        
        // When
        val success = orderRepo.updateOrderTotal(orderId)
        
        // Then
        assertTrue(success)
        
        // Verify
        val updated = orderRepo.findOrderById(orderId)
        assertNotNull(updated)
        assertEquals(29.97, updated?.totalAmount)
    }
    
    @Test
    fun `should get order summary`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        val items = listOf(
            OrderItem(
                orderId = orderId,
                productId = testProductId,
                quantity = 2,
                unitPrice = 9.99,
                subtotal = 19.98
            )
        )
        orderRepo.insertOrderItems(orderId, items)
        orderRepo.updateOrderTotal(orderId)
        
        // When
        val summary = orderRepo.getOrderSummary(orderId)
        
        // Then
        assertNotNull(summary)
        assertEquals(orderId, summary?.get("orderId"))
        assertEquals(1, summary?.get("itemCount"))
        assertEquals(2, summary?.get("totalQuantity"))
    }
    
    @Test
    fun `should get most popular products`() {
        // When
        val products = orderRepo.getMostPopularProducts(5)
        
        // Then
        assertNotNull(products)
        // Can't guarantee it's not empty, but should be a list
        assertTrue(products is List<*>)
    }
    
    @Test
    fun `should get top customers`() {
        // When
        val customers = orderRepo.getTopCustomers(5)
        
        // Then
        assertNotNull(customers)
        assertTrue(customers is List<*>)
    }
    
    @Test
    fun `should delete order`() {
        // Given
        val order = Order(
            customerId = testCustomerId,
            status = "Pending",
            totalAmount = 19.98
        )
        val orderId = orderRepo.insertOrder(order)
        testOrderId = orderId
        
        // When
        val success = orderRepo.deleteOrder(orderId)
        
        // Then
        assertTrue(success)
        
        // Verify - should be gone
        val found = orderRepo.findOrderById(orderId)
        assertNull(found)
    }
}
