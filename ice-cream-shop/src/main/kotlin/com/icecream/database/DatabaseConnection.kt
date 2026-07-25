package com.icecream.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * Singleton class to manage database connection
 */
object DatabaseConnection {
    private const val URL = "jdbc:postgresql://localhost:5432/ice_cream_shop"
    private const val USER = "postgres"
    private const val PASSWORD = "ErpLu100" // Change this to your password
    
    private var connection: Connection? = null
    
    /**
     * Gets a connection to the database
     */
    fun getConnection(): Connection {
        if (connection == null || connection?.isClosed == true) {
            try {
                // Load PostgreSQL driver
                Class.forName("org.postgresql.Driver")
                connection = DriverManager.getConnection(URL, USER, PASSWORD)
                println("✅ Database connected successfully!")
            } catch (e: SQLException) {
                println("❌ Database connection failed: ${e.message}")
                throw e
            } catch (e: ClassNotFoundException) {
                println("❌ PostgreSQL Driver not found!")
                throw e
            }
        }
        return connection!!
    }
    
    /**
     * Tests if the database connection is working
     */
    fun testConnection(): Boolean {
        return try {
            getConnection().isValid(2)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Closes the database connection
     */
    fun closeConnection() {
        try {
            connection?.close()
            connection = null
            println("Database connection closed.")
        } catch (e: SQLException) {
            println("Error closing connection: ${e.message}")
        }
    }
}
