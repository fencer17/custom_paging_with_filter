package co.library.paging

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import java.sql.SQLException
import kotlin.coroutines.CoroutineContext

/**
 * Base abstract class for searchable paging. Extends from it to use searchable paging library
 *
 * @param DB - Database model class
 * @param API - Response from server
 *
 * @param dataSource - dataSource factory which should extends from SearchableDataSourceFactory
 * @param parentJob - job for coroutine
 * @param PAGE_SIZE - declare it to use custom size per page, otherwise default value
 * @param DISTANCE - declare it to use custom preload distance, otherwise default value
 * @param INITIAL_PAGE - declare it to use custom first page, otherwise default value
 * @param PAGE - declare it to use custom current page, otherwise default value
 */
abstract class BaseRefreshableRepository<DB, API>(
    protected var dataSource: SearchableDataSourceFactory<DB, API>,
    protected val parentJob: Job? = null,
    protected open val PAGE_SIZE: Int = DEFAULT_PAGE_SIZE,
    protected open val DISTANCE: Int = DEFAULT_DISTANCE,
    protected open val INITIAL_PAGE: Int = DEFAULT_INITIAL_PAGE,
    protected open var PAGE: Int = DEFAULT_INITIAL_PAGE
): SearchableRepository<DB>, CoroutineScope {

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 20
        const val DEFAULT_DISTANCE: Int = 5
        const val DEFAULT_INITIAL_PAGE: Int = 0

        const val INVALID_QUERY_KEY_MESSAGE_ADVICE = "To use this key you should handle it in validateQueryKey() method"
    }

    override val coroutineContext: CoroutineContext by lazy { Dispatchers.IO + SupervisorJob(parentJob) }

    private var loadListener: WeakReference<OnLoadListener>? = null

    private val callback: PagedList.BoundaryCallback<DB> = object : PagedList.BoundaryCallback<DB>() {
        private var isFirstLoad = true

        override fun onZeroItemsLoaded() {
            if (isFirstLoad) {
                isFirstLoad = false
                launch { loadItems() }
            }
        }

        override fun onItemAtFrontLoaded(itemAtFront: DB) {
            if (isFirstLoad) {
                launch { loadItems(true) }
                isFirstLoad = false
            }
        }

        override fun onItemAtEndLoaded(itemAtEnd: DB) {
            if(PAGE > 0) {
                launch { loadItems() }
            }
        }
    }

    /**
     * Default builder for PagedList.Config
     * override pagedConfig if you want to customize PagedList.Config
     */
    open val pagedConfig: PagedList.Config by lazy {
        PagedList.Config.Builder()
                .setPageSize(PAGE_SIZE)
                .setPrefetchDistance(DISTANCE)
                .setEnablePlaceholders(true)
                .build()
    }

    private val pagedItems: LiveData<PagedList<DB>> by lazy {
        LivePagedListBuilder(dataSource , pagedConfig)
                .setBoundaryCallback(callback)
                .setInitialLoadKey(PAGE)
                .build()
    }

    /**
     * Use getItems() to observe data and submit it to paged list adapter
     *
     * @return LiveData with paged list
     */
    override fun getItems(): LiveData<PagedList<DB>> = pagedItems

    /**
     * Use refresh to reload data from api
     * @param force - set true if you want to reload data anyway
     */
    override fun refresh(force: Boolean) {
        launch { loadItems(force) }
    }

    /**
     * Use it to declare listener for data load
     */
    override fun setOnLoadListener(listener: OnLoadListener) {
        loadListener?.apply { clear() }
        loadListener = WeakReference(listener)
    }

    private suspend fun loadItems(force: Boolean = false) {

        if (force) {
            PAGE = INITIAL_PAGE
        }

        loadListener?.get()?.invoke(false)

        try {
            val result = loadData(PAGE, PAGE_SIZE, dataSource.getQueries())
            if (!result.isNullOrEmpty()) {
                dataSource.onDataLoaded(result, force)
                PAGE++
            }
            loadListener?.get()?.invoke(true)

        } catch (e: Exception) {
            e.printStackTrace()
            loadListener?.get()?.invoke(e)
        }
    }

    /**
     * Call this method for inserting items
     * @param item - array of items, which will be inserted
     * @param callback - transaction callback during insert items process
     */
    fun insertItems(vararg item: API, callback: TransactionCallback? = null) {
        itemsAction(ItemsActionType.INSERT, *item, callback = callback)
    }

    /**
     * Call this method for delete items
     * @param item - array of items, which will be removed
     * @param callback - transaction callback during delete items process
     */
    fun deleteItems(vararg item: API, callback: TransactionCallback? = null) {
        itemsAction(ItemsActionType.DELETE, *item, callback = callback)
    }

    private fun itemsAction(itemsActionType: ItemsActionType,
                            vararg item: API,
                            callback: TransactionCallback? = null) {
        launch {
            try {
                val responseModel = when (itemsActionType) {
                    ItemsActionType.INSERT -> insertItemsApi(listOf(*item))
                    ItemsActionType.DELETE -> deleteItemsApi(listOf(*item))
                }
                if (!responseModel.success) {
                    callback?.invoke(Exception(responseModel.errorMessage))
                }

                when (itemsActionType) {
                    ItemsActionType.INSERT -> dataSource.onItemsInserted(responseModel.success, listOf(*item))
                    ItemsActionType.DELETE -> dataSource.onItemsDeleted(responseModel.success, listOf(*item))
                }
                callback?.invoke()

            } catch (sqlException: SQLException) {
                callback?.invoke(sqlException)
            }
        }
    }

    /**
     * Get values of searching parameter
     * @param param - searching parameter
     *
     * @return values of searching parameter
     */
    override fun getQuery(param: String): List<Any> {
        return dataSource.getQuery(param)
    }

    /**
     * Get map of searching parameters and its values
     *
     * @return map of searching parameters and its values
     */
    override fun getQueries(): Map<String, List<Any>> {
        return dataSource.getQueries()
    }

    /**
     * Set searching parameter and it values
     * @param force - set true if you want to reload data anyway
     * @param param - searching parameter
     * @param values - values of searching parameter
     */
    override fun setQuery(force: Boolean, param: String, values: List<Any>) {

        if (!validateQueryKey(param)) {
            throw InvalidQueryKeyException("Key \"$param\" marked as invalid. $INVALID_QUERY_KEY_MESSAGE_ADVICE")
        }

        dataSource.setQuery(param, values)
        updateQueries(force)
    }

    /**
     * Set map of searching parameters and its values
     * @param force - set true if you want to reload data anyway
     * @param params - map of searching parameters and its values
     */
    override fun setQueries(force: Boolean, params: HashMap<String, List<Any>>) {

        params.keys.forEach { key ->
            if (!validateQueryKey(key)) {
                throw InvalidQueryKeyException("Key \"$key\" marked as invalid. $INVALID_QUERY_KEY_MESSAGE_ADVICE")
            }
        }

        dataSource.setQueries(params)
        updateQueries(force)
    }

    /**
     * Clear all parameters
     * @param force - set true if you want to reload data anyway
     */
    override fun clearQueries(force: Boolean) {
        dataSource.clearQueries()
        updateQueries(force)
    }

    private fun updateQueries(force: Boolean) {
        dataSource.invalidateDataSource()

        PAGE = INITIAL_PAGE

        if (force) {
            refresh(force)
        }
    }

    /**
     * Check if this key match with any of your keys, which were declared for current searchable paging
     * @param key - current key
     *
     * @return boolean of matching current key with any of your keys
     */
    protected abstract fun validateQueryKey(key: String): Boolean

    /**
     * In this method build searching query for api and return the response from server request
     * @param page - from this page you should start searching on server
     * @param limit - limit of items per page
     * @param params - map of parameters to build searching query for api
     *
     * @return response from server request
     */
    protected abstract suspend fun loadData(page: Int, limit: Int, params: Map<String, List<Any>>): List<API>

    /**
     * In this method do your insert items request to backend
     * @param items - items, which should be inserted into backend
     *
     * @return response model from server request with success flag and error message
     */
    protected open suspend fun insertItemsApi(items: Collection<API>): ResponseModel {
        return ResponseModel(false)
    }

    /**
     * In this method do your delete items request to backend
     * @param items - items, which should be removed from backend
     *
     * @return response model from server request with success flag and error message
     */
    protected open suspend fun deleteItemsApi(items: Collection<API>): ResponseModel {
        return ResponseModel(false)
    }

    private class InvalidQueryKeyException(message: String) : Exception(message)

    protected data class ResponseModel(val success: Boolean, val errorMessage: String? = null)

    private enum class ItemsActionType {
        INSERT,
        DELETE
    }
}