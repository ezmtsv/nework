package ru.netology.nework.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nework.viewmodel.AuthViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.RegFragmentBinding
import ru.netology.nework.error.UnknownError
import ru.netology.nework.media.MediaUpload

@AndroidEntryPoint
class RegFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var pressBtn = false
    private var upload: MediaUpload? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = RegFragmentBinding.inflate(layoutInflater)

        fun showBar(txt: String) {
            Snackbar.make(
                binding.root,
                txt,
                Snackbar.LENGTH_LONG
            ).show()
        }
//println("viewModel.authState ${viewModel.authState.value}")
        with(binding) {
            btnSignIn.setOnClickListener {
                if (fieldLogin.editText?.text?.isEmpty() == true
                    || fieldPass.editText?.text?.isEmpty() == true
                    || fieldConfirm.editText?.text?.isEmpty() == true
                    || fieldName.editText?.text?.isEmpty() == true
                ) showBar("Все поля должны быть заполнены!")
                else {
                    if (fieldConfirm.editText?.text?.contentEquals(fieldPass.editText?.text) == false) {
                        showBar("Поля 'confirm' и 'password' содержат разные значения!")
                    } else {
                        if (upload != null) {
                            val login = fieldLogin.editText?.text?.toString()!!
                            val pass = fieldPass.editText?.text?.toString()!!
                            val name = fieldName.editText?.text?.toString()!!
                            viewModel.getRegFromServer(login, pass, name, upload!!)
                            pressBtn = true
                        } else showBar("Необходимо загрузить фото для профиля!")

                    }
                }
            }
        }


        viewModel.authState.observe(viewLifecycleOwner) { _ ->
            if (pressBtn) {
                findNavController().popBackStack()
            }
            viewModel.dataState.observe(viewLifecycleOwner) {
                if (it.error403) showBar("Пользователь с таким именем уже зарегистрирован!")
                if (it.error415) showBar("Неправильный формат фото!")
                if (it.error) showBar("Проверьте ваше подключение к сети!")
                binding.statusReg.isVisible = it.loading
            }
        }

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> showBar(ImagePicker.getError(it.data))

                Activity.RESULT_OK -> {
                    val uri: Uri? = it.data?.data
                    upload = MediaUpload(uri?.toFile())
                }

                Activity.RESULT_CANCELED -> {
                    upload = null
                }
            }
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .maxResultSize(2048, 2048)
                .createIntent(launcher::launch)

        }
//        binding.removePhoto.setOnClickListener {
//            viewModel.clearPhoto()
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
        curFrag?.getCurFragmentAttach(getString(R.string.sign_up))

    }
}
