package com.example.imageservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class ImageServiceApplication

fun main(args: Array<String>) {
    runApplication<ImageServiceApplication>(*args)
}
