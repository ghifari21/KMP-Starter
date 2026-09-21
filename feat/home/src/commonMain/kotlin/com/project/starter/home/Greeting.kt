package com.project.starter.home

import com.project.starter.getPlatform
import com.project.starter.home.sayHello

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}
