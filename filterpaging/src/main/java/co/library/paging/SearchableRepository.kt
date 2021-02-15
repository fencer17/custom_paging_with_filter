package co.library.paging

interface SearchableRepository<DB> : BaseRepository<DB> {

    fun getQuery(param: String): List<Any>

    fun getQueries(): Map<String, List<Any>>

    fun setQuery(force: Boolean, param: String, values: List<Any>)

    fun setQueries(force: Boolean, params: HashMap<String, List<Any>>)

    fun clearQueries(force: Boolean = false)
}