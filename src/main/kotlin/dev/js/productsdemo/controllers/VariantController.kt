package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.service.VariantService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/products/{productId}/variants")
class VariantController(
    private val variantService: VariantService
) {

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(VariantController::class.java)
    }

    @GetMapping("/create")
    fun getCreateVariantForm(@PathVariable("productId") uid: Long, model: Model): String {
        logger.info("Loading create variant form")
        model.addAttribute("variantForm", VariantDTO(productId = uid))
        return "fragments/variant-form :: create-update-variant-form"
    }

    @PostMapping("/create")
    fun saveNewVariant(
        @PathVariable("productId") uid: Long,
        @Valid @ModelAttribute("variantForm") variantForm: VariantDTO,
        bindingResult: BindingResult,
        model: Model
    ): String {
        logger.info("Saving new variant: {}", variantForm)
        if (bindingResult.hasErrors()) {
            logger.error("Validation error: {}", bindingResult.allErrors)
            return "fragments/variant-form :: create-update-variant-form"
        }
        variantService.saveVariant(variantForm.copy(productId = uid))
        model.addAttribute("successMessage", "Successfully created variant")
        model.addAttribute("variantForm", VariantDTO(productId = uid))
        return "fragments/variant-form :: create-update-variant-form"
    }

    @GetMapping("{variantId}/update")
    fun getUpdateVariantForm(@PathVariable("productId") productId: Long,
                             @PathVariable("variantId") variantId: Long,
                             model: Model): String {
        logger.info("Loading update variant form for variant with ID: {}", variantId)
        val variant = variantService.findVariantByIdAndProductId(variantId, productId)
        logger.info("Found variant: {}", variant)
        model.addAttribute("variantForm", variant)
        return "fragments/variant-form :: create-update-variant-form"
    }

    @PostMapping("{variantId}/update")
    fun updateVariant(@PathVariable("productId") productId: Long,
                      @PathVariable("variantId") variantId: Long,
                      @Valid @ModelAttribute("variantForm") variantForm: VariantDTO,
                      bindingResult: BindingResult,
                      model: Model): String {
        logger.info("Updating variant: {}", variantForm)
        if (bindingResult.hasErrors()) {
            logger.error("Update Variant form validation error: {}", bindingResult.allErrors)
            return "fragments/variant-form :: create-update-variant-form"
        }
        variantService.updateVariant(variantId, variantForm.copy(productId = productId))
        model.addAttribute("successMessage", "Successfully updated variant")
        model.addAttribute("variantForm", variantForm)
        return "fragments/variant-form :: create-update-variant-form"
    }
}