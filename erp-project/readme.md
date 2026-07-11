
# 🍦 DachsIce Ice Cream Shop
A simple console-based ice cream ordering system built with Kotlin.


## Quick Start

kotlinc Main.kt -include-runtime -d Main.jar

java -jar Main.jar

### Prerequisites
- **Java JDK 11+** (Java 17 or 21 recommended for stability)
- **Kotlin Compiler** (included with IntelliJ IDEA or downloadable separately)
- **IntelliJ IDEA** (or any Kotlin-compatible IDE)
- **VS Code** (with extensions Kotlin)

## Code Structure
Main.kt

'├── MenuItem (data class)

'│       ├── id: Int

'│       ├── name: String

'│       ├── price: Double

'│       └── category: String

'│

'├── Order (class)

'│      ├── addItem()

'│      ├── removeItem()

'│      ├── calculateTotal()

'│      ├── getItems()

'│      └── clear()

'│

'├── main()

'│      ├── Initialize menu

'│      ├── Display main menu

'│      └── Handle user input

'│

'├── displayMenu()

'├── addItemToOrder()

'├── displayOrder()

'└── checkout()

## Task List
- [x] Kotlin
- [ ] SQL
- [ ] Mobile APP

```
Emerson Ronald Pereira
CSE310 course
```
