package com.menon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("inventory/v1")
public class MainRestController {
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    CustomerService customerService;
    @Autowired
    InventoryRepository inventoryRepository;

    @PostMapping("create")
    public ResponseEntity<?> publishProjectMessages(@RequestHeader("Authorization") String token,
                                                    @RequestBody Inventory inventory) {

        Principal principal = customerService.validateToken(token);
        if (principal.getState().equalsIgnoreCase("valid")) {
            logger.info("Token validated successfully");
            // Token is valid, proceed with the update

            if (inventory.getVendorPhone().equals(principal.getUsername())) // AUTHORIZATION OF REQUEST HAPPENS HERE
            {
                logger.info("Request received to create an inventory for product id: " + inventory.getProductId() + " from vendor: " + inventory.getVendorPhone());
                inventory.setTotalStockSold("0");
                inventory.setTotalStockReserved("0");
                inventory.setId("INVENTORY-" + new Random().nextInt(1000000));
                inventory.setVendorPhone(principal.getUsername());
                inventory.setUpdatedAt(LocalDateTime.now());
                inventoryRepository.save(inventory);
                logger.info("Inventory created successfully with inventory ID: " + inventory.getId() + " for product ID: " + inventory.getProductId());
                return ResponseEntity.ok(inventory);
            } else {
                logger.info("Phone number does not match with the token");
                return ResponseEntity.status(401).body("Unauthorized: Phone number does not match with the token");
            }
        } else {
            logger.info("Token not valid");
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }
    }

    @PostMapping("checkInventoryAvailability")
    public ResponseEntity<?> checkInventoryAvailability(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Integer> productQuantities) {

        if (!customerService.validateToken(token).getState().equalsIgnoreCase("valid")) {
            logger.info("Token not valid");
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }

        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            int requestedQty = entry.getValue();

            Inventory inventory = inventoryRepository.findByProductId(productId);
            if (inventory == null) {
                logger.info("Inventory not found for product ID: " + productId);
                return ResponseEntity.ok(false);
            }

            int totalStock = Integer.parseInt(inventory.getTotalStock());
            if (totalStock < requestedQty) {
                logger.info("Insufficient stock for product ID: " + productId);
                return ResponseEntity.ok(false);
            }
        }

        logger.info("All products have sufficient stock");
        return ResponseEntity.ok(true);
    }
}
