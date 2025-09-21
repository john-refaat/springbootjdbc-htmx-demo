package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Image
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcImageRepository(private val jdbcClient: JdbcClient) : ImageRepository {
    override fun saveImage(image: Image): Image {

        val generatedId = jdbcClient.sql("""
            INSERT INTO images (external_id, src, created_at)
            VALUES (?, ?, ?)
            RETURNING id
        """.trimIndent()
        )
            .param(image.externalId)
            .param(image.src)
            .param(image.createdAt)
            .query(Long::class.java)
            .single()


        return image.copy(id = generatedId)
    }

    private fun extractTimeStampTZ(key: Any?): OffsetDateTime = when (val v = key) {
        is OffsetDateTime -> v
        is Timestamp -> v.toInstant().atOffset(ZoneOffset.UTC) // TIMESTAMPTZ represents an instant
        is Instant -> v.atOffset(ZoneOffset.UTC)
        null -> error("Not present in generated keys")
        else -> error("Unexpected type: ${v::class.qualifiedName}")
    }


    override fun findImageById(id: Long): Image? {
        val sql = "SELECT * FROM images WHERE id = ?"
        return jdbcClient.sql(sql)
            .param(id)
            .query(Image::class.java)
            .optional()
            .orElse(null)
    }

    override fun findImageByExternalId(externalId: Long): Image? {
        val sql = "SELECT * FROM images WHERE external_id = ?"
        return jdbcClient.sql(sql)
            .param(externalId)
            .query(Image::class.java)
            .optional()
            .orElse(null)
    }

    override fun findImagesByProductId(productId: Long): List<Image> {
        val sql = """
            SELECT i.* FROM images i
            INNER JOIN variants v ON i.id = v.image_id
            WHERE v.product_id = ?
        """.trimIndent()
        return jdbcClient.sql(sql)
            .param(productId)
            .query(Image::class.java)
            .list()
    }

    override fun findImageByVariantId(variantId: Long): Image {
        val sql = """
            SELECT i.* FROM images i
            INNER JOIN variants v ON i.id = v.image_id
            WHERE v.id = ?
        """.trimIndent()
        return jdbcClient.sql(sql)
            .param(variantId)
            .query(Image::class.java)
            .single()
    }

    override fun deleteImage(id: Long) {
        val sql = "DELETE FROM images WHERE id = ?"
        jdbcClient.sql(sql)
            .param(id)
            .update()
    }
}