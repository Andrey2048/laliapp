package ru.netology.laliapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.FragmentNewJobBinding
import ru.netology.laliapp.dto.DATE_WORK_NOW
import ru.netology.laliapp.dto.STRING_FOR_LINK
import ru.netology.laliapp.utils.AndroidUtils
import ru.netology.laliapp.utils.AndroidUtils.selectDateDialog
import ru.netology.laliapp.utils.pickDate
import ru.netology.laliapp.viewmodel.JobViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NewJobFragment : Fragment() {

    private val jobViewModel by activityViewModels<JobViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )

        (activity as AppCompatActivity).supportActionBar?.title =
            context?.getString(R.string.title_job)

        binding.editTextStart.setOnClickListener {
            selectDateDialog(binding.editTextStart, requireContext())
            jobViewModel.startDate(binding.editTextStart.text.toString())
        }

        binding.editTextFinish.setOnClickListener {
            selectDateDialog(binding.editTextFinish, requireContext())
            jobViewModel.finishDate(binding.editTextFinish.text.toString())
        }

        val name = arguments?.getString("name")
        val position = arguments?.getString("position")
        val start = arguments?.getString("start")
        val finish = arguments?.getString("finish")
        val link = arguments?.getString("link")
        val workNow = getResources().getString(R.string.text_job_now)

        binding.editTextCompany.setText(name)
        binding.editTextPosition.setText(position)
        binding.editTextStart.setText(start)
        binding.editTextFinish.setText(
            finish ?: workNow
        )
        binding.editTextLink.setText(if (link == STRING_FOR_LINK) "" else link) /* костыль из-за бэкенда. Там link обязательное поле */

        binding.buttonSave.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            binding.let {
                if (
                    it.editTextCompany.text.isNullOrBlank() ||
                    it.editTextPosition.text.isNullOrBlank() ||
                    it.editTextStart.text.isNullOrBlank()
                ) {
                    Toast.makeText(
                        activity,
                        R.string.error_field_required,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    jobViewModel.changeJobData(
                        name = it.editTextCompany.text.toString(),
                        position = it.editTextPosition.text.toString(),
                        start = it.editTextStart.text.toString(),
                        finish = if (it.editTextFinish.text.toString() == workNow) DATE_WORK_NOW else it.editTextFinish.text.toString(),
                        link = if ((it.editTextLink.text == null) || (it.editTextLink.text.toString() == "")) STRING_FOR_LINK else it.editTextLink.text.toString(),
                    ) /* костыль из-за бэкенда. Там link и finish обязательные поля */
                    jobViewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
            }
        }

        binding.editTextStart.setOnClickListener {
            context?.let { item ->
                pickDate(binding.editTextStart, item)
            }
        }

        binding.editTextFinish.setOnClickListener {
            context?.let { item ->
                pickDate(binding.editTextFinish, item)
            }
        }

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        jobViewModel.dataState.observe(viewLifecycleOwner) {
            when {
                it.error -> {
                    Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.progressBar.isVisible = it.loading
        }

        return binding.root
    }
}