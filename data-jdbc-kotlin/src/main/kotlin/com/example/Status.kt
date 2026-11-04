package com.example

import io.micronaut.serde.annotation.Serdeable

@Serdeable
enum class Status {
    DRAFT, PENDING_MODERATED, PUBLISHED, REJECTED
}