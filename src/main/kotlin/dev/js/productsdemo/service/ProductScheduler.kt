package dev.js.productsdemo.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProductScheduler(private val productService: ProductService) {

    private val logger = LoggerFactory.getLogger(ProductScheduler::class.java)

    @Scheduled(initialDelay = 0, fixedDelay = 3600000) // Run immediately, then every hour
    fun fetchProducts() {
        logger.info("Running scheduled product fetch...")
        productService.fetchAndSaveExternalProducts()
    }
}