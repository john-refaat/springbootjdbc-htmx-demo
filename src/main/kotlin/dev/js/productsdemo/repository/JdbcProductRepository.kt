package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.domain.Variant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcProductRepository(private val jdbcClient: JdbcClient) : ProductRepository {
    override fun findAllProducts(limit: Int?, offset: Int?): List<Product> =
        jdbcClient.sql(
            """
            SELECT * from products 
            ORDER BY created_at DESC
            ${if (limit != null) " LIMIT $limit" else ""}
            ${if (offset != null) " OFFSET $offset" else ""}
        """.trimIndent()
        )
            .query { rs, _ ->
                mapRowToProduct(rs)
            }
            .list()


    override fun findAllProductsWithDetails(limit: Int?, offset: Int?): List<Product> {
        return jdbcClient.sql(
            """
            WITH paginated_products AS (
                SELECT * FROM products
                ORDER BY created_at DESC
                ${if (limit != null) " LIMIT $limit" else ""}
                ${if (offset != null) " OFFSET $offset" else ""}
            )
            SELECT 
                p.id AS p_id, p.external_id AS p_external_id, p.title AS p_title, 
                p.vendor AS p_vendor, p.product_type AS p_product_type, p.created_at AS p_created_at,
                v.id AS v_id, v.external_id AS v_external_id, v.title AS v_title,
                v.option1 AS v_option1, v.option2 AS v_option2, v.option3 AS v_option3,
                v.sku AS v_sku, v.price AS v_price, v.available AS v_available, v.created_at AS v_created_at,
                i.id AS i_id, i.external_id AS i_external_id, i.src AS i_src, 
                i.created_at AS i_created_at
            FROM paginated_products p
            LEFT JOIN variants v ON p.id = v.product_id
            LEFT JOIN images i ON v.image_id = i.id
            ORDER BY p.created_at DESC
        """.trimIndent()
        )
            .query { rs, _ ->
                mapRowToProductWithDetails(rs)
            }
            .list()
            .groupBy { (prod, _) -> prod }
            .mapValues { (_, pairs) -> pairs.map { (_, variant) -> variant } }
            .map { (prod, variants) -> prod.copy(variants = variants.filterNotNull()) }
    }

    override fun countAllProducts(): Long =
        jdbcClient.sql("SELECT COUNT(*) FROM products")
            .query(Long::class.java)
            .single()

    override fun findProductById(id: Long): Product? =
        jdbcClient.sql("SELECT * from products WHERE id = :id")
            .param("id", id)
            .query { rs, _ ->
                mapRowToProduct(rs)
            }.optional().orElse(null)


    override fun findProductByExternalId(externalId: Long): Product? {
        return jdbcClient.sql("SELECT * from products WHERE external_id = :externalId")
            .param("externalId", externalId)
            .query { rs, _ ->
                mapRowToProduct(rs)
            }
            .optional().orElse(null)
    }

    override fun findProductWithDetailsById(id: Long): Product {
        return jdbcClient.sql(
            """
                SELECT 
                    p.id as p_id, p.external_id as p_external_id, p.title as p_title, 
                    p.vendor as p_vendor, p.product_type as p_product_type, p.created_at as p_created_at,
                    v.id as v_id, v.external_id as v_external_id, v.title as v_title, 
                    v.option1 AS v_option1, v.option2 AS v_option2, v.option3 AS v_option3,
                    v.sku AS v_sku, v.price as v_price, v.available as v_available, v.created_at as v_created_at,
                    i.id as i_id, i.external_id as i_external_id, i.src as i_src, 
                    i.created_at as i_created_at
                FROM products p
                LEFT JOIN variants v ON p.id = v.product_id
                LEFT JOIN images i ON v.image_id = i.id
                WHERE p.id = :id
                """.trimIndent()

        )
            .param("id", id)
            .query { rs, _ ->
                mapRowToProductWithDetails(rs)
            }.list()
            .groupBy { (prod, _) -> prod }
            .mapValues { (_, pairs) -> pairs.map { (_, variant) -> variant } }
            .map { (prod, variants) -> prod.copy(variants = variants.filterNotNull()) }[0]
    }


    override fun saveProduct(product: Product): Product {
        val sql = """
            INSERT INTO products (external_id, title, vendor, product_type, created_at) 
            VALUES (:externalId, :title, :vendor, :productType, :createdAt)
            RETURNING id
        """.trimIndent()

        val generatedId: Long = jdbcClient.sql(sql)
            .param("externalId", product.externalId)
            .param("title", product.title)
            .param("vendor", product.vendor)
            .param("productType", product.productType)
            .param("createdAt", product.createdAt)
            .query(Long::class.java)
            .single()

        return product.copy(id = generatedId)
    }


    override fun updateProduct(product: Product): Product? {
        val updated = jdbcClient.sql(
            """
            UPDATE products 
            SET title = ?, vendor = ?, product_type = ?
            WHERE id = ? and external_id = ?
        """.trimIndent()
        )
            .param(product.title)
            .param(product.vendor)
            .param(product.productType)
            .param(product.id)
            .param(product.externalId)
            .update()

        return if (updated == 1) product else null
    }

    override fun saveOrUpdateProduct(product: Product): Product? {
        return if (product.id == 0L || findProductByExternalId(product.externalId) == null)
            saveProduct(product)
        else
            updateProduct(product)
    }

    override fun deleteProduct(id: Long): Boolean {
        return jdbcClient.sql("DELETE FROM products WHERE id = :id")
            .param(id)
            .update() == 1
    }

    override fun deleteAllProducts() {
        jdbcClient.sql("DELETE FROM products")
            .update()
    }

    private fun mapRowToProduct(rs: ResultSet): Product {
        return Product(
            id = rs.getLong("id"),
            externalId = rs.getLong("external_id"),
            title = rs.getString("title"),
            vendor = rs.getString("vendor"),
            productType = rs.getString("product_type"),
            createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("created_at"))
        )
    }

    private fun mapRowToProductWithDetails(rs: ResultSet): Pair<Product, Variant?> {
        val product = Product(
            id = rs.getLong("p_id"),
            externalId = rs.getLong("p_external_id"),
            title = rs.getString("p_title"),
            vendor = rs.getString("p_vendor"),
            productType = rs.getString("p_product_type"),
            createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("p_created_at"))
        )


        val variant = rs.getObject("v_id")?.run {
            Variant(
                id = rs.getLong("v_id"),
                externalId = rs.getLong("v_external_id"),
                productId = rs.getLong("p_id"),
                title = rs.getString("v_title"),
                option1 = rs.getString("v_option1"),
                option2 = rs.getString("v_option2"),
                option3 = rs.getString("v_option3"),
                sku = rs.getString("v_sku"),
                price = rs.getBigDecimal("v_price"),
                available = rs.getBoolean("v_available"),
                createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("v_created_at")),
                featuredImage = if (rs.getLong("i_id") != 0L) Image(
                    id = rs.getLong("i_id"),
                    externalId = rs.getLong("i_external_id"),
                    src = rs.getString("i_src"),
                    createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("i_created_at"))
                ) else null
            )
        }

        return Pair(product, variant)
    }
}