package co.library.custom_paging_with_filter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import co.library.custom_paging_with_filter.R
import co.library.custom_paging_with_filter.models.User
import kotlinx.android.synthetic.main.user_item.view.*
import java.lang.StringBuilder

class UserPagedListAdapter(diffUtilCallback: DiffUtil.ItemCallback<User>): PagedListAdapter<User, UserPagedListAdapter.UserViewHolder>(diffUtilCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(user: User?) {
            user?.let {
                itemView.tvLogin.text = StringBuilder(user.id.toString())
                    .append(".")
                    .append(user.login)
            }
        }
    }
}