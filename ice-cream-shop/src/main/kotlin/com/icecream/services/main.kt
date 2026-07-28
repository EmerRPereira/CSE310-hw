package com.icecream

import com.icecream.database.CustomerRepository
import com.icecream.database.DatabaseConnection
import com.icecream.database.ProductRepository
import com.icecream.database.OrderRepository
import com.icecream.models.Customer
import com.icecream.models.Product
import com.icecream.models.ShoppingCart
import com.icecream.services.MenuService
import com.icecream.services.OrderService
import com.icecream.services.OrderResult
import java.util.Scanner

/**
 * Represents a menu item for display
 */
data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String
)

/**
 * Main application for Dachsice Ice Cream Shop
 */
fun main() {
    val scanner = Scanner(System.`in`)
    var running = true
    
    // Initialize services and repositories
    val customerRepo = CustomerRepository()
    val productRepo = ProductRepository()
    val orderRepo = OrderRepository()
    val menuService = MenuService()
    val orderService = OrderService()
    val cart = ShoppingCart()
    
    // Current user session
    var currentCustomer: Customer? = null
    
    // Welcome
    println("=".repeat(50))
    println("      🍦 DACHSICE ICE CREAM SHOP 🍦")
    println("=".repeat(50))
    
    // Check database connection
    println("\n🔌 Checking database connection...")
    if (DatabaseConnection.testConnection()) {
        println("✅ Database connected successfully!")
        val productCount = productRepo.getProductCount()
        println("📦 $productCount products available in database")
    } else {
        println("⚠️  Database not available. Running in limited mode.")
        println("   Make sure PostgreSQL is running and the database 'ice_cream_shop' exists.")
    }
    
    // Main menu loop
    while (running) {
        println("\n--- MAIN MENU ---")
        println("1 - View Menu")
        println("2 - Customer Management")
        println("3 - Shopping Cart")
        println("4 - Checkout")
        println("5 - View Orders")
        println("6 - Reports")
        println("7 - Admin (Products)")
        println("8 - Help")
        println("9 - Exit")
        print("\nChoose an option: ")
        
        when (readlnOrNull()?.toIntOrNull()) {
            1 -> viewMenu(productRepo)  // ← Agora recarrega do banco
            2 -> customerMenu(scanner, customerRepo, currentCustomer) { currentCustomer = it }
            3 -> shoppingCartMenu(scanner, productRepo, cart)  // ← Agora com validação
            4 -> checkout(scanner, orderService, cart, currentCustomer)
            5 -> viewOrders(orderRepo, currentCustomer)
            6 -> reportsMenu(scanner, orderRepo)
            7 -> adminMenu(scanner, menuService, productRepo)  // ← Agora recarrega menu
            8 -> showHelp()
            9 -> {
                println("\n🍦 Thank you for visiting Dachsice Ice Cream Shop!")
                println("👋 Come back soon!")
                running = false
            }
            else -> println("❌ Invalid option! Please try again.")
        }
    }
    
    scanner.close()
    DatabaseConnection.closeConnection()
}

// =====================================================
// VIEW MENU - CORRIGIDO (recarrega do banco)
// =====================================================
fun viewMenu(productRepo: ProductRepository) {
    val products = productRepo.getAllAvailableProducts()
    
    if (products.isEmpty()) {
        println("\n📭 No products available.")
        return
    }
    
    println("\n🍦 ICE CREAM MENU 🍦")
    println("=".repeat(40))
    
    val categories = products.map { it.categoryName ?: "Uncategorized" }.distinct()
    categories.forEach { category ->
        println("\n📌 ${category.uppercase()}:")
        products.filter { it.categoryName == category }.forEach { product ->
            println("  ${product.productId}. ${product.name} - $${"%.2f".format(product.price)}")
        }
    }
    println("\n" + "=".repeat(40))
}

