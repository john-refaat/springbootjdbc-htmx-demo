package dev.js.productsdemo.service

import dev.js.productsdemo.model.VariantDTO

interface VariantService {
    fun saveVariants(variants: List<VariantDTO>): List<VariantDTO>
    fun saveVariant(variant: VariantDTO): VariantDTO?
}