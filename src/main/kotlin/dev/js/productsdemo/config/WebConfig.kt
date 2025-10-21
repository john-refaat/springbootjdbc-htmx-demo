package dev.js.productsdemo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // This will return 404 directly without logging an error for .well-known paths
        registry.addResourceHandler("/.well-known/**")
            .addResourceLocations("classpath:/META-INF/resources/empty/")
    }
}