package co.library.paging

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

interface BaseRepository<T> {

    fun getItems(): LiveData<PagedList<T>>

    fun refresh(force: Boolean = true)

    fun setOnLoadListener(listener: OnLoadListener)
}