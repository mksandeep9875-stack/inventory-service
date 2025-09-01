# Inventory Service

**Port:** 8092 

---

## Overview
`inventory-service` manages inventory operations for the Ecommerce platform.  
It allows creating inventory entries and checking inventory availability for products.

The service also has a Kafka consumer which listens to the order-events created by order-service and update the stock availability based on order confirmed or failed.

---

## Endpoints

| Endpoint                             | Method | Description                                |
|-------------------------------------|--------|--------------------------------------------|
| /inventory/v1/create                 | POST   | Create a new inventory entry               |
| /inventory/v1/checkInventoryAvailability | POST   | Check availability of a product in inventory |

---
## Configuration

Configuration file: src/main/resources/application*.properties or application.yml.

The application properties will be taken from the profile from https://github.com/mksandeep9875-stack/config-server-properties.git using spring cloud config server

---
## Dependencies

-Spring Boot Starter Web

-Spring Boot Starter Actuator

-Spring Boot Starter MongoDB (depending on your database)

-Spring Cloud Config Client

-Eureka Client

-Spring cloud Webflux


---

## How to Run

```bash
git clone <your-repo-url>
cd customer-service
mvn clean install
mvn spring-boot:run
