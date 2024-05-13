package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.activity.AppActivity.Companion.userArg
import ru.netology.nework.adapter.AdapterJobsList
import ru.netology.nework.adapter.AdapterScreenPosts
import ru.netology.nework.adapter.ListenerSelectionJobs
import ru.netology.nework.adapter.OnIteractionListener
import ru.netology.nework.databinding.UserAccountBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint

class UserAccount : Fragment() {

    private val viewModelUser: UsersViewModel by viewModels()
    private val viewModelPosts: PostsViewModel by viewModels()

    private var nameLoginUser = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = UserAccountBinding.inflate(layoutInflater)
//        var user = arguments?.userArg?:{
//
//        }
        val idUser = arguments?.userArg?.id!!
//        println("idUser $idUser")
        nameLoginUser =
            "${arguments?.userArg?.name.toString()} / ${arguments?.userArg?.login.toString()}"

        fun showBar(txt: String) {
            Snackbar.make(
                binding.root,
                txt,
                Snackbar.LENGTH_LONG
            ).show()
        }

        lifecycleScope.launch {
            viewModelUser.getUser(idUser)
            viewModelUser.getUserJobs(idUser)
            viewModelPosts.getUserPosts(idUser)
        }

        val adapterJobs = AdapterJobsList(object : ListenerSelectionJobs {

        })

        binding.listJobs.adapter = adapterJobs

        val adapterPosts = AdapterScreenPosts(object : OnIteractionListener {
            override fun onLike(post: Post) {
                if (AuthViewModel.userAuth) {
                    viewModelPosts.like(post, !post.likedByMe)
                } else {
                    DialogAuth.newInstance(
                        AuthViewModel.DIALOG_IN,
                        "Для установки лайков нужна авторизация, выполнить вход?"
                    )
                        .show(childFragmentManager, "TAG")
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, "Share Post")
                startActivity(shareIntent)
            }

            override fun onEdit(post: Post) {
                TODO("Not yet implemented")
            }

            override fun onRemove(post: Post) {
                TODO("Not yet implemented")
            }

            override fun openLinkVideo(post: Post) {
                TODO("Not yet implemented")
            }

            override fun openCardPost(post: Post) {
                findNavController().navigate(
                    R.id.postView,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
            }

        })

        binding.listPosts.adapter = adapterPosts

//        val manager = LinearLayoutManager(context)
//        binding.listPosts.layoutManager = manager
//        binding.listPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
////                println("layoutManager.findFirstCompletelyVisibleItemPosition() ${manager.findFirstCompletelyVisibleItemPosition()}")
//                when(manager.findFirstCompletelyVisibleItemPosition()){
//                    0 -> binding.imageAvatar.visibility = View.VISIBLE
//                    else -> binding.imageAvatar.visibility = View.GONE
//                }
//            }
//        })
        viewModelUser.listUsers.observe(viewLifecycleOwner) { users ->
            viewModelUser.takeUser(users.find { it.id == idUser })
        }

        viewModelUser.userAccount.observe(viewLifecycleOwner) { user ->
//            println("avatar ${user.avatar}")
            with(binding) {
                Glide.with(imageAvatar)
                    .load(user.avatar)
                    .timeout(35_000)
                    .into(imageAvatar)


            }
        }

        viewModelUser.userJobs.observe(viewLifecycleOwner) { jobs ->
            adapterJobs.submitList(
                jobs.filter { it.idUser == idUser }
            )
        }

        viewModelPosts.userWall.observe(viewLifecycleOwner) { posts ->
            if (binding.listPosts.isVisible) {
                adapterPosts.submitList(posts)
            }
        }

        viewModelPosts.data.observe(viewLifecycleOwner) { posts ->
            viewModelPosts.takePosts(posts.filter { it.authorId == idUser })
        }

        viewModelUser.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModelUser.getUserJobs(idUser) }
                    .show()
            } else if (state.error404) {
                showBar("Пользователь не найден!")
            } else {
                if (state.error) {
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModelUser.getUser(idUser) }
                        .show()
                }
            }
        }

        binding.wallJobsNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.work -> {
                    binding.listPosts.visibility = View.GONE
                    binding.listJobs.visibility = View.VISIBLE
                    true
                }

                R.id.jobs -> {
                    binding.listPosts.visibility = View.VISIBLE
                    binding.listJobs.visibility = View.GONE
                    true
                }

                else -> false
            }
        }

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

    override fun onStart() {
        super.onStart()
        curFrag?.getCurFragmentAttach(nameLoginUser)

    }

}