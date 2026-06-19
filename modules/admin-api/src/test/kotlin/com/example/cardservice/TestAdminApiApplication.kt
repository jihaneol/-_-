package com.example.cardservice

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<AdminApiApplication>().with(TestcontainersConfiguration::class).run(*args)
}
