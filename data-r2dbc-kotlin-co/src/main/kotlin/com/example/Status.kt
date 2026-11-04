package com.example

import io.micronaut.serde.annotation.Serdeable

@Serdeable
enum class Status {
    DRAFT, PUBLISHED, REJECTED, PENDING_MODERATED
}