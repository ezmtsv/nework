package ru.netology.nework.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterPostView
import ru.netology.nework.adapter.OnIteractionListenerPostView
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.error.UnknownError
import ru.netology.nework.players.MPlayer
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.PostsViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostView : Fragment() {
    private var audioPl: MPlayer? = null
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
        val idPost = arguments?.idArg
        var txtShare = ""


        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val post = posts.find { it.id == idPost }
            txtShare = (post?.attachment?.url ?: post?.content).toString()
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

                        override fun onEdit(post: Post) {

                        }

                        override fun onRemove(post: Post) {

                        }

                        override fun playAudio(link: String) {
                            if (audioPl == null) {
                                audioPl = MPlayer()
                            }
                            if (binding.playAudio.isChecked) {
                                audioPl?.play(link, object : MPlayer.GetInfo {
                                    override fun getDuration(dut: Int) {
                                        if (dut != 0) binding.duration.text =
                                            AndroidUtils.getTimeTrack(dut)
                                    }

                                    override fun onCompletionPlay() {
                                        audioPl?.stopPlayer()
                                    }

                                })
                            } else {
                                audioPl?.pausePlayer()
                            }
                        }

                        override fun playVideo(link: String) {
                            binding.videoView.apply {
                                setMediaController(MediaController(context))
                                setVideoURI(
                                    Uri.parse(link)
                                )
                                setOnPreparedListener {
                                    start()
                                }
                            }

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
                    }, yakit, context = cntx).bind(post)
                }
            }
        }

        viewModelUsers.listUsers.observe(viewLifecycleOwner) {}

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

    override fun onStop() {
        yakit.stopMapView()
        audioPl?.stopPlayer()
        super.onStop()
    }

    override fun onDestroy() {
        audioPl?.stopPlayer()
        super.onDestroy()
    }
}

