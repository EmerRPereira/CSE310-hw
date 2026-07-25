# Ice Cream Shop - SQL Relational Database Integration

## Overview

This project demonstrates the integration of a SQL relational database (PostgreSQL) with a Kotlin application. As a software engineer, I developed this application to deepen my understanding of database connectivity, CRUD operations, and the practical application of SQL within a real-world software context. The project extends a previous Kotlin console application by replacing in-memory data storage with a persistent PostgreSQL database, implementing a complete data layer with repositories, and adding advanced SQL features such as complex queries, views, and transaction management.

The software is a comprehensive **Ice Cream Shop Management System** that allows users to manage customers, products, shopping carts, and orders through an interactive console interface. It demonstrates the seamless integration between Kotlin and PostgreSQL using JDBC, showcasing best practices in database design, code organization, and error handling.

[Software Demo Video](https://youtu.be/your-video-link-here)

## Development Environment

### Tools
- **IntelliJ IDEA** - Primary IDE for Kotlin development
- **PostgreSQL 18** - Relational database management system
- **pgAdmin 4** - Database administration and query tool
- **Git & GitHub** - Version control and code repository
- **VS Code** - Additional code editing and SQL script management

### Programming Language and Libraries
- **Kotlin (JVM)** - Main programming language
- **PostgreSQL JDBC Driver (42.7.3)** - Database connectivity
- **Kotlin Standard Library** - Core language features
- **Java JDK 17** - Runtime environment

### Database Schema
The application uses a normalized database schema with six main tables:
- `customers` - Stores customer information
- `categories` - Product categories
- `products` - Ice cream products with prices
- `payment_types` - Available payment methods
- `orders` - Customer orders with status tracking
- `order_items` - Individual items within orders

## Project Structure
ice-cream-shop/
'├── src/main/kotlin/com/icecream/
'│ ├── Main.kt # Application entry point
'│ ├── models/ # Data classes
'│ │ ├── Customer.kt
'│ │ ├── Product.kt
'│ │ └── Order.kt
'│ ├── database/ # Database layer
'│ │ ├── DatabaseConnection.kt # Connection management
'│ │ ├── CustomerRepository.kt
'│ │ ├── ProductRepository.kt
'│ │ └── OrderRepository.kt
'│ └── services/ # Business logic
'│ ├── MenuService.kt
'│ └── OrderService.kt
'├── database/ # SQL scripts
'│ ├── 01_create_database.sql
'│ ├── 02_create_tables.sql
'│ ├── 03_insert_data.sql
'│ ├── 04_create_views.sql
'│ └── 05_create_indexes.sql
'├── build.gradle.kts # Build configuration
'└── README.md


## Key Features

### 1. Customer Management
- Create, view, search, and update customer information
- Set current customer for order processing
- Soft delete functionality (archive customers)

### 2. Product Management
- View menu with products organized by category
- Add new products to the database
- Toggle product availability
- Search products by name

### 3. Shopping Cart
- Add/remove products with quantity management
- Real-time total calculation
- Persistent cart session

### 4. Order Processing
- Complete checkout with customer validation
- Multiple payment methods support
- Order status tracking (Pending, Preparing, Ready, Delivered, Cancelled)
- Automatic total calculation with database triggers
- Transaction management for data integrity

### 5. Reports
- Most popular products by sales volume
- Top customers by total spending
- Order history by customer

## SQL Features Implemented

- **Complex JOINs**: Combining multiple tables for order summaries
- **Aggregate Functions**: SUM, COUNT, AVG for sales reports
- **Subqueries**: For data analysis and reporting
- **Views**: Predefined queries for common reports
- **Indexes**: Performance optimization on frequently queried columns
- **Transactions**: Ensuring data consistency during order creation
- **Constraints**: Primary keys, foreign keys, CHECK constraints
- **Triggers**: Automatic order total updates

### Prerequisites
1. PostgreSQL 18 installed and running
2. JDK 17 or higher
3. Git (optional)

### Setup Database
```bash
# Create database and tables
psql -U postgres -f database/01_create_database.sql
psql -U postgres -d ice_cream_shop -f database/02_create_tables.sql
psql -U postgres -d ice_cream_shop -f database/03_insert_data.sql

## How to Run
# Run Application
## Using Gradle
gradlew.bat run
## Or using compiled JAR
java -cp "main.jar;lib/postgresql-42.7.3.jar" com.icecream.MainKt

