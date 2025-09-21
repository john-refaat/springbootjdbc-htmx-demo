package dev.js.productsdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SpringBootJdbcHtmxDemoApplication

fun main(args: Array<String>) {
	runApplication<SpringBootJdbcHtmxDemoApplication>(*args)
}
