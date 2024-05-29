package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.ListMarkedUsers
import ru.netology.nework.dto.MarkedUser
import ru.netology.nework.dto.UserResponse

interface ListenerSelectionUser {
    fun selectUser(user: UserResponse?)
    fun addUser(idUser: Long?)
    fun removeUser(idUser: Long?)
}

class AdapterUsersList(
    private val listenerSelectionUser: ListenerSelectionUser,
    private val checkBoxUser: Boolean,
) : ListAdapter<UserResponse, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, listenerSelectionUser)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user, checkBoxUser)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val listenerSelectionUser: ListenerSelectionUser,
) : RecyclerView.ViewHolder(binding.root) {
    //    private val listUsers = mutableListOf<Long>()
    fun bind(user: UserResponse, checkBoxUser: Boolean) {
        binding.apply {

            nameUser.text = user.name
            loginUser.text = user.login
            if (checkBoxUser) checkbox.visibility = View.VISIBLE
            else checkbox.visibility = View.GONE
            checkbox.isChecked = ListMarkedUsers.listUsers.contains(MarkedUser(id = user.id))
            checkbox.setOnClickListener {
                if (checkbox.isChecked) {
                    listenerSelectionUser.addUser(user.id)
//                    val markedUser = MarkedUser(user.id, true)
                    ListMarkedUsers.addUser(MarkedUser(id = user.id))
                } else {
                    listenerSelectionUser.removeUser(user.id)
//                    val markedUser = MarkedUser(user.id, false)
                    ListMarkedUsers.removeUser(MarkedUser(id = user.id))
                }
            }
            Glide.with(avatarUser)
                .load(user.avatar)
                .placeholder(R.drawable.icon_person_24)
                .circleCrop()
                .timeout(35_000)
                .into(avatarUser)
            cardUser.setOnClickListener {
                listenerSelectionUser.selectUser(user)
            }
        }
    }
}

class UserDiffCallback : DiffUtil.ItemCallback<UserResponse>() {
    override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        return oldItem == newItem
    }

}
