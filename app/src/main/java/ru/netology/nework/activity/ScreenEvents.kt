package ru.netology.nework.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.eventArg
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.adapter.AdapterEventsList
import ru.netology.nework.adapter.OnEventsListener
import ru.netology.nework.databinding.ScreenEventsBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.EventsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint

class ScreenEvents : Fragment() {

    var binding: ScreenEventsBinding? = null
    val viewModelEvent: EventsViewModel by viewModels()
    private val viewModelAuth: AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ScreenEventsBinding.inflate(layoutInflater)
        binding?.bottomNavigationEvents?.selectedItemId = R.id.menu_events

        fun showBar(txt: String) {
            Snackbar.make(
                binding?.root!!,
                txt,
                Snackbar.LENGTH_LONG
            ).show()
        }

        val adapterEvents = AdapterEventsList(object : OnEventsListener {
            override fun onLike(event: Event) {
                if (userAuth) {
                    viewModelEvent.likeEvent(event, !event.likedByMe!!)
                } else {
                    DialogAuth.newInstance(
                        AuthViewModel.DIALOG_IN,
                        "Для установки лайков нужно авторизоваться"
                    )
                        .show(childFragmentManager, "TAG")
                }
            }

            override fun onShare(event: Event) {
                val txtShare = (event.attachment?.url?: event.content).toString()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, txtShare)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, "Share Post")
                startActivity(shareIntent)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.newEvent,
                    Bundle().apply {
                        eventArg = event
                    }
                )
            }

            override fun onRemove(event: Event) {
                if (event.authorId == myID){
                    viewModelEvent.removeEvent(event)
                }

            }

            override fun openCardEvent(event: Event) {
                findNavController().navigate(
                    R.id.eventView,
                    Bundle().apply {
                        idArg = event.id!!
                    }
                )
            }

            override fun onParticipants(event: Event) {
                if (userAuth) {
                    viewModelEvent.participateEvent(event, !event.participatedByMe!!)
                } else {
                    DialogAuth.newInstance(
                        AuthViewModel.DIALOG_IN,
                        "Для добавления в список участников нужно авторизоваться"
                    )
                        .show(childFragmentManager, "TAG")
                }
            }
        })

        binding?.listEvents?.adapter = adapterEvents

//        viewModelEvent.events.observe(viewLifecycleOwner) { list ->
//            adapterEvents.submitList(list)
//        }

        fun reload() {
            Snackbar.make(binding?.root!!, R.string.error_loading, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry_loading) { adapterEvents.refresh() }
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelEvent.events.collectLatest(adapterEvents::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapterEvents.loadStateFlow.collectLatest { state ->
                    binding?.swipeRefreshLayout?.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                    if (state.refresh is LoadState.Error) reload()
                }
            }
        }

        viewModelAuth.authState.observe(viewLifecycleOwner) { _ ->
            adapterEvents.refresh()
        }

        viewModelEvent.dataState.observe(viewLifecycleOwner) { state ->
            binding?.progress?.isVisible = state.loading
            binding?.swipeRefreshLayout?.isRefreshing = state.refreshing

            if (state.errorNetWork) {
                reload()
            }

            if (state.error403) {
                userAuth = false
                myID = null
                showBar("Ошибка авторизации, выполните вход")
//                viewModelEvent.loadEvents()
            }

//            if(!state.statusAuth){
//                showBar("Выход из аккаунта!") выполнен вход
//            }

        }

        binding?.swipeRefreshLayout?.setOnRefreshListener {
//            viewModelEvent.loadEvents()
            adapterEvents.refresh()
        }
        binding?.swipeRefreshLayout?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light,
        )

        binding?.bottomNavigationEvents?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_posts -> {
                    findNavController().navigate(
                        R.id.screenPosts,
                    )
                    true
                }

                R.id.menu_users -> {
                    findNavController().navigate(
                        R.id.screenUsers,
                    )
                    true
                }

                R.id.menu_events -> {

                    true
                }

                else -> false
            }
        }

        binding?.fabEvent?.setOnClickListener {
            if (userAuth) {
                findNavController().navigate(
                    R.id.newEvent,
                    Bundle().apply {
                        eventArg = null
                    }
                )
            } else {
                DialogAuth.newInstance(
                    AuthViewModel.DIALOG_IN,
                    "Для добавления события нужно авторизоваться"
                )
                    .show(childFragmentManager, "TAG")
            }
        }

        return binding?.root
    }

    override fun onResume() {
        binding?.bottomNavigationEvents?.selectedItemId = R.id.menu_events
        super.onResume()
    }

//    override fun onStart() {
////        viewModelEvent.loadEvents()
//        super.onStart()
//    }

}