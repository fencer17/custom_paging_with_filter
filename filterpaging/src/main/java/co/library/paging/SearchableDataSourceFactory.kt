package co.library.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import java.sql.SQLException
import kotlin.collections.HashMap

/**
 * Base abstract class for searchable DataSource. Create your own source factory class and extend it from
 * SearchableDataSourceFactory to use searchable paging library
 *
 * @param dao - dao, which implement SearchableDao
 */
abstract class SearchableDataSourceFactory<DB, API>(private val dao: SearchableDao<DB>) : DataSource.Factory<Int, DB>() {

    private val mutableLiveData: MutableLiveData<DataSource<Int, DB>> = MutableLiveData()
    private var params: HashMap<String, List<Any>> = hashMapOf()

    /**
     * Create new filtered DataSource and notify LiveData about it
     *
     * @return the new DataSource
     */
    override fun create(): DataSource<Int, DB> {
        return getDataSource(dao, params).create().apply {
            getData().postValue(this)
        }
    }

    /**
     * Get live data of DataSource
     * override to use your own
     *
     * @return current MutableLiveData of DataSource
     */
    open fun getData(): MutableLiveData<DataSource<Int, DB>> {
        return mutableLiveData
    }

    /**
     * invalidate DataSource
     */
    fun invalidateDataSource() {
        getData().value?.invalidate()
    }

    /**
     * Get values of searching parameter
     * @param param - searching parameter
     *
     * @return values of searching parameter
     */
    fun getQuery(param: String): List<Any> {
        return params[param] ?: listOf()
    }

    /**
     * Get map of searching parameters and its values
     *
     * @return map of searching parameters and its values
     */
    fun getQueries(): Map<String, List<Any>> {
        return params
    }

    /**
     * Set searching parameter and it values
     * @param param - searching parameter
     * @param values - values of searching parameter
     */
    fun setQuery(param: String, values: List<Any>) {
        if (values.isEmpty()) {
            params.remove(param)
        } else {
            params[param] = values
        }
    }

    /**
     * Set map of searching parameters and its values
     * @param params - map of searching parameters and its values
     */
    fun setQueries(params: HashMap<String, List<Any>>) {
        this.params = params
    }

    /**
     * Clear all parameters
     */
    fun clearQueries() {
        params.clear()
    }

    /**
     * Method for build database queries, apply it into dao and get back filtered DataSource
     * @param dao - dao for applying database queries
     * @param params - map of searching parameters and its values
     *
     * @return filtered DataSource
     */
    protected abstract fun getDataSource(dao: SearchableDao<DB>, params: HashMap<String, List<Any>>): DataSource.Factory<Int, DB>

    /**
     * Do whatever you want with the data from server. For instance, insert result into database
     * @param result - list of items from server
     * @param force - flag to detect that current loading is forcible
     *
     * @return boolean value for success or failure result
     */
    abstract suspend fun onDataLoaded(result: List<API>, force: Boolean)

    /**
     * Do your stuff with items which was inserted on backend. For instance, insert these items into database
     * @param success - result of insert items request
     * @param insertedItems - list of inserted items
     */
    @Throws(SQLException::class)
    open suspend fun onItemsInserted(success: Boolean, insertedItems: Collection<API>) { }

    /**
     * Do your stuff with items which was deleted on backend. For instance, remove these items from database
     * @param success - result of delete items request
     * @param deletedItems - list of deleted items
     */
    @Throws(SQLException::class)
    open suspend fun onItemsDeleted(success: Boolean, deletedItems: Collection<API>) { }
}