package com.menon;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Inventory findByProductId(String productId);
}
