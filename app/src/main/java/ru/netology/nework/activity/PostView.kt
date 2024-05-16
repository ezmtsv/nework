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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterPostView
import ru.netology.nework.adapter.AdapterUserMentions
import ru.netology.nework.adapter.OnIteractionListenerPostView
import ru.netology.nework.databinding.PostViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.UnknownError
import ru.netology.nework.players.MPlayer
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth
import ru.netology.nework.viewmodel.PostsViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostView : Fragment() {
    private var audioPl: MPlayer? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: PostsViewModel by viewModels()
        val binding = PostViewBinding.inflate(layoutInflater)
        val idPost = arguments?.idArg
        var txtShare = ""



        val listMentions = listOf(
            UserResponse(
                id = 1L, "login1", name = "name1",
                avatar = "https://ik.imagekit.io/ube3bjrcz/92f2b97a-457d-46f7-be08-ee419ad07230_vNJ6FRcnK.jpg"
            ),
            UserResponse(
                id = 2L, "login2", name = "name2",
                avatar = "https://ik.imagekit.io/ube3bjrcz/1dcd6992-a0da-441c-908a-085c259c64ae_3ppxigjSA.png"
            ),
            UserResponse(
                id = 3L, "login3", name = "name3",
                avatar = "https://ik.imagekit.io/ube3bjrcz/9f369605-6b1a-4831-82d8-c794afe1bbe3_5doHTFYFe.jpg"
            ),
            UserResponse(
                id = 4L, "login4", name = "name4",
                avatar = "https://ik.imagekit.io/ube3bjrcz/92f2b97a-457d-46f7-be08-ee419ad07230_vNJ6FRcnK.jpg"
            ),
            UserResponse(
                id = 5L, "login5", name = "name5",
                avatar = "https://ik.imagekit.io/ube3bjrcz/2c323c6c-79bd-4562-8bfd-36ef87ac3db9_LAjf3CJuY.jpg"
            ),
            UserResponse(
                id = 6L, "login6", name = "name6",
                avatar = "https://ik.imagekit.io/ube3bjrcz/d55e8d0b-d46a-4f51-8cd3-58541a51c031_OsdFSRfrb.jpg"
            ),
            UserResponse(
                id = 7L, "login7", name = "name7",
                avatar = "https://ik.imagekit.io/ube3bjrcz/6ff7919f-1f8c-40fd-ad6e-d9869723e00d_M60-d19_t.jpg"
            ),

            )




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
                            TODO("Not yet implemented")
                        }

                        override fun onRemove(post: Post) {
                            TODO("Not yet implemented")
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
                    }, listMentions, context = cntx).bind(post)
                }
            }


//            val adapterUserResponse = AdapterUserMentions()
//            binding.listUsers.adapter = adapterUserResponse
//            adapterUserResponse.submitList(listMentions)
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

    override fun onStop() {
        audioPl?.stopPlayer()
        super.onStop()
    }

    override fun onDestroy() {
        audioPl?.stopPlayer()
        super.onDestroy()
    }
}