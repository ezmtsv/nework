package ru.netology.nework.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.postArg
import ru.netology.nework.adapter.AdapterScreenPosts
import ru.netology.nework.adapter.OnIteractionListener
import ru.netology.nework.databinding.ScreenPostsBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.PostsViewModel
import androidx.paging.LoadState
import ru.netology.nework.viewmodel.AuthViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint

class ScreenPosts : Fragment() {
    val viewModel: PostsViewModel by viewModels()
    private val viewModelAuth: AuthViewModel by viewModels()
    var binding: ScreenPostsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = ScreenPostsBinding.inflate(inflater, container, false)
//        val binding = ScreenPostsBinding.inflate(inflater, container, false)
        binding?.bottomNavigation?.selectedItemId = R.id.screenPosts

        fun showBar(txt: String) {
            Snackbar.make(
                binding?.root!!,
                txt,
                Snackbar.LENGTH_LONG
            ).show()
        }

        val adapter = AdapterScreenPosts(object : OnIteractionListener {
            override fun onLike(post: Post) {
                if (userAuth) {
                    viewModel.like(post, !post.likedByMe)
                } else {
                    DialogAuth.newInstance(
                        DIALOG_IN,
                        "Для установки лайков нужно авторизоваться"
                    )
                        .show(childFragmentManager, "TAG")

//                    DialogAddJob.newInstance("SELECT_DATE", "12321321")
//                        .show(childFragmentManager, "TAG")
                }
            }

            override fun onShare(post: Post) {
                val txtShare = (post.attachment?.url ?: post.content).toString()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, txtShare)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, "Share Post")
                startActivity(shareIntent)

            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.newPostFrag,
                    Bundle().apply {
                        postArg = post
                    }
                )
            }

            override fun onRemove(post: Post) {
                if (post.authorId == myID)
                    viewModel.removePost(post)
                //viewModel.loadPosts()
            }

            override fun openCardPost(post: Post) {

                findNavController().navigate(
                    R.id.postView,
                    Bundle().apply {
                        postArg = post
                    }
                )
            }

        })

        fun reload() {
            Snackbar.make(binding?.root!!, R.string.error_loading, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry_loading) { adapter.refresh() }
                .show()
        }

        binding?.list?.adapter = adapter

//binding?.list?.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->  }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding?.swipeRefreshLayout?.isRefreshing =
                        state.refresh is LoadState.Loading ||
                                state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                    if (state.refresh is LoadState.Error) reload()
                }
            }
        }

        viewModelAuth.authState.observe(viewLifecycleOwner) { _ ->
            adapter.refresh()
        }


        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding?.progress?.isVisible = state.loading
            binding?.swipeRefreshLayout?.isRefreshing = state.refreshing
//            if (state.errorNetWork) {
////                Snackbar.make(binding?.root!!, R.string.error_loading, Snackbar.LENGTH_LONG)
////                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
////                    .show()
////                reload()
//            }

            if (state.error403) {
                userAuth = false
                myID = null
                showBar("Ошибка авторизации, выполните вход")
//                viewModel.loadPosts()
            }
            if (state.error415) {
                showBar("Неправильный формат файла!")
            }

//            if(!state.statusAuth){
//                showBar("Выход из аккаунта!") выполнен вход
//            }

        }

        binding?.swipeRefreshLayout?.setOnRefreshListener {
//            viewModel.loadPosts()
            adapter.refresh()
        }
        binding?.swipeRefreshLayout?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_red_light,
        )

        binding?.bottomNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_posts -> {
//                    viewModel.loadPosts()
//                    adapter.refresh()
                    true
                }

                R.id.menu_events -> {
//                    println("click EVENTS")
                    findNavController().navigate(
                        R.id.screenEvents,
                    )
                    true
                }

                R.id.menu_users -> {
                    findNavController().navigate(
                        R.id.screenUsers,
                    )
                    true
                }

                else -> false
            }

        }

        binding?.fab?.setOnClickListener {
            if (userAuth) {
                findNavController().navigate(
                    R.id.newPostFrag,
                    Bundle().apply {
                        postArg = null
                    }
                )
            } else {
                DialogAuth.newInstance(
                    DIALOG_IN,
                    "Для добавления поста нужно авторизоваться"
                )
                    .show(childFragmentManager, "TAG")
            }

        }

        return binding?.root!!
    }

//    override fun onStart() {
////        viewModel.loadPosts()
//        super.onStart()
//    }

    override fun onResume() {
        binding?.bottomNavigation?.selectedItemId = R.id.menu_posts
        super.onResume()
    }
}