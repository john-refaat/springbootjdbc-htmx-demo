package dev.js.productsdemo.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File

@Controller
@RequestMapping("/images")
class ImageController(
    @Value("\${app.upload.dir}") private val uploadDir: String,
) {
    @GetMapping("{productId}/{variantId}")
    @ResponseBody
    fun serveFile(@PathVariable productId: String, @PathVariable variantId: String): ByteArray {
        val productDir = File("$uploadDir/$productId")
        val variantFile = productDir.listFiles { file ->
            file.name.startsWith(variantId)
        }?.firstOrNull() ?: throw RuntimeException("Image not found")

        return variantFile.readBytes()
    }
}