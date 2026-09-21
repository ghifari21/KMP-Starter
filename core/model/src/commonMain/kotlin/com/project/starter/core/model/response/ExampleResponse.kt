package com.project.starter.core.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ExampleResponse(
    val id: String,
    val name: String,
    val desc: String
)
