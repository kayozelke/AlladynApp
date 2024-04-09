package com.example.alladyn.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.alladyn.R
import com.example.alladyn.databinding.FragmentTakeCarpetPhotoBinding
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TakeCarpetPhotoFragment : Fragment() {

    private var _binding: FragmentTakeCarpetPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var carpetAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTakeCarpetPhotoBinding.inflate(layoutInflater, container, false)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireContext()))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.carpetLoadingAnimation.apply {
            setBackgroundResource(R.drawable.carpet_animation)
            carpetAnimation = background as AnimationDrawable
        }
        carpetAnimation.start()

        binding.takeNewPhotoButton.setOnClickListener {
            capturePhotos()
        }
    }

    private fun capturePhotos() {
        val now = LocalDateTime.now()
        val dateTimeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val filePath = requireContext().filesDir.toString() + "/" + dateTimeString + "_images/"
        val resultFilePath = filePath + "average_image.png"

        val directory = File(filePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val python = Python.getInstance()

        val pythonFileRunCurl = python.getModule("run_curl")
        pythonFileRunCurl.callAttr("generateSamplePhotos", filePath)

        val pythonFileAverageImage = python.getModule("average_image")
        pythonFileAverageImage.callAttr("getAvgFromImages",filePath,resultFilePath)

        viewGeneratedImage(resultFilePath)
    }


    private fun viewGeneratedImage(resultFilePath: String){
        val imageFile = File(resultFilePath)
        if (imageFile.exists()){

            carpetAnimation.stop()
            binding.carpetLoadingAnimation.setBackgroundColor(resources.getColor(R.color.white))

            val bitmap: Bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            binding.carpetLoadingAnimation.setImageBitmap(bitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release the binding when the view is destroyed to avoid memory leaks
        _binding = null
    }

}