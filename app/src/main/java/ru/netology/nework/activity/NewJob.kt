package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.databinding.NewJobBinding
import ru.netology.nework.date.DateJob
import ru.netology.nework.dialog.DialogSelectRemoveJob
import ru.netology.nework.dto.Job
import ru.netology.nework.util.AndroidUtils.getTimeJob
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID
import ru.netology.nework.viewmodel.UsersViewModel


@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewJob : Fragment() {
    private var startDate: String? = null
    private var finishDate: String? = null
    var binding: NewJobBinding? = null

    private val viewModelUser: UsersViewModel by viewModels()

    private fun showBar(txt: String) {
        Snackbar.make(
            binding!!.root,
            txt,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var lastStateLoading = false

        binding = NewJobBinding.inflate(layoutInflater)

        binding?.let {
            with(it) {
                dateJob.setOnClickListener {
                    DialogSelectRemoveJob.newInstance(SELECT_DATE, 0)
                        .show(childFragmentManager, "TAG")
                }

                btnCreate.setOnClickListener {
                    if (
                        fieldName.editText?.text?.isEmpty() == true ||
                        fieldPosition.editText?.text?.isEmpty() == true
                    ) {
                        showBar("Поля имя и должность должны быть заполнены!")
                    } else {
                        val name = fieldName.editText?.text.toString()
                        val position = fieldPosition.editText?.text.toString()
                        val link = fieldLink.editText?.text.toString()
                        val job = Job(0, myID, name, position, startDate, finishDate, link)
                        viewModelUser.saveJob(job)
                    }
                }
            }
        }

        viewModelUser.dataState.observe(viewLifecycleOwner) {
            binding?.progressLoad?.isVisible = it.loadingJob
            if (it.error403) showBar("Ошибка авторизации,отказано в доступе!")
            if (it.error404) showBar("Запись не найдена!")
            if (it.error400) showBar("Не указана дата или некорректно введены данные!")
            if (it.error) showBar("Проверьте ваше подключение к сети!")
            if (!it.loadingJob && lastStateLoading) findNavController().navigateUp()
            lastStateLoading = it.loadingJob
        }

        return binding!!.root
    }

    @SuppressLint("SetTextI18n")
    fun getDateJob(date: DateJob) {
        startDate = date.dateStart
        finishDate = date.dateEnd
        binding?.dateJob?.text = "${getTimeJob(startDate)} - ${getTimeJob(finishDate)}"
    }
}