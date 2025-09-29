package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductRequest
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant

@Controller
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping("")
    fun loadProducts(
        @RequestParam(required = false) currentPage: Int?,
        @RequestParam(required = false) pageSize: Int?,
        model: Model
    ): String {
        logger.info("Loading products with page: {}, size: {}", currentPage, pageSize)
        val productsWithDetails = productService.getAllProductsWithDetails(currentPage?:0, pageSize)
        model.addAttribute("productsWithDetails", productsWithDetails)
        model.addAttribute("newProduct", ProductRequest(
            product = ProductDTO()
        ))
        return "fragments/product-table :: product-table"
    }

    @PostMapping("/variant-row")
    fun getVariantRow(@ModelAttribute newProduct: ProductRequest, model: Model): String {
        logger.info("Adding New Variant Row: {}", newProduct)
        newProduct.product.variants.add(VariantDTO(
            uid = null,
            sku = "",
            price = null,
            option1 = null,
            option2 = null,
            option3 = null,
            available = true,
            externalId = null,
            productId = null,
            title = "",
            featuredImage = null,
            createdAt = null
        ))
        model.addAttribute("variantIndex", newProduct.product.variants.size - 1)
        model.addAttribute("newProduct", newProduct)
        return "fragments/add-variant :: variant-row"
    }

//
//    @RequestParam title: String,
//    @RequestParam(required = false) vendor: String?,
//    @RequestParam(required = false) productType: String?,
//    @RequestParam(name = "pageSize", required = false) pageSize: Int?,

    @PostMapping("")
    fun addProduct(
        @ModelAttribute request: ProductRequest,
        model: Model
    ): String {
        logger.info("Adding product: {}", request)

        val savedProduct = productService.saveProduct(productDTO = request.product)
        logger.info("Saved product: {}", savedProduct)
        // Return updated table
        val productsWithDetails = productService.getAllProductsWithDetails(0, request.pageSize)

        model.addAttribute("productsWithDetails", productsWithDetails)
        return "fragments/product-table"
    }

}

@Controller
class IndexController {

    @GetMapping("", "/")
    fun index(model: Model): String {
        model.addAttribute("newProduct", ProductRequest(
            product = ProductDTO(
                title = "",
                vendor = "",
                productType = "",
                variants = mutableListOf(
                    VariantDTO(
                        uid = null,
                        sku = "",
                        price = null,
                        option1 = null,
                        option2 = null,
                        option3 = null,
                        available = true,
                        externalId = Instant.now().toEpochMilli(),
                        productId = null,
                        title = "",
                        featuredImage = null,
                        createdAt = null
                    )
                )
            ),
            pageSize = 10
        ))
        return "index"
    }

}