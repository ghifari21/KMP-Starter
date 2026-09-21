package com.project.starter.home

import com.project.starter.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
