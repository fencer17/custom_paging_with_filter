package co.library.custom_paging_with_filter.database

import androidx.paging.DataSource
import co.library.paging.SearchableDao
import co.library.paging.SearchableDataSourceFactory
import co.library.custom_paging_with_filter.models.User

class UserSourceFactory(private val dao: UserDao) : SearchableDataSourceFactory<User, User>(dao) {

    companion object {
        const val KEY_LOGIN = "login"
        const val KEY_AVATAR = "avatar_url"
    }

    override fun getDataSource(dao: SearchableDao<User>,
                               params: HashMap<String, List<Any>>): DataSource.Factory<Int, User> {

        val loginQuery = params[KEY_LOGIN]?.filterIsInstance<String>()?.get(0) ?: "%"
        val avatarQuery = params[KEY_AVATAR]?.filterIsInstance<String>()?.get(0) ?: "%"

        return (dao as UserDao).getUsersDataSource(loginQuery, avatarQuery)
    }

    override suspend fun onDataLoaded(result: List<User>, force: Boolean) {
        dao.insertAll(result)
    }

    override suspend fun onItemsInserted(success: Boolean, insertedItems: Collection<User>) {
        dao.updateOrInsert(*insertedItems.toTypedArray())
    }

    override suspend fun onItemsDeleted(success: Boolean, deletedItems: Collection<User>) {
        dao.delete(*deletedItems.toTypedArray())
    }
}