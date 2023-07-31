package ru.netology.laliapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.laliapp.R
import ru.netology.laliapp.databinding.FragmentImageAttachmentBinding

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ImageAttachmentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val binding = FragmentImageAttachmentBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()

        binding.apply {
            imageViewImageFullScreen.visibility = View.GONE
            val url = arguments?.getString("url")

            Glide.with(imageViewImageFullScreen)
                .load(url)
                .placeholder(R.drawable.ic_baseline_loading_24)
                .error(R.drawable.ic_baseline_error_outline_24)
                .timeout(10_000)
                .into(imageViewImageFullScreen)

            imageViewImageFullScreen.visibility = View.VISIBLE
        }

        binding.imageViewImageFullScreen.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}