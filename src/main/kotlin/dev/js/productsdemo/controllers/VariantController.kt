package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.service.VariantService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/products/{uid}/variants")
class VariantController(
    private val variantService: VariantService
) {

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(VariantController::class.java)
    }

    @GetMapping("/create")
    fun getCreateVariantForm(@PathVariable("uid") uid:Long, model: Model): String {
        logger.info("Loading create variant form")
        model.addAttribute("newVariant", VariantDTO(productId = uid))
        return "fragments/create-variant-form :: create-variant-form"
    }

    @PostMapping("/create")
    fun saveNewVariant(@PathVariable("uid") uid:Long,
                       @Valid @ModelAttribute("newVariant") newVariant: VariantDTO,
                       bindingResult: BindingResult,
                       model: Model): String {
        logger.info("Saving new variant: {}", newVariant)
        if (bindingResult.hasErrors()) {
            logger.error("Validation error: {}", bindingResult.allErrors)
            return "fragments/create-variant-form :: create-variant-form"
        }
        variantService.saveVariant(newVariant.copy(productId = uid))
        model.addAttribute("successMessage", "Successfully created variant")
        model.addAttribute("newVariant", VariantDTO(productId = uid))
        return "fragments/create-variant-form :: create-variant-form"
    }

}