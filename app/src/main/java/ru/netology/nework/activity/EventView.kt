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
import ru.netology.nework.activity.AppActivity.Companion.idArg
import ru.netology.nework.activity.AppActivity.Companion.listUserArg
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.adapter.AdapterEventView
import ru.netology.nework.adapter.OnEventListener
import ru.netology.nework.adapter.YaKit
import ru.netology.nework.databinding.EventViewBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.error.UnknownError
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventsViewModel
import ru.netology.nework.viewmodel.MediaViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class EventView : Fragment() {
    @Inject
    lateinit var yakit: YaKit
    var binding: EventViewBinding? = null

    private val viewModelEvents: EventsViewModel by viewModels()
    private val viewModelUsers: UsersViewModel by viewModels()
    private val viewModelMedia: MediaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EventViewBinding.inflate(layoutInflater)
        var txtShare = ""
        val idEvent = arguments?.idArg
        val adapterEventView = context?.let {
            AdapterEventView(binding!!, yakit, object : OnEventListener {
                override fun onLike(event: Event) {
                    if (AuthViewModel.userAuth) {
                        viewModelEvents.likeEvent(event, !event.likedByMe!!)
                    } else {
                        DialogAuth.newInstance(
                            AuthViewModel.DIALOG_IN,
                            "Для установки лайков нужно авторизоваться"
                        )
                            .show(childFragmentManager, "TAG")
                    }
                }

                override fun openSpacePhoto(event: Event) {
                    findNavController().navigate(
                        R.id.spacePhoto,
                        Bundle().apply {
                            uriArg = event.attachment?.url
                        }
                    )
                }

                override fun playVideo(url: String) {
                    viewModelMedia.playVideo(url, binding?.videoView!!)
                }

                override fun playAudio(url: String) {
                    if (binding?.playAudio!!.isChecked) {
                        viewModelMedia.playAudio(url)
                    } else {
                        viewModelMedia.pauseAudio()
                    }
                }

                override fun showUsers(users: List<Long>?) {
                    val list = users?.let { it -> viewModelUsers.selectUsers(it) }
                    findNavController().navigate(
                        R.id.tmpFrag,
                        Bundle().apply {
                            listUserArg = list
                        }
                    )
                }

                override fun participateEvan(event: Event) {
                    if (AuthViewModel.userAuth) {
                        viewModelEvents.participateEvent(event, !event.participatedByMe!!)
                    } else {
                        DialogAuth.newInstance(
                            AuthViewModel.DIALOG_IN,
                            "Для добавления в список участников нужно авторизоваться"
                        )
                            .show(childFragmentManager, "TAG")
                    }
                }
            })
        }
        viewModelEvents.receivedEvents.observe(viewLifecycleOwner) { events ->
            val event = events.find { it.id == idEvent }
            txtShare = (event?.attachment?.url ?: event?.content).toString()
            event?.let {
                adapterEventView?.bind(event)
            }
        }

        viewModelUsers.listUsers.observe(viewLifecycleOwner) {}

        viewModelMedia.duration.observe(viewLifecycleOwner){
            if(it != "STOP") binding?.duration!!.text = it
            else binding?.playAudio?.isChecked = false
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.share -> {
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

        return binding?.root
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
        curFrag?.getCurFragmentAttach(getString(R.string.event))

    }

    override fun onStop() {
        yakit.stopMapView()
        super.onStop()
    }

//    override fun onDestroy() {
//        super.onDestroy()
//    }

}