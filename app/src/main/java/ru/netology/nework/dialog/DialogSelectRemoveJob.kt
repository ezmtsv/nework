package ru.netology.nework.dialog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import ru.netology.nework.R

import ru.netology.nework.activity.HIDE
import ru.netology.nework.activity.REMOVE_JOB
import ru.netology.nework.databinding.SelectDateBinding
import ru.netology.nework.date.DateJob
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.AndroidUtils.getDate
import java.lang.Exception
import java.util.Calendar

interface ListenerDialogSelectDate {
    fun returnDateJob(date: DateJob)
    fun returnIdJob(id: Long)
}

class DialogSelectRemoveJob : DialogFragment() {
    private var sel = 0
    private var idJob: Long? = null
    private var selectStart: Boolean = true
    private var timeStartForServer: String? = null
    private var timeEndtForServer: String? = null
    var binding: SelectDateBinding? = null
    private var listener: ListenerDialogSelectDate? = null

    companion object {
        private const val SEL_DIALOG = "SEL_DIALOG"
        private const val ID_JOB = "ID_JOB"
        private val args = Bundle()

        fun newInstance(select: Int, idJob: Long): DialogSelectRemoveJob {
            args.putInt(SEL_DIALOG, select)
            args.putLong(ID_JOB, idJob)
            return DialogSelectRemoveJob()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SelectDateBinding.inflate(layoutInflater)
        sel = args.getInt(SEL_DIALOG)
        idJob = args.getLong(ID_JOB)

        with(binding!!) {

            if (sel == REMOVE_JOB) {
                fieldHeader.text = "Удаление записи о работе"
                fieldInfo.text = "Вы уверены, что хотите удалить запись?"
                fieldInfo.textSize = 20F
                fieldStartDate.visibility = HIDE
                fieldEndDate.visibility = HIDE
                btnOk.text = getString(R.string.yes)
                btnChanel.text = getString(R.string.no)
            }
            fieldStartDate.setText(getDate())
            fieldEndDate.setText("НВ")

            val td = AndroidUtils.getTimeFormat(Calendar.getInstance())
            timeStartForServer = getDateForServer(td)

            fieldStartDate.setOnTouchListener { _, event ->
                selectStart = true
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        DatePickerDialog(
                            requireContext(),
                            datePickerDialog,
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        ).show()
                        false
                    }

                    else -> {
                        true
                    }
                }
            }

            fieldEndDate.setOnTouchListener { _, event ->
                selectStart = false
                when (event.action) {
                    MotionEvent.ACTION_UP -> {
                        DatePickerDialog(
                            requireContext(),
                            datePickerDialog,
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                        ).show()
                        false
                    }

                    else -> {
                        true
                    }
                }
            }
            btnChanel.setOnClickListener {
                dismiss()
            }
            btnOk.setOnClickListener {
                if (sel == REMOVE_JOB) {
                    listener?.returnIdJob(idJob!!)
                } else {
                    var dateJob = DateJob()
                    timeStartForServer?.let {
                        dateJob = dateJob.copy(dateStart = it)
                    }
                    timeEndtForServer?.let {
                        dateJob = dateJob.copy(dateEnd = it)
                    }
                    listener?.returnDateJob(dateJob)
                }
                dismiss()
            }
        }
        return binding!!.root
    }


    private var datePickerDialog =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val dateAndTime = Calendar.getInstance()
            dateAndTime.set(Calendar.YEAR, year)
            dateAndTime.set(Calendar.MONTH, monthOfYear)
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val td = AndroidUtils.getTimeFormat(dateAndTime)
            if (selectStart) {
                binding?.fieldStartDate?.setText(getDate(dateAndTime))
                timeStartForServer = getDateForServer(td)
            } else {
                binding?.fieldEndDate?.setText(getDate(dateAndTime))
                timeEndtForServer = getDateForServer(td)
            }
        }

    private fun getDateForServer(dateTime: String): String{
        val date = dateTime.subSequence(0, 10)
        val time = dateTime.subSequence(11, 16)
        return  "${date}T${time}:33.874Z"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ListenerDialogSelectDate
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}

