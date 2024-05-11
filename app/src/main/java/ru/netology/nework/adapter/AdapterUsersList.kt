package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.UserResponse

interface ListenerSelectionUser {
    fun selectUser(user: UserResponse?)
}

class AdapterUsersList(
    private val listenerSelectionUser: ListenerSelectionUser
) : ListAdapter<UserResponse, UserViewHolder>(UserDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, listenerSelectionUser)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val listenerSelectionUser: ListenerSelectionUser,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserResponse) {
        binding.apply {
            nameUser.text = user.name
            loginUser.text = user.login
            Glide.with(avatarUser)
                .load(user.avatar)
                .placeholder(R.drawable.icon_person_24)
                //.error(R.drawable.ic_error_100dp)
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
