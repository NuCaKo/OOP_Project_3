# Local Greengrocer Application

A JavaFX desktop application for a local greengrocer, featuring separate interfaces for Customers, Owners, and Carriers.

## Prerequisites

Before running the application, ensure you have the following installed on your macOS system:

1.  **Java Development Kit (JDK) 17 or higher**
    *   Verify with `java -version` in your terminal.
2.  **Apache Maven**
    *   Verify with `mvn -version`.
    *   If not installed, you can use Homebrew: `brew install maven`.
3.  **MySQL Server**
    *   Verify with `mysql --version`.
    *   If not installed: `brew install mysql`.
    *   Start the service: `brew services start mysql`.

## Database Setup

The application requires a MySQL user with specific credentials to automatically create and manage the database.

1.  Open your MySQL terminal:
    ```bash
    mysql -u root -p
    ```
2.  Run the following commands to create the user and grant permissions:
    ```sql
    CREATE USER 'myuser'@'localhost' IDENTIFIED BY '1234';
    GRANT ALL PRIVILEGES ON *.* TO 'myuser'@'localhost';
    FLUSH PRIVILEGES;
    EXIT;
    ```
    *Note: The application will automatically create the database `greengrocer_db` and all necessary tables upon the first run.*

## How to Run

1.  Navigate to the project root directory in your terminal.
2.  Run the application using Maven:
    ```bash
    mvn javafx:run
    ```

## Usage Credentials

The application comes seeded with default users for testing:

*   **Customer**:
    *   Username: `cust`
    *   Password: `cust`
*   **Owner**:
    *   Username: `own`
    *   Password: `own`
*   **Carrier**:
    *   Username: `carr`
    *   Password: `carr`

## Troubleshooting on macOS

*   **"Database connection error"**: Ensure MySQL is running (`brew services list`) and the `myuser` user exists with the password `1234`.
*   **"JavaFX runtime components are missing"**: This project uses Maven to handle JavaFX dependencies. Ensure you are running with `mvn javafx:run` rather than trying to run the JAR directly without shading.
