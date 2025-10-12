# ActOn Framework

> Declarative architecture for Java — built on **Contracts**, **Actors**, and **Stores**.

## Overview

ActOn is a new foundation for building backend systems with minimal boilerplate.  
You define *what* happens through contracts and *how* through actors — stores handle persistence.

### Core Concepts

| Concept | Description |
|----------|--------------|
| **Contract** | Defines the operation's intent (input and output). |
| **Actor** | Executes business logic for a contract. |
| **Store** | Abstracts persistence and eliminates repositories. |

### Example

```java
@Contract("orders.create")
public record OrderCreate(String customerId, BigDecimal total) {}

public class OrderActor {
    private final StoreState<Order> orders;
    public Order on(OrderCreate c) {
        var order = new Order(c.customerId(), "NEW", c.total());
        return orders.save(order);
    }
}
