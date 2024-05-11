package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.adapter.AdapterPostView
import ru.netology.nework.adapter.OnIteractionListenerPostView
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.PostsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostView : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostsViewModel by viewModels()
        val binding = PostViewBinding.inflate(layoutInflater)
        val idPost = arguments?.idArg
        var txtShare = ""

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == idPost }
            txtShare = post?.content ?: "No content"
            post?.let {
                container?.context?.let { cntx ->
                    AdapterPostView(binding, object : OnIteractionListenerPostView {
                        override fun onLike(post: Post) {
                            if (userAuth) {
                                viewModel.like(post, !post.likedByMe)
                            } else {
                                DialogAuth.newInstance(
                                    AuthViewModel.DIALOG_IN,
                                    "Для установки лайков нужна авторизация, выполнить вход?"
                                )
                                    .show(childFragmentManager, "TAG")
                            }
                        }

                        override fun onShare(post: Post) {

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

                        override fun openCardPost(id: Long) {

                        }
                    }, listOf(), context = cntx).bind(post)
                }
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.share -> {
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://94.228.125.136:8080/swagger-ui/index.html#/Posts/getAll_3"))
//                        startActivity(intent)

                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, txtShare)
                            type = "text/plain"
                        }
                        val shareIntent =
                            Intent.createChooser(intent, "Share Post")
                        startActivity(shareIntent)
                        true
                    }

                    android.R.id.home -> {
                        println("home")
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)


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
        curFrag?.getCurFragmentAttach(getString(R.string.post))

    }
}