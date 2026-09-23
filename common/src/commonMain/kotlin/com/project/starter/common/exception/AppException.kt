package com.project.starter.common.exception

/**
 * Sealed exception hierarchy for the application.
 * All domain-level errors should be one of these subtypes.
 */
sealed class AppException(
    override val message: String,
) : Exception(message) {
    /** Network-level failure: timeout, no connectivity, DNS error. */
    class NetworkException(
        message: String,
    ) : AppException(message)

    /** Server-side error (HTTP 5xx). */
    class ServerException(
        message: String,
    ) : AppException(message)

    /** API-level error (HTTP 4xx, except 401). */
    class ApiException(
        val code: Int,
        message: String,
    ) : AppException(message)

    /** Authentication failed — token expired or invalid (HTTP 401). */
    class UnauthorizedException(
        message: String = "Unauthorized. Please sign in again.",
    ) : AppException(message)

    /** Local storage read/write failure (DataStore, database). */
    class LocalStorageException(
        message: String,
    ) : AppException(message)

    /** Catch-all for any other unexpected error. */
    class UnknownException(
        message: String,
    ) : AppException(message)
}
