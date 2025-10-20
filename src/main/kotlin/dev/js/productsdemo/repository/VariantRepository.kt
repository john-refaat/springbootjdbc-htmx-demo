package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Variant

interface VariantRepository {

    fun findVariantsByProductId(productId: Long): List<Variant>
    fun findVariantById(id: Long): Variant?
    fun externalIdExists(externalId: Long): Boolean
    fun findVariantByExternalId(externalId: Long): Variant?
    fun saveOrUpdateVariant(variant: Variant): Variant?
    fun deleteVariant(id: Long)
}