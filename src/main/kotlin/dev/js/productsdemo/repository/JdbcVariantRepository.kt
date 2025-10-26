package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Variant
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcVariantRepository(private val jdbcClient: JdbcClient) : VariantRepository {

    override fun findVariantsByProductId(productId: Long): List<Variant> {
        val sql = """
            SELECT v.id, v.external_id, v.product_id, v.title, v.option1, v.option2, v.option3, v.sku, v.price, v.available, v.created_at,
                   i.id as image_id, i.external_id as image_external_id, i.src, i.created_at as image_created_at
            FROM variants v
            LEFT JOIN images i ON v.image_id = i.id
            WHERE v.product_id = :productId
        """

        return jdbcClient.sql(sql)
            .param("productId", productId)
            .query { rs, _ ->
                mapResultSetToVariant(rs)
            }
            .list()
    }

    override fun findVariantById(id: Long): Variant? {
        val sql = """
            SELECT v.id, v.external_id, v.product_id, v.title, v.option1, v.option2, v.option3, v.sku, v.price, v.available, v.created_at,
                   i.id as image_id, i.external_id as image_external_id, i.src, i.created_at as image_created_at
            FROM variants v
            LEFT JOIN images i ON v.image_id = i.id
            WHERE v.id = :id
        """

        return jdbcClient.sql(sql)
            .param("id", id)
            .query { rs, _ ->
                mapResultSetToVariant(rs)
            }
            .optional()
            .orElse(null)
    }

    override fun findVariantByIdAndProductId(
        id: Long,
        productId: Long
    ): Variant? {
        val sql = """
            SELECT v.id, v.external_id, v.product_id, v.title, v.option1, v.option2, v.option3, v.sku, v.price, v.available, v.created_at,
                   i.id as image_id, i.external_id as image_external_id, i.src, i.created_at as image_created_at
            FROM variants v
            LEFT JOIN images i ON v.image_id = i.id
            WHERE v.id = :id
            AND v.product_id = :productId
        """.trimIndent()

        return jdbcClient.sql(sql)
            .param("id", id)
            .param("productId", productId)
            .query { rs, _ ->
                mapResultSetToVariant(rs)
            }
            .optional()
            .orElse(null)
    }


    override fun externalIdExists(externalId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM variants WHERE external_id = :externalId"
        return jdbcClient.sql(sql)
            .param("externalId", externalId)
            .query(Int::class.java)
            .single() > 0
    }

    override fun saveOrUpdateVariant(variant: Variant): Variant? {
        return if (variant.id !=null) {
            updateVariant(variant)
        } else {
            saveVariant(variant)
        }
    }

    override fun findVariantByExternalId(externalId: Long): Variant? {
        val selectSql = """
            SELECT v.id, v.external_id, v.product_id, v.title, v.option1, v.option2, v.option3, v.sku, v.price, v.available, v.created_at,
                   i.id as image_id, i.external_id as image_external_id, i.src, i.created_at as image_created_at
            FROM variants v
            LEFT JOIN images i ON v.image_id = i.id
            WHERE v.external_id = :externalId
            """
        return jdbcClient.sql(selectSql)
            .param("externalId", externalId)
            .query { rs, _ ->
                mapResultSetToVariant(rs)
            }
            .optional().orElse(null)
    }

    private fun mapResultSetToVariant(rs: ResultSet): Variant = Variant(
        id = rs.getLong("id"),
        externalId = rs.getLong("external_id"),
        productId = rs.getLong("product_id"),
        title = rs.getString("title"),
        option1 = rs.getString("option1"),
        option2 = rs.getString("option2"),
        option3 = rs.getString("option3"),
        sku = rs.getString("sku"),
        price = rs.getBigDecimal("price"),
        available = rs.getBoolean("available"),
        createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("created_at")),
        featuredImage = if (rs.getLong("image_id") != 0L) Image(
            id = rs.getLong("image_id"),
            externalId = rs.getLong("image_external_id"),
            src = rs.getString("src"),
            createdAt = RepositoryUtil.extractTimeStampTZ(rs.getTimestamp("image_created_at"))
        ) else null
    )

    private fun saveVariant(variant: Variant): Variant {
        val insertSql = """
            INSERT INTO variants (external_id, product_id, image_id, title, option1, option2, option3, sku, price, available, created_at)
            VALUES (:externalId, :productId, :imageId, :title, :option1, :option2, :option3, :sku, :price, :available, :createdAt)
            RETURNING id
        """.trimIndent()

        val id: Long = jdbcClient.sql(insertSql)
            .param("externalId", variant.externalId)
            .param("productId", variant.productId)
            .param("imageId", variant.featuredImage?.id)
            .param("title", variant.title)
            .param("option1", variant.option1)
            .param("option2", variant.option2)
            .param("option3", variant.option3)
            .param("sku", variant.sku)
            .param("price", variant.price)
            .param("available", variant.available)
            .param("createdAt", variant.createdAt)
            .query(Long::class.java)
            .single()

        return variant.copy(id = id)
    }


    private fun updateVariant(variant: Variant): Variant? {
        val updateSql = """
            UPDATE variants 
            SET product_id = :productId,
                image_id = :imageId,
                title = :title,
                option1 = :option1,
                option2 = :option2,
                option3 = :option3,
                sku = :sku,
                price = :price,
                available = :available,
                external_id = :externalId
            WHERE id = :id
        """.trimIndent()
        val updated = jdbcClient.sql(updateSql)
            .param("productId", variant.productId)
            .param("imageId", variant.featuredImage?.id)
            .param("title", variant.title)
            .param("option1", variant.option1)
            .param("option2", variant.option2)
            .param("option3", variant.option3)
            .param("sku", variant.sku)
            .param("price", variant.price)
            .param("available", variant.available)
            .param("externalId", variant.externalId)
            .param("id", variant.id)
            .update()

        return if (updated == 1) variant else null
    }


    override fun deleteVariant(id: Long) {
        val sql = "DELETE FROM variants WHERE id = :id"
        jdbcClient.sql(sql)
            .param("id", id)
            .update()
    }
}