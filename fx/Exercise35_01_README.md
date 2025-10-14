# Exercise 35.1 - Batch Update Performance Demonstration

A JavaFX application that compares the performance difference between batch updates and individual database updates when inserting 1000 records.

## Features

- **Database Connection Dialog**: Custom DBConnectionPanel for entering database credentials
- **Batch Update**: Inserts 1000 records using JDBC batch processing
- **Non-Batch Update**: Inserts 1000 records using individual INSERT statements
- **Performance Timing**: Measures and displays elapsed time for both approaches
- **Random Data Generation**: Uses Math.random() to generate test data

## Database Setup

The program creates a temporary table with this structure:
```sql
CREATE TABLE Temp(num1 DOUBLE, num2 DOUBLE, num3 DOUBLE)
```

## How to Use

1. **Run the Application**: Launch Exercise35_01
2. **Connect to Database**: 
   - Click "Connect to Database" button
   - Enter your database connection details in the dialog
   - Default values are provided for MySQL
   - Click "Connect to DB"
3. **Test Performance**:
   - Click "Batch Update" to test batch processing (faster)
   - Click "Non Batch Update" to test individual inserts (slower)
   - Compare the elapsed times shown in the results area

## Expected Results

Batch updates should be significantly faster than individual updates because:

- **Batch Updates**: All 1000 INSERT statements are prepared and sent to the database in one batch, reducing network overhead and allowing database optimization
- **Individual Updates**: Each of the 1000 INSERT statements requires a separate round-trip to the database

Typical performance improvement with batch updates can be 10x to 100x faster depending on:
- Network latency
- Database server performance
- JDBC driver implementation
- Database configuration

## Connection Panel Fields

- **JDBC Driver**: Database driver class (default: com.mysql.cj.jdbc.Driver)
- **Database URL**: Connection URL (default: jdbc:mysql://localhost/york)
- **Username**: Database username (default: scott)
- **Password**: Database password (default: tiger)

## Technical Implementation

### Batch Update Process:
```java
PreparedStatement pstmt = connection.prepareStatement(sql);
for (int i = 0; i < 1000; i++) {
    pstmt.setDouble(1, Math.random());
    pstmt.setDouble(2, Math.random());
    pstmt.setDouble(3, Math.random());
    pstmt.addBatch();  // Add to batch
}
pstmt.executeBatch();  // Execute all at once
```

### Individual Update Process:
```java
PreparedStatement pstmt = connection.prepareStatement(sql);
for (int i = 0; i < 1000; i++) {
    pstmt.setDouble(1, Math.random());
    pstmt.setDouble(2, Math.random());
    pstmt.setDouble(3, Math.random());
    pstmt.executeUpdate();  // Execute immediately
}
```

## Learning Objectives

This exercise demonstrates:
- The performance benefits of JDBC batch processing
- How to implement batch updates in Java
- Database connection management in JavaFX applications
- Custom dialog creation and modal windows
- Performance measurement and comparison
- Practical application of PreparedStatement batch operations

## Requirements

- Java 8+ with JavaFX
- MySQL database server
- MySQL JDBC Connector (mysql-connector-java)
- Database with appropriate permissions for creating tables and inserting data

The dramatic performance difference shown by this program illustrates why batch processing is essential for applications that need to perform many database operations efficiently.