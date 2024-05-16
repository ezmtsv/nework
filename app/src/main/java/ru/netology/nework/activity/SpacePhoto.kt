package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.activity.AppActivity.Companion.uriArg
import ru.netology.nework.databinding.FragmentSpacePhotoBinding
import ru.netology.nework.error.UnknownError

class SpacePhoto : Fragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSpacePhotoBinding.inflate(inflater, container, false)
        val uri = arguments?.uriArg

        Glide.with(binding.viewSpaceFoto)
            .load(uri)
            .error(R.drawable.err_load)
            .into(binding.viewSpaceFoto)

//        uri?.let {
//            with(binding){
//                viewSpaceFoto.settings.builtInZoomControls = true
//                viewSpaceFoto.loadUrl(uri)
//            }
//        }
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
        curFrag?.getCurFragmentAttach("Pic")

    }
}