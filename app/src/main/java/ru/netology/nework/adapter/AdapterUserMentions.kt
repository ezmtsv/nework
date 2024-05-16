package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.MentionUsersLayBinding
import ru.netology.nework.dto.UserResponse

class AdapterUserMentions: ListAdapter<UserResponse, MentionsViewHolder>(MentionsDiffCallBack) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionsViewHolder {
        val binding = MentionUsersLayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MentionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MentionsViewHolder, position: Int) {
        val userResponse = getItem(position)
        println("START")
        holder.bind(userResponse)
    }
}

class MentionsViewHolder(
    private val binding: MentionUsersLayBinding
): RecyclerView.ViewHolder(binding.root) {
        fun bind(userResponse: UserResponse){
            with(binding){
                userName.text = userResponse.name
                login.text = userResponse.login
                Glide.with(userAvatar)
                    .load(userResponse.avatar)
                    .placeholder(R.drawable.ic_loading_100dp)
                    //.error(R.drawable.ic_error_100dp)
                    .timeout(45_000)
                    .into(userAvatar)
            }
        }
}

object MentionsDiffCallBack: DiffUtil.ItemCallback<UserResponse>() {
    override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        println("areItemsTheSame")
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
        println("areItemsTheSame UserRespons")
        return oldItem == newItem
    }

}
