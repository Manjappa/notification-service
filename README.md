# Notification Service

A Spring Boot microservice that collects payment details (payment success or payment failed) and sends email notifications to merchants. All payment transactions are persisted in a PostgreSQL database.

## Features

- **Unified Payment API**: Single endpoint to handle both payment success and failed notifications
- **Database Persistence**: All payment details are saved to PostgreSQL database
- **Email Notifications**: Sends email notifications to merchants when payments are processed
- **RESTful API**: Simple REST endpoint to receive payment events
- **Input Validation**: Validates incoming payment data using Jakarta Validation
- **Transaction Management**: Prevents duplicate transactions using unique transaction IDs

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Maven**
- **PostgreSQL** (database)
- **Spring Data JPA** (database access)
- **Spring Mail** (for email notifications)
- **Lombok** (for reducing boilerplate code)

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- SMTP email server access (Gmail, Outlook, or custom SMTP server)

## Database Setup

1. **Create PostgreSQL Database:**
   ```sql
   CREATE DATABASE notification_db;
   ```

2. **Configure Database Connection:**
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/notification_db
   spring.datasource.username=postgres
   spring.datasource.password=your-password
   ```

   Or use environment variables:
   - `DATABASE_URL` (default: `jdbc:postgresql://localhost:5432/notification_db`)
   - `DATABASE_USERNAME` (default: `postgres`)
   - `DATABASE_PASSWORD` (default: `postgres`)

3. **Database Schema:**
   The table will be created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`), or you can run the SQL script manually:
   ```bash
   psql -U postgres -d notification_db -f src/main/resources/schema.sql
   ```

## Configuration

### Email Configuration

Configure your email settings in `src/main/resources/application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

**For Gmail users:**
- Enable 2-factor authentication
- Generate an App Password: https://myaccount.google.com/apppasswords
- Use the App Password instead of your regular password

**Using Environment Variables:**
You can also set these via environment variables:
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `SERVER_PORT` (default: 8080)

## Building the Project

```bash
mvn clean install
```

## Running the Application

1. **Start PostgreSQL** (if not running):
   ```bash
   # Windows
   net start postgresql-x64-14
   
   # Linux/Mac
   sudo systemctl start postgresql
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

   Or build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/notification-service-1.0.0.jar
   ```

The service will start on port 8080 by default.

## API Endpoints

### Payment Notification (Unified Endpoint)

**POST** `/api/notifications/payment`

Handles both payment success and failed notifications. The `paymentStatus` field determines the type of notification.

**Request Body for Payment Success:**
```json
{
  "transactionId": "TXN123456789",
  "merchantEmail": "merchant@example.com",
  "merchantName": "ABC Store",
  "amount": 100.50,
  "currency": "USD",
  "paymentMethod": "Credit Card",
  "paymentStatus": "SUCCESS",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "transactionDate": "2024-01-15T10:30:00",
  "orderId": "ORD123456",
  "description": "Product purchase"
}
```

**Request Body for Payment Failed:**
```json
{
  "transactionId": "TXN123456789",
  "merchantEmail": "merchant@example.com",
  "merchantName": "ABC Store",
  "amount": 100.50,
  "currency": "USD",
  "paymentMethod": "Credit Card",
  "paymentStatus": "FAILED",
  "failureReason": "Insufficient funds",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "transactionDate": "2024-01-15T10:30:00",
  "orderId": "ORD123456",
  "description": "Product purchase"
}
```

**Response:**
```
200 OK - Payment notification processed and saved successfully
```

**Error Responses:**
- `400 Bad Request` - Missing required fields or invalid payment status
- `409 Conflict` - Transaction ID already exists
- `500 Internal Server Error` - Server error during processing

## Testing the API

### Using cURL

**Payment Success:**
```bash
curl -X POST http://localhost:8080/api/notifications/payment \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN123456789",
    "merchantEmail": "merchant@example.com",
    "merchantName": "ABC Store",
    "amount": 100.50,
    "currency": "USD",
    "paymentMethod": "Credit Card",
    "paymentStatus": "SUCCESS"
  }'
```

**Payment Failed:**
```bash
curl -X POST http://localhost:8080/api/notifications/payment \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN987654321",
    "merchantEmail": "merchant@example.com",
    "merchantName": "ABC Store",
    "amount": 100.50,
    "currency": "USD",
    "paymentMethod": "Credit Card",
    "paymentStatus": "FAILED",
    "failureReason": "Insufficient funds"
  }'
```

## Project Structure

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── homeware/
│   │   │           └── notificationservice/
│   │   │               ├── NotificationServiceApplication.java
│   │   │               ├── controller/
│   │   │               │   └── NotificationController.java
│   │   │               ├── dto/
│   │   │               │   └── PaymentDetails.java
│   │   │               ├── entity/
│   │   │               │   └── PaymentDetails.java
│   │   │               ├── repository/
│   │   │               │   └── PaymentDetailsRepository.java
│   │   │               └── service/
│   │   │                   └── EmailNotificationService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application.yml
│   │       └── schema.sql
│   └── test/
│       └── java/
│           └── com/
│               └── homeware/
│                   └── notificationservice/
│                       └── NotificationControllerTest.java
├── pom.xml
└── README.md
```

## Required Fields

### PaymentDetails (All Payments)
- `transactionId` (required, unique)
- `merchantEmail` (required)
- `merchantName` (required)
- `amount` (required)
- `currency` (required)
- `paymentMethod` (required)
- `paymentStatus` (required, must be "SUCCESS" or "FAILED")

### Additional Required for Failed Payments
- `failureReason` (required when `paymentStatus` is "FAILED")

### Optional Fields
- `customerEmail`
- `customerName`
- `transactionDate` (defaults to current time if not provided)
- `orderId`
- `description`

## Database Schema

The `payment_details` table includes:
- `id` - Primary key (auto-generated)
- `transaction_id` - Unique transaction identifier
- `merchant_email` - Merchant's email address
- `merchant_name` - Merchant's name
- `amount` - Payment amount
- `currency` - Currency code
- `payment_method` - Payment method used
- `payment_status` - Status (SUCCESS or FAILED)
- `failure_reason` - Reason for failure (if applicable)
- `customer_email` - Customer's email
- `customer_name` - Customer's name
- `transaction_date` - Transaction timestamp
- `order_id` - Associated order ID
- `description` - Transaction description
- `created_at` - Record creation timestamp
- `updated_at` - Last update timestamp

## Error Handling

The service includes error handling for:
- Invalid input data (validation errors)
- Duplicate transaction IDs
- Missing required fields (especially `failureReason` for failed payments)
- Email sending failures
- Database connection issues

All errors are logged and appropriate HTTP status codes are returned.

## License

This project is part of the Homeware payment system.
