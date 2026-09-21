package com.project.starter.core.model

import com.project.starter.core.model.entity.ExampleEntity
import com.project.starter.core.model.response.ExampleResponse

object DataMapper {
    fun ExampleResponse.toEntity(): ExampleEntity =
        ExampleEntity(
            id = this.id,
            name = this.name,
            description = this.desc,
        )

    fun ExampleEntity.toDomain(): ExampleModel =
        ExampleModel(
            id = this.id,
            name = this.name,
            description = this.description,
        )
}
