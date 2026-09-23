package com.project.starter.common.base

import com.project.starter.common.exception.AppException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Base class for all repositories providing a [safeCall] utility
 * that wraps suspend functions in a [Flow<Result<T>>] with error handling.
 *
 * Note: Uses [Dispatchers.Default] instead of [Dispatchers.IO] for KMP
 * compatibility (IO is not available on all platforms).
 */
abstract class BaseRepository {
    /**
     * Executes [block] on [dispatcher] and emits the result.
     * Maps any [AppException] through as-is; wraps other exceptions in [AppException.UnknownException].
     *
     * @param T Raw return type of [block]
     * @param R Transformed return type emitted in the [Result]
     */
    protected fun <T, R> safeCall(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend () -> T,
        transform: (T) -> R,
    ): Flow<Result<R>> =
        flow {
            emit(Result.success(transform(block())))
        }.catch { e ->
            val appException =
                when (e) {
                    is AppException -> e
                    else -> AppException.UnknownException(e.message ?: "An unknown error occurred")
                }
            emit(Result.failure(appException))
        }.flowOn(dispatcher)

    /** Overload without transformation. */
    protected fun <T> safeCall(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend () -> T,
    ): Flow<Result<T>> = safeCall(dispatcher, block) { it }
}
