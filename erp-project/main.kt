// Main.kt - Console Version

import java.util.Scanner

/**
 * Represents a menu item
 * Using data class for automatic functionality
 */
data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String
)

/**
 * Represents an order in progress
 */
class Order {
    private val items = mutableListOf<Pair<MenuItem, Int>>()
    
    fun addItem(menuItem: MenuItem, quantity: Int) {
        val existing = items.find { it.first.id == menuItem.id }
        if (existing != null) {
            val index = items.indexOf(existing)
            items[index] = existing.first to (existing.second + quantity)
        } else {
            items.add(menuItem to quantity)
        }
    }
    
    fun removeItem(menuItemId: Int) {
        items.removeAll { it.first.id == menuItemId }
    }
    
    fun calculateTotal(): Double {
        return items.sumOf { it.first.price * it.second }
    }
    
    fun getItems(): List<Pair<MenuItem, Int>> {
        return items.toList()
    }
    
    fun clear() {
        items.clear()
    }
}

/**
 * Main menu - console version
 */
fun main() {
    val scanner = Scanner(System.`in`)
    
    // Creating menu using list
    val menu = listOf(
        MenuItem(1, "Simple Cone", 5.00, "Cone"),
        MenuItem(2, "Double Cone", 8.00, "Cone"),
        MenuItem(3, "Chocolate Sundae", 12.00, "Sundae"),
        MenuItem(4, "Strawberry Sundae", 12.00, "Sundae"),
        MenuItem(5, "Vanilla Milkshake", 15.00, "Milkshake"),
        MenuItem(6, "Chocolate Milkshake", 15.00, "Milkshake"),
        MenuItem(7, "Cream Ice Cream (1 scoop)", 4.00, "Ice Cream"),
        MenuItem(8, "Cream Ice Cream (2 scoops)", 7.00, "Ice Cream")
    )
    
    val order = Order()
    var running = true
    
    println("=".repeat(50))
    println("      WELCOME TO DACHSICE ICE CREAM SHOP ")
    println("=".repeat(50))
    
    while (running) {
        println("\n--- MAIN MENU ---")
        println("1 - View Menu")
        println("2 - Add Item to Order")
        println("3 - View Cart")
        println("4 - Checkout")
        println("5 - Exit")
        print("Choose an option: ")
        
        when (readlnOrNull()?.toIntOrNull()) {
            1 -> displayMenu(menu)
            2 -> addItemToOrder(menu, order, scanner)
            3 -> displayOrder(order)
            4 -> checkout(order)
            5 -> {
                println("Thank you for visiting! Come back soon!")
                running = false
            }
            else -> println("Invalid option! Please try again.")
        }
    }
    
    scanner.close()
}

/**
 * Displays the menu
 */
fun displayMenu(menu: List<MenuItem>) {
    println("\n--- MENU ---")
    // Grouping by category using expressions
    val categories = menu.map { it.category }.distinct()
    
    categories.forEach { category ->
        println("\n $category:")
        menu.filter { it.category == category }.forEach { item ->
            println("   ${item.id}. ${item.name} - $${"%.2f".format(item.price)}")
        }
    }
}

/**
 * Adds item to order with validation
 */
fun addItemToOrder(menu: List<MenuItem>, order: Order, scanner: Scanner) {
    displayMenu(menu)
    
    print("\nEnter the item number: ")
    val id = readlnOrNull()?.toIntOrNull()
    
    if (id == null) {
        println("Invalid ID!")
        return
    }
    
    val item = menu.find { it.id == id }
    if (item == null) {
        println("Item not found!")
        return
    }
    
    print("Enter the quantity: ")
    val quantity = readlnOrNull()?.toIntOrNull() ?: 1
    
    if (quantity <= 0) {
        println("Invalid quantity!")
        return
    }
    
    order.addItem(item, quantity)
    println(" ${item.name} added to order!")
}

/**
 * Displays the current cart
 */
fun displayOrder(order: Order) {
    val items = order.getItems()
    
    if (items.isEmpty()) {
        println("\n Your cart is empty!")
        return
    }
    
    println("\n--- YOUR ORDER ---")
    items.forEach { (item, quantity) ->
        println("   ${item.name} x$quantity = $${"%.2f".format(item.price * quantity)}")
    }
    
    val total = order.calculateTotal()
    println("=".repeat(30))
    println("   TOTAL: $${"%.2f".format(total)}")
}

/**
 * Finalizes the order
 */
fun checkout(order: Order) {
    val items = order.getItems()
    
    if (items.isEmpty()) {
        println("Empty cart! Please add some items.")
        return
    }
    
    println("\n--- CHECKOUT ---")
    displayOrder(order)
    
    print("Customer name: ")
    val name = readlnOrNull() ?: "Customer"
    
    println("\n Order finalized for $name!")
    println(" Total to pay: $${"%.2f".format(order.calculateTotal())}")
    println(" Status: Pending")
    println("\nThank you for your preference!")
    
    order.clear()
}
