package co.library.paging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class OnLoadListener(private val coroutineContext: CoroutineContext = Dispatchers.Main) {

    private var onStart: (() -> Unit)? = null
    private var onFinish: (() -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    fun onStartLoad(listener: () -> Unit) {
        onStart = listener
    }

    fun onFinishLoad(listener: () -> Unit) {
        onFinish = listener
    }


    fun onErrorLoad(listener: (Throwable) -> Unit) {
        onError = listener
    }

    operator fun invoke(error: Throwable) {
        runBlocking(coroutineContext) {
            onError?.invoke(error)
        }
    }

    operator fun invoke(isFinish: Boolean = false) {
        runBlocking(coroutineContext) {
            if (isFinish) {
                onFinish?.invoke()
            } else {
                onStart?.invoke()
            }
        }
    }
}