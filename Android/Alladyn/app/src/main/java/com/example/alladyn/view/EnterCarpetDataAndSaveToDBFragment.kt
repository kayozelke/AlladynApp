package com.example.alladyn.view

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alladyn.R
import com.example.alladyn.databinding.FragmentEnterCarpetDataAndSaveToDBBinding
import com.example.alladyn.datasource.DatabaseHandler
import com.example.alladyn.datasource.ServiceTypeData
import com.google.android.material.button.MaterialButton
import java.io.File
import kotlin.math.ceil


class EnterCarpetDataAndSaveToDBFragment : Fragment() {

    companion object {
        const val TAG = "SaveCarpetFragment"
    }

    private var _binding: FragmentEnterCarpetDataAndSaveToDBBinding? = null
    private val binding get() = _binding!!

    // Arguments received form the TakeCarpetPhotoFragment
    private val args: EnterCarpetDataAndSaveToDBFragmentArgs by navArgs()
    private val carpetLength: Float by lazy { args.carpetLength }
    private val carpetWidth: Float by lazy { args.carpetWidth }
    private val carpetArea: Float by lazy { args.carpetArea }
    private val averageImageFilePath: String by lazy { args.averageImageFilePath }
    private val measuredImageFilePath: String by lazy { args.measuredImageFilePath }

    // Other global variables
    private var buttonsAdded = false
    private var selectedButton: MaterialButton? = null
    private var carpetPricePerMeter: Double = 0.0
    private var carpetFinalPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEnterCarpetDataAndSaveToDBBinding.inflate(inflater, container, false)

        updateUICurrentCarpetData()
//        if (!buttonsAdded) {
//            showServiceTypesButtons()
//            buttonsAdded = true
//            Log.d(TAG, "buttonsAdded = true")
//        }

        binding.individualServiceTypeButton.setOnClickListener {
            val serviceTypeData = ServiceTypeData()
            val serviceTypesList = serviceTypeData.loadServiceTypesData()
            val individualService = serviceTypesList.find { it.serviceTypeTag == "individual_service" }
            val individualServicePrice = individualService?.serviceTypePrice
            if (individualServicePrice != null) {
                Log.d(TAG, "carpetPricePerMeter: $individualServicePrice")
                carpetPricePerMeter = individualServicePrice
                calculateCarpetPrice()
                updateUICarpetPrice()
            } else {
                Log.d(TAG, "individual_service price not found")
            }
        }

        binding.partnerServiceTypeButton.setOnClickListener {
            val serviceTypeData = ServiceTypeData()
            val serviceTypesList = serviceTypeData.loadServiceTypesData()
            val partnerService = serviceTypesList.find { it.serviceTypeTag == "partner_service" }
            val partnerServicePrice = partnerService?.serviceTypePrice
            if (partnerServicePrice != null) {
                Log.d(TAG, "carpetPricePerMeter: $partnerServicePrice")
                carpetPricePerMeter = partnerServicePrice
                calculateCarpetPrice()
                updateUICarpetPrice()
            } else {
                Log.d(TAG, "partner_service price not found")
            }
        }

        binding.backToCarperMeasuresButton.setOnClickListener{
            val action = EnterCarpetDataAndSaveToDBFragmentDirections.actionEnterCarpetDataAndSaveToDBFragmentToTakeCarpetPhotoFragment()
            findNavController().navigate(action)
        }

        binding.saveCarpetToDbButton.setOnClickListener {
            val dataBaseHandler = DatabaseHandler()
//            dataBaseHandler.saveCarpetToDatabase()
            val message = "TO BE DONE"
            val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
            val toast = Toast.makeText(context, message, duration)
            toast.show()
        }

        return binding.root
    }

    private fun updateUICurrentCarpetData() {

        // Average photo update
        val averageImageFile = File(averageImageFilePath)
        if (averageImageFile.exists()) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(averageImageFile.absolutePath)
            binding.averageImageview.setBackgroundColor(resources.getColor(R.color.white))
            binding.averageImageview.setImageBitmap(bitmap)
        }

        // Photo with measures update
        val measuredImageFile = File(measuredImageFilePath)
        if (measuredImageFile.exists()) {
            val bitmap: Bitmap = BitmapFactory.decodeFile(measuredImageFile.absolutePath)
            binding.measuredImageview.setBackgroundColor(resources.getColor(R.color.white))
            binding.measuredImageview.setImageBitmap(bitmap)
        }

        // Carpet measurements update
        binding.currentCarpetMeasuresTextview.text =
            getString(R.string.wymiary_s_m_x_s_m, carpetLength.toString(), carpetWidth.toString())

        // Carpet area update
        binding.currentCarpetAreaTextview.text =
            getString(R.string.powierzchnia_s_m2, carpetArea.toString())
    }

//    private fun showServiceTypesButtons() {
//        val datasource = ServiceTypeData()
//        val serviceTypesList = datasource.loadServiceTypesData()
//        val linearLayout = binding.serviceTypeLinearLayout
//
//        for (serviceType in serviceTypesList) {
//            val button = MaterialButton(requireContext())
//
//            button.text = serviceType.serviceTypeName
//            Log.d(TAG, "button.text = ${serviceType.serviceTypeName}")
//            val layoutParams = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                1.0f
//            )
//            layoutParams.width = 0
//            button.layoutParams = layoutParams
//
//            linearLayout.addView(button)
//
//            button.setOnClickListener {
//                selectedButton?.backgroundTintList = null
//                button.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
//                selectedButton = button
//                carpetPricePerMeter = serviceType.serviceTypePrice
//                calculateCarpetPrice()
//                updateUICarpetPrice()
//            }
//        }
//    }

    private fun calculateCarpetPrice(){
        carpetFinalPrice = Math.round(carpetPricePerMeter * args.carpetArea.toDouble() * 100.0) / 100.0;
        Log.d(TAG, "carpetFinalPrice: $carpetFinalPrice")
    }

    private fun updateUICarpetPrice(){
        binding.carpetMeterPriceTextview.text =
            getString(R.string.cena_za_czyszczenie_metra_kw_dywanu_wynosi, carpetPricePerMeter.toString())

        binding.finalCarpetPriceTextview.text =
            getString(R.string.ostateczna_cena_tego_dywanu_wynosi_s_z_otych, carpetFinalPrice.toString())
    }
}
