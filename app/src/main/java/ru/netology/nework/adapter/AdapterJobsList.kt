package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardWorkBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.util.AndroidUtils.getTimeJob
import ru.netology.nework.viewmodel.AuthViewModel.Companion.myID

interface ListenerSelectionJobs {
fun removeJob(job: Job)
}

class AdapterJobsList(
    private val listenerSelectionJobs: ListenerSelectionJobs,
): ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardWorkBinding.inflate(LayoutInflater.from(parent.context), parent, false )
        return JobViewHolder(binding, listenerSelectionJobs)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: CardWorkBinding,
    private val listenerSelectionJobs: ListenerSelectionJobs,
): RecyclerView.ViewHolder(binding.root) {
    fun bind(job: Job){
        binding.apply {
            companyName.text = job.name
            val startJob = getTimeJob(job.start)
            val finishJob = getTimeJob(job.finish)
            "$startJob - $finishJob".also { experience.text = it }
            jobTitle.text = job.position
            jobLink.text = job.link
            if(myID == job.idUser) trash.visibility = View.VISIBLE
            else trash.visibility = View.GONE
            trash.setOnClickListener{
                listenerSelectionJobs.removeJob(job)
            }
        }
    }
// "${job.start} - ${job.finish}"
//"${ getTimePublish(job.start) } - ${ getTimePublish(job.finish) }"
}

class JobDiffCallback: DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem.id == newItem.id


    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem == newItem


}
