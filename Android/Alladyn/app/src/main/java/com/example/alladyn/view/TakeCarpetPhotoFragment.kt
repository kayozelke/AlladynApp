package com.example.alladyn.view

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.alladyn.R
import com.example.alladyn.databinding.FragmentTakeCarpetPhotoBinding
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.navigation.fragment.findNavController


class TakeCarpetPhotoFragment : Fragment() {

    companion object {
        const val TAG = "CarpetPhotoFragment"
    }

    private var _binding: FragmentTakeCarpetPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var carpetAnimation: AnimationDrawable

    // General variables - carpet measures
    private var carpetLength: Double? = null
    private var carpetWidth: Double? = null
    private var carpetSquareArea: Double? = null

    // Paths to the files, passed to the next fragment
    private var averageImageFilePath: String = ""
    private var measuredImageFilePath: String = ""


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
            updateUICarpetMeasurements()
        }

        binding.saveGoNextButton.setOnClickListener {
            val isAutomaticallyGeneratedDataCorrect: Boolean = checkAutomaticallyGeneratedData()
            Log.d(TAG,"isAutomaticallyGeneratedDataCorrect: $isAutomaticallyGeneratedDataCorrect")
            val isDataChangedByUser: Boolean = checkIfUserEnteredNewCarpetData()
            Log.d(TAG,"isDataChangedByUser: $isDataChangedByUser")
            val areImageFilePathsNotEmpty: Boolean = checkImageFilePaths()

            if ((isAutomaticallyGeneratedDataCorrect or isDataChangedByUser) && areImageFilePathsNotEmpty)  {
                val action = TakeCarpetPhotoFragmentDirections.actionTakeCarpetPhotoFragmentToEnterCarpetDataAndSaveToDBFragment(
                    carpetLength = carpetLength!!.toFloat(),
                    carpetWidth = carpetWidth!!.toFloat(),
                    carpetArea = carpetSquareArea!!.toFloat(),
                    averageImageFilePath = averageImageFilePath,
                    measuredImageFilePath = measuredImageFilePath
                )
                findNavController().navigate(action)
            } else {
                val message = getString(R.string.something_went_wrong_try_again)
                val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, message, duration)
                toast.show()
            }
        }
    }

    private fun capturePhotos() {
        val now = LocalDateTime.now()
        val python = Python.getInstance()
        val dateTimeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val imagesFilePath = requireContext().filesDir.toString() + "/" + dateTimeString + "_images/"
        averageImageFilePath = imagesFilePath + "average_image.png"
        measuredImageFilePath = imagesFilePath + "measuredImage.png"

        // Make a directory if it does not exists
        val directory = File(imagesFilePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Generate photos (max 10) and save to imagesFilePath
        val pythonFileRunCurl = python.getModule("run_curl")
        pythonFileRunCurl.callAttr("generateSamplePhotos", imagesFilePath)

        // Generate average image from previously taken photos
        // In order to remove any artifacts
        val pythonFileAverageImage = python.getModule("average_image")
        pythonFileAverageImage.callAttr("getAvgFromImages",imagesFilePath,averageImageFilePath)

        // Show the average photo to the user
//        viewGeneratedImage(averageImageFilePath)

        // Measure an object on the generate photo
        val pythonFileComputeMeasurements = python.getModule("measure_objects")
        // Below also measuredImageFilePath will be given to the real script
        val measurements = pythonFileComputeMeasurements.callAttr("alladyn_main", averageImageFilePath, measuredImageFilePath)
        val inputString = measurements.toString()

        // Show the photo with carpet measures to the user
        viewGeneratedImage(measuredImageFilePath)

        // Parse the input string as a JSON object
        val jsonObject = JSONObject(inputString)

        // Access and print the extracted values
        carpetLength = jsonObject.getDouble("length")
        carpetWidth = jsonObject.getDouble("width")
        carpetSquareArea = jsonObject.getDouble("area")

        Log.d(TAG, "carpetLength: $carpetLength")
        Log.d(TAG, "carpetWidth: $carpetWidth")
        Log.d(TAG, "carpetSquareArea: $carpetSquareArea")
    }


    private fun viewGeneratedImage(resultFilePath: String){
        val imageFile = File(resultFilePath)
        if (imageFile.exists()){
            val bitmap: Bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            carpetAnimation.stop()
            binding.carpetLoadingAnimation.setBackgroundColor(resources.getColor(R.color.white))
            binding.carpetLoadingAnimation.setImageBitmap(bitmap)
        }
    }

    private fun updateUICarpetMeasurements(){

        binding.lengthTextview.apply {
            text = getString(R.string.len_text_string, carpetLength.toString())
//            visibility = View.VISIBLE
        }

//        binding.multiplyIcon.visibility = View.VISIBLE

        binding.widthTextview.apply{
            text = getString(R.string.wid_text_string, carpetWidth.toString())
//            visibility = View.VISIBLE
        }

//        binding.areaTextview.visibility = View.VISIBLE

        binding.carpetMetricAreaTextview.apply{
            text = getString(R.string.area_text_string, carpetSquareArea.toString())
//            visibility = View.VISIBLE
        }
    }

    private fun checkImageFilePaths() : Boolean {
        return averageImageFilePath.isNotEmpty() && measuredImageFilePath.isNotEmpty()
    }

    private fun checkAutomaticallyGeneratedData() : Boolean {
        return carpetLength != null && carpetWidth != null && carpetSquareArea != null
    }

    private fun checkIfUserEnteredNewCarpetData() : Boolean {
        val lengthInput = binding.lengthTextInput.text.toString()
        val widthInput = binding.widthTextInput.text.toString()
        val carpetMetricInput = binding.carpetMetricAreaTextInput.text.toString()

        // Everything is empty - not change anything and return
        if (lengthInput.isEmpty() && widthInput.isEmpty() && carpetMetricInput.isEmpty()){
            Log.d(TAG, "Everything is empty - not change anything and return")
            return false
        }
        // User typed carpet's square area properly
        else if (carpetMetricInput.toDoubleOrNull() != null) { // is not null so it is Double
            Log.d(TAG, "User typed carpet's square area properly")
            // Save that as a general carpet's square area
            carpetSquareArea = carpetMetricInput.toDouble()
            return true
        }
        // Used typed carpet's length and width properly
        else if (lengthInput.toDoubleOrNull() != null && widthInput.toDoubleOrNull() != null) { // is not null so it is Double
            Log.d(TAG, "Used typed carpet's length and width properly")
            // Calculate and save the carpet's square area
            carpetLength = lengthInput.toDouble()
            carpetWidth = widthInput.toDouble()
            carpetSquareArea = lengthInput.toDouble() * widthInput.toDouble()
            return true
        }
        // User typed some data but not correctly
        else {
            Log.d(TAG, "User typed some data but not correctly")
            val message = getString(R.string.check_entered_carpet_measurements)
            val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, message, duration)
            toast.show()
            return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release the binding when the view is destroyed to avoid memory leaks
        _binding = null
    }

}