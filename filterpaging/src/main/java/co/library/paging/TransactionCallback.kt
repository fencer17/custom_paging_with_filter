package co.library.paging

import android.database.SQLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class TransactionCallback(private val coroutineContext: CoroutineContext = Dispatchers.Main) {

    private var onSuccess: (() -> Unit)? = null
    private var onNetworkError: ((Exception) -> Unit)? = null
    private var onDatabaseError: ((SQLException) -> Unit)? = null

    fun onSuccess(listener: () -> Unit) {
        onSuccess = listener
    }

    fun onNetworkError(listener: (Exception) -> Unit) {
        onNetworkError = listener
    }


    fun onDatabaseError(listener: (SQLException) -> Unit) {
        onDatabaseError = listener
    }

    operator fun invoke(exception: Exception) {
        runBlocking(coroutineContext) {
            onNetworkError?.invoke(exception)
        }
    }

    operator fun invoke(sqlException: SQLException) {
        runBlocking(coroutineContext) {
            onDatabaseError?.invoke(sqlException)
        }
    }

    operator fun invoke() {
        runBlocking(coroutineContext) {
            onSuccess?.invoke()
        }
    }
}