// =====================================================
// CUSTOMER MANAGEMENT
// =====================================================
fun customerMenu(scanner: Scanner, repo: CustomerRepository, currentCustomer: Customer?, updateCustomer: (Customer?) -> Unit) {
    println("\n--- CUSTOMER MANAGEMENT ---")
    println("1 - View all customers")
    println("2 - Search customer")
    println("3 - Add new customer")
    println("4 - Set current customer")
    println("5 - View current customer")
    println("6 - Back to main menu")
    print("\nChoose an option: ")
    
    when (readlnOrNull()?.toIntOrNull()) {
        1 -> {
            val customers = repo.getAllCustomers()
            if (customers.isEmpty()) {
                println("📭 No customers found.")
            } else {
                println("\n📋 CUSTOMERS:")
                customers.forEach { customer ->
                    println("  #${customer.customerId} - ${customer.name} (${customer.email})")
                }
            }
        }
        2 -> {
            println("\n--- SEARCH CUSTOMER ---")
            println("1 - Search by name")
            println("2 - Search by ID")
            print("Choose option: ")
            
            when (readlnOrNull()?.toIntOrNull()) {
                1 -> {
                    print("Enter customer name: ")
                    val name = readlnOrNull()?.trim() ?: ""
                    if (name.isNotEmpty()) {
                        val customers = repo.findCustomersByName(name)
                        if (customers.isEmpty()) {
                            println("📭 No customers found matching '$name'")
                        } else {
                            println("\n📋 SEARCH RESULTS:")
                            customers.forEach { customer ->
                                println("  #${customer.customerId} - ${customer.name} (${customer.email})")
                            }
                        }
                    }
                }
                2 -> {
                    print("Enter customer ID: ")
                    val id = readlnOrNull()?.toIntOrNull()
                    if (id != null) {
                        val customer = repo.findCustomerById(id)
                        if (customer != null) {
                            println("  #${customer.customerId} - ${customer.name} (${customer.email})")
                        } else {
                            println("📭 No customer found with ID $id")
                        }
                    } else {
                        println("❌ Invalid ID!")
                    }
                }
                else -> println("❌ Invalid option!")
            }
        }
        3 -> {
            println("\n--- ADD NEW CUSTOMER ---")
            print("Name: ")
            val name = readlnOrNull()?.trim() ?: ""
            print("Phone: ")
            val phone = readlnOrNull()?.trim() ?: ""
            print("Email: ")
            val email = readlnOrNull()?.trim() ?: ""
            print("Address: ")
            val address = readlnOrNull()?.trim() ?: ""
            
            if (name.isBlank() || phone.isBlank() || email.isBlank() || address.isBlank()) {
                println("❌ All fields are required!")
                return
            }
            
            val customer = Customer(
                name = name,
                phone = phone,
                email = email,
                address = address
            )
            
            val id = repo.insertCustomer(customer)
            if (id > 0) {
                println("✅ Customer added successfully! ID: $id")
                updateCustomer(repo.findCustomerById(id))
            } else {
                println("❌ Failed to add customer. Email might already exist.")
            }
        }
        4 -> {
            print("\nEnter customer ID: ")
            val id = readlnOrNull()?.toIntOrNull()
            if (id != null) {
                val customer = repo.findCustomerById(id)
                if (customer != null) {
                    updateCustomer(customer)
                    println("✅ Current customer set to: ${customer.name}")
                } else {
                    println("❌ Customer not found!")
                }
            } else {
                println("❌ Invalid ID!")
            }
        }
        5 -> {
            if (currentCustomer != null) {
                println("\n👤 CURRENT CUSTOMER:")
                println("  ID: ${currentCustomer.customerId}")
                println("  Name: ${currentCustomer.name}")
                println("  Email: ${currentCustomer.email}")
                println("  Phone: ${currentCustomer.phone}")
            } else {
                println("ℹ️  No customer selected.")
            }
        }
        6 -> println("Returning to main menu...")
        else -> println("❌ Invalid option!")
    }
}

// =====================================================
// SHOPPING CART - CORRIGIDO (valida disponibilidade)
// =====================================================
fun shoppingCartMenu(scanner: Scanner, productRepo: ProductRepository, cart: ShoppingCart) {
    println("\n--- SHOPPING CART ---")
    println("1 - View cart")
    println("2 - Add item")
    println("3 - Remove item")
    println("4 - Update quantity")
    println("5 - Clear cart")
    println("6 - Back to main menu")
    print("\nChoose an option: ")
    
    when (readlnOrNull()?.toIntOrNull()) {
        1 -> println("\n${cart}")
        2 -> {
            // Recarrega o menu sempre para mostrar apenas produtos disponíveis
            viewMenu(productRepo)
            
            print("\nEnter product ID to add: ")
            val id = readlnOrNull()?.toIntOrNull()
            if (id != null) {
                // Verifica disponibilidade diretamente no banco
                val product = productRepo.findProductById(id)
                if (product == null) {
                    println("❌ Product not found!")
                    return
                }
                if (!product.isAvailable) {
                    println("❌ This product is currently unavailable!")
                    return
                }
                
                print("Quantity: ")
                val quantity = readlnOrNull()?.toIntOrNull() ?: 1
                if (quantity > 0) {
                    cart.addItem(product, quantity)
                    println("✅ ${quantity}x ${product.name} added to cart!")
                } else {
                    println("❌ Invalid quantity!")
                }
            } else {
                println("❌ Invalid ID!")
            }
        }
        3 -> {
            if (cart.isEmpty()) {
                println("📭 Cart is empty!")
                return
            }
            println("\n${cart}")
            print("\nEnter product ID to remove: ")
            val id = readlnOrNull()?.toIntOrNull()
            if (id != null) {
                cart.removeItem(id)
                println("✅ Item removed from cart!")
            } else {
                println("❌ Invalid ID!")
            }
        }
        4 -> {
            if (cart.isEmpty()) {
                println("📭 Cart is empty!")
                return
            }
            println("\n${cart}")
            print("\nEnter product ID to update: ")
            val id = readlnOrNull()?.toIntOrNull()
            if (id != null) {
                print("New quantity: ")
                val quantity = readlnOrNull()?.toIntOrNull()
                if (quantity != null) {
                    cart.updateQuantity(id, quantity)
                    println("✅ Quantity updated!")
                } else {
                    println("❌ Invalid quantity!")
                }
            } else {
                println("❌ Invalid ID!")
            }
        }
        5 -> {
            cart.clear()
            println("✅ Cart cleared!")
        }
        6 -> println("Returning to main menu...")
        else -> println("❌ Invalid option!")
    }
}

