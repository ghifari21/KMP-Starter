package com.project.starter.feat.home.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DetailRoute(
    val id: String,
    val title: String,
)
