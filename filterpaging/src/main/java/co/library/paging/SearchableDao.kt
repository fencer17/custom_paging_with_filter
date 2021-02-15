package co.library.paging

interface SearchableDao<T> {
    suspend fun updateOrInsert(vararg item: T)

    suspend fun delete(vararg item: T)
}