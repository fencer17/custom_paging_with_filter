package co.library.custom_paging_with_filter.models

data class SearchResult(val total_count: Int, val incomplete_results: Boolean, val items: List<User>)
