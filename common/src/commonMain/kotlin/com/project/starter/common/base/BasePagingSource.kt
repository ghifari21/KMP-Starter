package com.project.starter.common.base

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Base class for paginated data sources using Paging 3.
 *
 * Compatible with all KMP targets via [androidx.paging:paging-common:3.3.x].
 * Override [fetchData] to provide page-specific data.
 *
 * @param T The type of paginated items.
 */
abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {
    /** The index of the first page. Defaults to 1. */
    protected open val initialPageIndex: Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val position = params.key ?: initialPageIndex
        return try {
            val data = fetchData(position, params.loadSize)
            LoadResult.Page(
                data = data,
                prevKey = if (position == initialPageIndex) null else position - 1,
                nextKey = if (data.isEmpty()) null else position + 1,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    /**
     * Fetch items for a given [page] and [size].
     * Called by [load] — suspend-safe.
     */
    abstract suspend fun fetchData(
        page: Int,
        size: Int,
    ): List<T>

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
