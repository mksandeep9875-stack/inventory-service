package com.menon;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventories")
@Data
public class Inventory {

    @Id
    private String id;
    private String productId;
    private String totalStock; // Total stock available
    private String totalStockSold; // Stock sold
    private String totalStockReserved; // Stock reserved for pending orders
    private String vendorPhone; // Link to the vendor selling the product
    private String remarks; // Any additional remarks
    private LocalDateTime updatedAt; // Timestamp for last update

}
