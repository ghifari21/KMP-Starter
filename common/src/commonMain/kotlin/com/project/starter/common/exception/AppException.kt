package com.project.starter.common.exception

sealed class AppException(
    override val message: String,
) : Exception(message) {
    class NetworkException(
        message: String,
    ) : AppException(message)

    class ServerException(
        message: String,
    ) : AppException(message)

    class UnknownException(
        message: String,
    ) : AppException(message)
}
