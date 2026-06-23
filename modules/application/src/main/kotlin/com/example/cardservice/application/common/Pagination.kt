package com.example.cardservice.application.common

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class Pagination(
    val page: Int,
    val size: Int,
    val sort: String,
) {
    val normalizedPage: Int = page.coerceAtLeast(0)
    val normalizedSize: Int = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
    val sortDirection: SortDirection = SortDirection.from(sort)
}

enum class SortDirection {
    ASC,
    DESC,
    ;

    companion object {
        fun from(sort: String): SortDirection =
            if (sort.substringAfter(",", "desc").equals("asc", ignoreCase = true)) ASC else DESC
    }
}

const val DEFAULT_PAGE_SIZE = 20
const val MAX_PAGE_SIZE = 100
private const val MIN_PAGE_SIZE = 1

fun Pagination.toPageable(sortProperty: String = "id"): Pageable =
    PageRequest.of(
        normalizedPage,
        normalizedSize,
        Sort.by(sortDirection.toSpringDirection(), sortProperty),
    )

private fun SortDirection.toSpringDirection(): Sort.Direction =
    when (this) {
        SortDirection.ASC -> Sort.Direction.ASC
        SortDirection.DESC -> Sort.Direction.DESC
    }
