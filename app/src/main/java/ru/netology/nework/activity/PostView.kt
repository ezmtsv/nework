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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.postArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterPostView
import ru.netology.nework.adapter.OnIteractionListenerPostView
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.MediaViewModel
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostView : Fragment() {
    private val viewModelMedia: MediaViewModel by viewModels()

    @Inject
    lateinit var yakit: YaKit
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostsViewModel by viewModels()
        val viewModelUsers: UsersViewModel by viewModels()
        val binding = PostViewBinding.inflate(layoutInflater)
        val post = arguments?.postArg
        val txtShare = (post?.attachment?.url ?: post?.content).toString()


        val adapter = container?.context?.let { _ ->
            AdapterPostView(binding, object : OnIteractionListenerPostView {
                override fun onLike(post: Post) {
                    if (userAuth) {
                        viewModel.like(post, !post.likedByMe)
                    } else {
                        DialogAuth.newInstance(
                            AuthViewModel.DIALOG_IN,
                            "Для установки лайков нужна нужно авторизоваться"
                        )
                            .show(childFragmentManager, "TAG")
                    }
                }

                override fun onEdit(post: Post) {

                }

                override fun onRemove(post: Post) {

                }

                override fun playAudio(link: String) {
                    if (binding.playAudio.isChecked) {
                        viewModelMedia.playAudio(link)
                    } else {
                        viewModelMedia.pauseAudio()
                    }
                }

                override fun playVideo(link: String) {
                    viewModelMedia.playVideo(link, binding.videoView)
                }

                override fun openSpacePhoto(post: Post) {
                    findNavController().navigate(
                        R.id.spacePhoto,
                        Bundle().apply {
                            uriArg = post.attachment?.url
                        }
                    )
                }

                override fun showUsers(users: List<Long>?) {
                    //val list = viewModelUsers.selectUsers(listOf(65, 66, 67, 68, 69, 70))
                    val list = users?.let { viewModelUsers.selectUsers(it) }
                    findNavController().navigate(
                        R.id.tmpFrag,
                        Bundle().apply {
                            listUserArg = list
                        }
                    )
                }
            }, yakit)
        }


        viewModelUsers.listUsers.observe(viewLifecycleOwner) {}

        viewModel.receivedPosts.observe(viewLifecycleOwner) { posts ->
            val _post = posts.find { it.id == post?.id }
            _post?.let {
                adapter?.bind(_post)
            }
        }


        viewModelMedia.duration.observe(viewLifecycleOwner) {
            if (it != "STOP") binding.duration.text = it
            else binding.playAudio.isChecked = false
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
                        viewModelMedia.stopAudio()
                        findNavController().navigateUp()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)



        return binding.root
    }

    fun stopMedia() {
        viewModelMedia.stopAudio()
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

    override fun onStop() {
        yakit.stopMapView()
        super.onStop()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//    }
}

