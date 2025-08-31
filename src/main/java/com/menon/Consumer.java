package com.menon;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Consumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    @Autowired
    InventoryRepository inventoryRepository;

    @KafkaListener(topics = "order-events", groupId = "1")
    @Transactional // Ensure atomicity of operations
    public void consumeOrderEvent(String message) {
        try {
            OrderDatum orderEvent = objectMapper.readValue(message, OrderDatum.class);
            log.info("Received Order Event: " + orderEvent);

            switch (orderEvent.getStatus()) {
                case "PROCESSING":
                    updateReserveStock(orderEvent);
                    break;
                case "CONFIRMED":
                    updateStock(orderEvent);
                    break;
                case "CANCELLED":
                    restoreStock(orderEvent);
                    break;
                default:
                    log.info("Ignoring status: " + orderEvent.getStatus());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateReserveStock(OrderDatum orderEvent) {
        orderEvent.getProductsItems().forEach((productId, qty) -> {
            Inventory inv = inventoryRepository.findByProductId(productId);
            if (inv != null) {
                int reserved = Integer.parseInt(StringUtils.isEmpty(inv.getTotalStockReserved()) ? "0" : inv.getTotalStockReserved());
                inv.setTotalStockReserved(String.valueOf(reserved + qty));
                inventoryRepository.save(inv);

                log.info("Reserved stock of %s by %d → new reserved: %s%n",
                        productId, qty, inv.getTotalStockReserved());
            }else{
                log.warn("Inventory not found for productId: " + productId);
            }
        });
    }

    private void updateStock(OrderDatum orderEvent) {
        orderEvent.getProductsItems().forEach((productId, qty) -> {
            Inventory inv = inventoryRepository.findByProductId(productId);
            if (inv != null) {
                int soldStock = Integer.parseInt(StringUtils.isEmpty(inv.getTotalStockSold()) ? "0" : inv.getTotalStockSold());
                int reservedStock = Integer.parseInt(StringUtils.isEmpty(inv.getTotalStockReserved()) ? "0" : inv.getTotalStockReserved());
                inv.setTotalStockSold(String.valueOf(soldStock + qty));
                inv.setTotalStockReserved(String.valueOf(qty - reservedStock));
                inventoryRepository.save(inv);

                log.info("Reduced stock of %s by %d → new sold: %s%n",
                        productId, qty, inv.getTotalStockSold());
            }else{
                log.warn("Inventory not found for productId: " + productId);
            }
        });
    }

    private void restoreStock(OrderDatum orderEvent) {
        orderEvent.getProductsItems().forEach((productId, qty) -> {
            Inventory inv = inventoryRepository.findByProductId(productId);
            if (inv != null) {
                int reservedStock = Integer.parseInt(StringUtils.isEmpty(inv.getTotalStockReserved()) ? "0" : inv.getTotalStockReserved());
                inv.setTotalStockReserved(String.valueOf(qty - reservedStock));
                inventoryRepository.save(inv);

                log.info("Restored stock of %s by %d → new sold: %s%n",
                        productId, qty, inv.getTotalStockSold());
            }else{
                log.warn("Inventory not found for productId: " + productId);
            }
        });
    }
}