// =====================================================
// CHECKOUT
// =====================================================
fun checkout(scanner: Scanner, orderService: OrderService, cart: ShoppingCart, currentCustomer: Customer?) {
    if (cart.isEmpty()) {
        println("📭 Cart is empty! Add some items first.")
        return
    }
    
    if (currentCustomer == null) {
        println("❌ No customer selected! Please set a customer first.")
        return
    }
    
    println("\n--- CHECKOUT ---")
    println("Customer: ${currentCustomer.name}")
    println("\n${cart}")
    
    print("\nDelivery address (optional): ")
    val address = readlnOrNull()?.trim() ?: ""
    print("Special instructions (optional): ")
    val instructions = readlnOrNull()?.trim() ?: ""
    
    println("\nPayment methods:")
    println("  1 - Cash")
    println("  2 - Credit Card")
    println("  3 - Debit Card")
    println("  4 - Pix")
    print("Choose payment method: ")
    val paymentId = when (readlnOrNull()?.toIntOrNull()) {
        1 -> 1
        2 -> 2
        3 -> 3
        4 -> 4
        else -> null
    }
    
    if (paymentId == null) {
        println("❌ Invalid payment method!")
        return
    }
    
    println("\n🔍 Processing order...")
    
    val result = orderService.createOrderFromCart(
        customerId = currentCustomer.customerId!!,
        cart = cart,
        deliveryAddress = address.ifEmpty { null },
        specialInstructions = instructions.ifEmpty { null },
        paymentTypeId = paymentId
    )
    
    when (result) {
        is OrderResult.Success -> {
            println("\n✅ ORDER CONFIRMED!")
            println("Order #${result.orderId}")
            if (result.summary != null) {
                println("  Customer: ${result.summary["customerName"]}")
                println("  Total Items: ${result.summary["itemCount"]}")
                println("  Total Amount: $${"%.2f".format(result.summary["totalAmount"])}")
                println("  Status: ${result.summary["status"]}")
            }
            println("\n🎉 Thank you for your order, ${currentCustomer.name}!")
        }
        is OrderResult.Error -> {
            println("\n❌ ${result.message}")
        }
    }
}

// =====================================================
// VIEW ORDERS
// =====================================================
fun viewOrders(orderRepo: OrderRepository, currentCustomer: Customer?) {
    if (currentCustomer == null) {
        println("❌ No customer selected! Please set a customer first.")
        return
    }
    
    val orders = orderRepo.getOrdersByCustomer(currentCustomer.customerId!!)
    
    if (orders.isEmpty()) {
        println("📭 No orders found for ${currentCustomer.name}.")
        return
    }
    
    println("\n📋 ORDER HISTORY FOR ${currentCustomer.name.uppercase()}")
    println("=".repeat(50))
    
    orders.forEach { order ->
        println("\nOrder #${order.orderId}")
        println("  Date: ${order.orderDate}")
        println("  Status: ${order.status}")
        println("  Total: $${"%.2f".format(order.totalAmount)}")
        
        val items = orderRepo.getOrderItems(order.orderId!!)
        if (items.isNotEmpty()) {
            println("  Items:")
            items.forEach { item ->
                println("    ${item.productName} x${item.quantity} = $${"%.2f".format(item.subtotal)}")
            }
        }
        println("  " + "-".repeat(30))
    }
}

