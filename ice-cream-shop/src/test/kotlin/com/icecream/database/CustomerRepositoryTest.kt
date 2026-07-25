package com.icecream.database

import com.icecream.models.Customer
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Testes para o CustomerRepository
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerRepositoryTest {
    
    private lateinit var repository: CustomerRepository
    private var testCustomerId: Int = -1
    
    @BeforeAll
    fun setup() {
        repository = CustomerRepository()
        println("🧪 Iniciando testes do CustomerRepository...")
    }
    
    @BeforeEach
    fun setupEach() {
        // Criar um cliente de teste antes de cada teste
        val customer = Customer(
            name = "Test User ${System.currentTimeMillis()}",
            phone = "(11) 99999-9999",
            email = "test.${System.currentTimeMillis()}@email.com",
            address = "Rua Teste, 123"
        )
        testCustomerId = repository.insertCustomer(customer)
        println("✅ Cliente de teste criado: ID $testCustomerId")
    }
    
    @AfterEach
    fun cleanupEach() {
        // Limpar cliente de teste após cada teste
        if (testCustomerId > 0) {
            repository.hardDeleteCustomer(testCustomerId)
            println("🧹 Cliente de teste removido: ID $testCustomerId")
        }
    }
    
    @Test
    fun `should insert a customer successfully`() {
        // Given
        val customer = Customer(
            name = "João Silva",
            phone = "(11) 98888-7777",
            email = "joao.silva@teste.com",
            address = "Av. Teste, 456"
        )
        
        // When
        val id = repository.insertCustomer(customer)
        
        // Then
        assertTrue(id > 0, "ID do cliente deve ser positivo")
        
        // Verify
        val savedCustomer = repository.findCustomerById(id)
        assertNotNull(savedCustomer, "Cliente deve ser encontrado")
        assertEquals("João Silva", savedCustomer?.name)
        
        // Cleanup
        repository.hardDeleteCustomer(id)
    }
    
    @Test
    fun `should find customer by id`() {
        // When
        val found = repository.findCustomerById(testCustomerId)
        
        // Then
        assertNotNull(found, "Cliente deve ser encontrado")
        assertEquals(testCustomerId, found?.customerId)
        assertTrue(found?.name?.startsWith("Test User") == true)
    }
    
    @Test
    fun `should find customer by email`() {
        // Given
        val customer = repository.findCustomerById(testCustomerId)
        assertNotNull(customer)
        
        // When
        val found = repository.findCustomerByEmail(customer!!.email)
        
        // Then
        assertNotNull(found)
        assertEquals(customer.customerId, found?.customerId)
        assertEquals(customer.email, found?.email)
    }
    
    @Test
    fun `should update customer correctly`() {
        // Given
        val customer = repository.findCustomerById(testCustomerId)
        assertNotNull(customer)
        
        val updatedCustomer = customer!!.copy(
            name = "Updated Name",
            phone = "(11) 97777-8888"
        )
        
        // When
        val success = repository.updateCustomer(updatedCustomer)
        
        // Then
        assertTrue(success, "Atualização deve ser bem-sucedida")
        
        // Verify
        val saved = repository.findCustomerById(testCustomerId)
        assertNotNull(saved)
        assertEquals("Updated Name", saved?.name)
        assertEquals("(11) 97777-8888", saved?.phone)
    }
    
    @Test
    fun `should soft delete customer`() {
        // When
        val success = repository.deleteCustomer(testCustomerId)
        
        // Then
        assertTrue(success, "Delete deve ser bem-sucedido")
        
        // Verify - não deve encontrar
        val found = repository.findCustomerById(testCustomerId)
        assertNull(found, "Cliente não deve ser encontrado após soft delete")
        
        // Get all customers - não deve incluir o deletado
        val allCustomers = repository.getAllCustomers()
        assertFalse(allCustomers.any { it.customerId == testCustomerId })
    }
    
    @Test
    fun `should get all customers`() {
        // When
        val customers = repository.getAllCustomers()
        
        // Then
        assertNotNull(customers)
        assertTrue(customers.isNotEmpty())
        
        // At least one customer should exist
        val hasTestCustomer = customers.any { it.customerId == testCustomerId }
        assertTrue(hasTestCustomer, "Deve conter o cliente de teste")
    }
    
    @Test
    fun `should search customers by name`() {
        // When
        val results = repository.findCustomersByName("Test User")
        
        // Then
        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.customerId == testCustomerId })
    }
    
    @Test
    fun `should return correct customer count`() {
        // When
        val count = repository.getCustomerCount()
        
        // Then
        assertTrue(count > 0, "Deve haver pelo menos 1 cliente")
    }
}
