package com.project.starter.core.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ExampleRequest(
    val query: String,
)
