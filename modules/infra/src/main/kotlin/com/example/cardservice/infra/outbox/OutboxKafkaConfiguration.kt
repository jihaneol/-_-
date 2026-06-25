package com.example.cardservice.infra.outbox

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableKafka
@EnableScheduling
@ConditionalOnProperty(prefix = "commerce.outbox.kafka", name = ["enabled"], havingValue = "true")
class OutboxKafkaConfiguration
