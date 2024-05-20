package ru.netology.nework.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.userArg
import ru.netology.nework.adapter.AdapterUsersList
import ru.netology.nework.adapter.ListenerSelectionUser
import ru.netology.nework.databinding.SelectionUsersBinding
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.UnknownError

class SelectionUsersFrag: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SelectionUsersBinding.inflate(layoutInflater)
        val listUsers = arguments?.listUserArg


        val adapterUserResponse = AdapterUsersList(object : ListenerSelectionUser{
            override fun selectUser(user: UserResponse?) {
                val id = user?.id
                findNavController().navigate(
                    R.id.userAccount,
                    id?.let {
                        Bundle().apply {
                            userArg = user
                        }
                    }

                )
            }

            override fun addUser(idUser: Long?) {

            }

            override fun removeUser(idUser: Long?) {

            }
        }, false)
        binding.listUsers.adapter = adapterUserResponse
        adapterUserResponse.submitList(listUsers)

        return binding.root
    }

    private var curFrag: CurrentShowFragment? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            curFrag = context as CurrentShowFragment
        } catch (e: ClassCastException) {
            throw UnknownError
        }
    }

    override fun onDetach() {
        super.onDetach()
        curFrag?.getCurFragmentDetach()
        curFrag = null
    }
}