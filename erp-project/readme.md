
# 🍦 DachsIce Ice Cream Shop
A simple console-based ice cream ordering system built with Kotlin.

## Quick Start
kotlinc Main.kt -include-runtime -d Main.jar
java -jar Main.jar

## Code Structure
'Main.kt

'├── MenuItem (data class)

'│   ├── id: Int

'│   ├── name: String

'│   ├── price: Double

'│   └── category: String

'│

'├── Order (class)

'│   ├── addItem()

'│   ├── removeItem()

'│   ├── calculateTotal()

'│   ├── getItems()

'│   └── clear()

'│

'├── main()

'│   ├── Initialize menu

'│   ├── Display main menu

'│   └── Handle user input

'│

'├── displayMenu()

'├── addItemToOrder()

'├── displayOrder()

'└── checkout()

