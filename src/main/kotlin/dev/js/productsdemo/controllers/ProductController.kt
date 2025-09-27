package dev.js.productsdemo.controllers

import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.service.ProductService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {

    @GetMapping("")
    fun loadProducts(
        @RequestParam(required = false) currentPage: Int?,
        @RequestParam(required = false) pageSize: Int?,
        model: Model
    ): String {
        println("Loading products...")
        val productsWithDetails = productService.getAllProductsWithDetails(currentPage?:0, pageSize)
        model.addAttribute("productsWithDetails", productsWithDetails)
        return "fragments/product-table :: product-table"
    }

    @PostMapping("")
    fun addProduct(
        @RequestParam title: String,
        @RequestParam(required = false) vendor: String?,
        @RequestParam(required = false) productType: String?,
        model: Model
    ): String {
        val product = Product(
            externalId = System.currentTimeMillis(), // Use timestamp as external ID for manually added products
            title = title,
            vendor = vendor?.takeIf { it.isNotBlank() },
            productType = productType?.takeIf { it.isNotBlank() }
        )

        productService.saveProduct(product)

        // Return updated table
        val productsWithDetails = productService.getAllProductsWithDetails(0, null)
        model.addAttribute("productsWithDetails", productsWithDetails)
        return "fragments/product-table"
    }

}

@Controller
@RequestMapping("/")
class IndexController {

    fun index() = "index"
}