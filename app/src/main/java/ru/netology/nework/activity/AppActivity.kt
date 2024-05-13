package ru.netology.nework.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels

import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.ActivityAppBinding
import ru.netology.nework.dialog.DialogAuth
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.util.LongEditArg
import ru.netology.nework.util.StringArg
import ru.netology.nework.util.UserArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_IN
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_OUT
import ru.netology.nework.viewmodel.AuthViewModel.Companion.DIALOG_REG
import ru.netology.nework.viewmodel.AuthViewModel.Companion.userAuth


@AndroidEntryPoint

class AppActivity : AppCompatActivity(), DialogAuth.ReturnSelection, CurrentShowFragment {
    private var actionBar: ActionBar? = null
    private var imageView: ImageView? = null
    val viewModel: AuthViewModel by viewModels()

    companion object {
        var Bundle.idArg: Long by LongEditArg
        var Bundle.uriArg: String? by StringArg
        var Bundle.userArg: UserResponse? by UserArg
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actionBar = supportActionBar
        imageView = ImageView(this)
        imageView?.setImageResource(R.drawable.icon_person_24)


        val lp = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.END
        actionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setCustomView(imageView, lp)
        }
        //actionBar?.hide()

//        viewModel.statusAuth.observe(this) {
//            println("viewModel.authState.value?.login ${viewModel.authState.value?.login}")
//            println("userAuth $userAuth")
//            actionBar?.apply {
////                if(userAuth) {
//
////                    subtitle = viewModel.authState.value?.login
//                subtitle = ""
////                }
////                else subtitle = ""
//            }
//        }


        imageView?.setOnClickListener {
            PopupMenu(this, it).apply {
                inflate(R.menu.menu_main)
                menu.setGroupVisible(R.id.authenticated, AuthViewModel.userAuth)
                menu.setGroupVisible(R.id.unauthenticated, !AuthViewModel.userAuth)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.signin -> {
                            findNavController(R.id.nav_host_fragment).navigate(
                                R.id.authFragment
                            )
                            true
                        }

                        R.id.signup -> {
                            findNavController(R.id.nav_host_fragment).navigate(
                                R.id.regFragment
                            )
                            true
                        }

                        R.id.signout -> {
                            DialogAuth.newInstance(DIALOG_OUT, "Выйти из аккаунта?")
                                .show(supportFragmentManager, "TAG")
                            true
                        }

                        R.id.account -> {
                            if (userAuth) {
                                val myAcc = viewModel.getMyAcc()
//                                println("myAcc $myAcc")
                                findNavController(R.id.nav_host_fragment).navigate(
                                    R.id.userAccount,
                                    Bundle().apply {
                                        userArg = myAcc
                                    }
                                )
                            }
                            true
                        }

                        else -> false
                    }
                }
            }.show()

        }
        //findNavController(R.id.nav_host_fragment).navigate(R.id.screenPosts)

    }

    override fun returnDialogValue(select: Int) {
        when (select) {
            DIALOG_OUT -> {
                viewModel.deleteAuth()
                Toast.makeText(
                    this@AppActivity,
                    "Вы вышли из аккаунта.", Toast.LENGTH_LONG
                )
                    .show()

            }

            DIALOG_IN -> {
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.authFragment
                )
            }

            DIALOG_REG -> {
                println("fragment registration")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.setDisplayHomeAsUpEnabled(destination.id != R.id.screenPosts)
        }
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    override fun getCurFragmentAttach(headerTitle: String) {
        actionBar.apply {
            title = headerTitle
            imageView?.visibility = View.GONE

        }
    }

    override fun getCurFragmentDetach() {
        actionBar.apply {
            title = getString(R.string.app_name)
            imageView?.visibility = View.VISIBLE
        }
    }

}