// =====================================================
// REPORTS MENU
// =====================================================
fun reportsMenu(scanner: Scanner, orderRepo: OrderRepository) {
    println("\n--- REPORTS ---")
    println("1 - Most popular products")
    println("2 - Top customers")
    println("3 - Back to main menu")
    print("\nChoose an option: ")
    
    when (readlnOrNull()?.toIntOrNull()) {
        1 -> {
            println("\n🏆 MOST POPULAR PRODUCTS")
            println("=".repeat(40))
            val products = orderRepo.getMostPopularProducts(10)
            if (products.isEmpty()) {
                println("📭 No product data available.")
            } else {
                products.forEachIndexed { index, product ->
                    println("${index + 1}. ${product["productName"]}")
                    println("   Sold: ${product["totalSold"]} units")
                    println("   Revenue: $${"%.2f".format(product["totalRevenue"])}")
                    println("   Orders: ${product["orderCount"]}")
                    println()
                }
            }
        }
        2 -> {
            println("\n👑 TOP CUSTOMERS")
            println("=".repeat(40))
            val customers = orderRepo.getTopCustomers(10)
            if (customers.isEmpty()) {
                println("📭 No customer data available.")
            } else {
                customers.forEachIndexed { index, customer ->
                    println("${index + 1}. ${customer["customerName"]}")
                    println("   Orders: ${customer["totalOrders"]}")
                    println("   Total spent: $${"%.2f".format(customer["totalSpent"])}")
                    println("   Avg order: $${"%.2f".format(customer["averageOrderValue"])}")
                    println()
                }
            }
        }
        3 -> println("Returning to main menu...")
        else -> println("❌ Invalid option!")
    }
}

// =====================================================
// ADMIN MENU - CORRIGIDO (recarrega menu após alterações)
// =====================================================
fun adminMenu(scanner: Scanner, menuService: MenuService, productRepo: ProductRepository) {
    println("\n--- ADMIN - PRODUCT MANAGEMENT ---")
    println("1 - Add new product")
    println("2 - Toggle product availability")
    println("3 - View all products")
    println("4 - Search products")
    println("5 - Back to main menu")
    print("\nChoose an option: ")
    
    when (readlnOrNull()?.toIntOrNull()) {
        1 -> {
            println("\n--- ADD NEW PRODUCT ---")
            print("Name: ")
            val name = readlnOrNull()?.trim() ?: ""
            print("Price: ")
            val price = readlnOrNull()?.toDoubleOrNull()
            print("Category ID (optional): ")
            val categoryId = readlnOrNull()?.toIntOrNull()
            print("Description (optional): ")
            val description = readlnOrNull()?.trim()
            
            if (name.isBlank() || price == null) {
                println("❌ Name and price are required!")
                return
            }
            
            menuService.addProduct(name, price, categoryId, description)
            println("✅ Product added! Menu will update automatically.")
        }
        2 -> {
            print("\nEnter product ID: ")
            val id = readlnOrNull()?.toIntOrNull()
            if (id != null) {
                menuService.toggleProductAvailability(id)
                println("✅ Product availability toggled! Menu will update automatically.")
            } else {
                println("❌ Invalid ID!")
            }
        }
        3 -> {
            val products = productRepo.getAllProducts()
            if (products.isEmpty()) {
                println("📭 No products found.")
            } else {
                println("\n📋 ALL PRODUCTS:")
                products.forEach { product ->
                    val status = if (product.isAvailable) "✅" else "🔴"
                    println("  $status #${product.productId} - ${product.name} ($${"%.2f".format(product.price)})")
                }
            }
        }
        4 -> {
            print("\nSearch query: ")
            val query = readlnOrNull()?.trim() ?: ""
            val results = menuService.searchProducts(query)
            if (results.isEmpty()) {
                println("📭 No products found.")
            } else {
                println("\n📋 SEARCH RESULTS:")
                results.forEach { product ->
                    println("  ${product.productId}. ${product.name} - $${"%.2f".format(product.price)}")
                }
            }
        }
        5 -> println("Returning to main menu...")
        else -> println("❌ Invalid option!")
    }
}

// =====================================================
// HELP
// =====================================================
fun showHelp() {
    println("\n📖 HELP - DACHSICE ICE CREAM SHOP")
    println("=".repeat(40))
    println("Welcome to Dachsice! Here's how to use this app:")
    println()
    println("1. First, go to 'Customer Management' and set a customer.")
    println("2. View the menu and add items to your cart.")
    println("3. Review your cart and proceed to checkout.")
    println("4. Complete your order and view order history.")
    println()
    println("📝 TIPS:")
    println("  - You need a customer to place an order.")
    println("  - Products must be 'available' to be added to cart.")
    println("  - Check the 'Reports' section for popular items and top customers.")
    println()
    println("💡 TRY THESE:")
    println("  - Add a new customer and create an order.")
    println("  - Search for products by name.")
    println("  - View your order history.")
    println()
}
