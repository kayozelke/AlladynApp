package com.example.alladyn.view

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.alladyn.R
import com.example.alladyn.databinding.FragmentEnterCarpetDataAndSaveToDBBinding
import com.example.alladyn.datasource.DatabaseHandler
import com.example.alladyn.datasource.ServiceTypeData
import com.example.alladyn.model.CarpetModel
import com.google.android.material.button.MaterialButton
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KProperty


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

//    Timestamp as carpet ID
    private var timestamp: Long = 0L

//    Carpet pickup modes
    private var ifCarpetFromPrivatePerson: Boolean = false
    private var ifCarpetFromCompanyPartner: Boolean = false

//     Case carpet from private person
    private var carpetOwnerSurname: String = ""
    private var carpetOwnerPhoneNum: String = ""

//     Case carpet from a company partner
    private var partnerPickupPoint: String = ""
    private var carpetNumber: String = ""

//    Other general variables
    private var carpetPricePerMeter: Double = 0.0
    private var carpetFinalPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEnterCarpetDataAndSaveToDBBinding.inflate(inflater, container, false)

        updateUICurrentCarpetData()

        binding.individualServiceTypeButton.setOnClickListener {
            val serviceTypeData = ServiceTypeData()
            val serviceTypesList = serviceTypeData.loadServiceTypesData()
            val individualService =
                serviceTypesList.find { it.serviceTypeTag == "individual_service" }
            val individualServicePrice = individualService?.serviceTypePrice

//            Handle the button selection
            resetButtonStroke(binding.partnerServiceTypeButton)
            selectButton(binding.individualServiceTypeButton)

            if (individualServicePrice != null) {
                Log.d(TAG, "carpetPricePerMeter: $individualServicePrice")
                carpetPricePerMeter = individualServicePrice
                calculateCarpetPrice()
                updateUICarpetPrice()
                updateElementsVisibility()
            } else {
                Log.d(TAG, "individual_service price not found")
            }
//            Set proper vars
            ifCarpetFromPrivatePerson = true
            ifCarpetFromCompanyPartner = false
//            Set visibility for the individual client part
            binding.addPickupPointLinLayout.visibility = View.GONE
            binding.addContactDataLinLayout.visibility = View.VISIBLE
        }

        binding.partnerServiceTypeButton.setOnClickListener {
            val serviceTypeData = ServiceTypeData()
            val serviceTypesList = serviceTypeData.loadServiceTypesData()
            val partnerService = serviceTypesList.find { it.serviceTypeTag == "partner_service" }
            val partnerServicePrice = partnerService?.serviceTypePrice

//            Handle the button selection
            resetButtonStroke(binding.individualServiceTypeButton)
            selectButton(binding.partnerServiceTypeButton)

            if (partnerServicePrice != null) {
                Log.d(TAG, "carpetPricePerMeter: $partnerServicePrice")
                carpetPricePerMeter = partnerServicePrice
                calculateCarpetPrice()
                updateUICarpetPrice()
                updateElementsVisibility()
            } else {
                Log.d(TAG, "partner_service price not found")
            }
//            Set proper vars
            ifCarpetFromPrivatePerson = false
            ifCarpetFromCompanyPartner = true
//            Set visibility for the company partner part
            binding.addContactDataLinLayout.visibility = View.GONE
            binding.addPickupPointLinLayout.visibility = View.VISIBLE
        }

        binding.pickupPoint1Button.setOnClickListener {
//            Handle the button selection
            resetButtonStroke(binding.pickupPoint2Button)
            resetButtonStroke(binding.pickupPoint3Button)
            selectButton(binding.pickupPoint1Button)
//            Getting pickup point name
            partnerPickupPoint = binding.pickupPoint1Button.text.toString()
        }
        binding.pickupPoint2Button.setOnClickListener {
//            Handle the button selection
            resetButtonStroke(binding.pickupPoint1Button)
            resetButtonStroke(binding.pickupPoint3Button)
            selectButton(binding.pickupPoint2Button)
//            Getting pickup point name
            partnerPickupPoint = binding.pickupPoint2Button.text.toString()
        }
        binding.pickupPoint3Button.setOnClickListener {
//            Handle the button selection
            resetButtonStroke(binding.pickupPoint1Button)
            resetButtonStroke(binding.pickupPoint2Button)
            selectButton(binding.pickupPoint3Button)
//            Getting pickup point name
            partnerPickupPoint = binding.pickupPoint3Button.text.toString()
        }

        binding.backToCarpetMeasuresButton.setOnClickListener {
            val action =
                EnterCarpetDataAndSaveToDBFragmentDirections.
                actionEnterCarpetDataAndSaveToDBFragmentToTakeCarpetPhotoFragment()
            findNavController().navigate(action)
        }

        binding.saveCarpetToDbButton.setOnClickListener {
//            Getting input data that user may provided
            carpetOwnerSurname = binding.ownerSurnameTextInput.text.toString()
            carpetOwnerPhoneNum = binding.ownerPhoneNumberTextInput.text.toString()
            carpetNumber = binding.carpetNumberTextInput.text.toString()

            val dataCheck = checkIfAllDataCompleted()
            if (dataCheck) {
                saveCarpetToDatabase()
            } else {
                val message = getString(R.string.nie_wype_niono_wszystkich_niezb_dnych_p_l)
                val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, message, duration)
                toast.show()
            }

            val action =
                EnterCarpetDataAndSaveToDBFragmentDirections.
                actionEnterCarpetDataAndSaveToDBFragmentToTakeCarpetPhotoAfterCleaningFragment(
                    carpetID = timestamp
                )
            findNavController().navigate(action)
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

    private fun calculateCarpetPrice() {
        carpetFinalPrice =
            Math.round(carpetPricePerMeter * args.carpetArea.toDouble() * 100.0) / 100.0;
        Log.d(TAG, "carpetFinalPrice: $carpetFinalPrice")
    }

    private fun updateUICarpetPrice() {
        binding.carpetMeterPriceTextview.text =
            getString(
                R.string.cena_za_czyszczenie_metra_kw_dywanu_wynosi,
                carpetPricePerMeter.toString()
            )

        binding.finalCarpetPriceTextview.text =
            getString(
                R.string.ostateczna_cena_tego_dywanu_wynosi_s_z_otych,
                carpetFinalPrice.toString()
            )
    }

    private fun updateElementsVisibility() {
        binding.apply {
            carpetMeterPriceTextview.visibility = View.VISIBLE
            finalCarpetPriceTextview.visibility = View.VISIBLE
            everythingGoodTextview.visibility = View.VISIBLE
            backToMeasureTextview.visibility = View.VISIBLE
            backToCarpetMeasuresButton.visibility = View.VISIBLE
            saveCarpetToDbButton.visibility = View.VISIBLE
        }
    }

    private fun checkIfAllDataCompleted(): Boolean {
        if (!ifCarpetFromPrivatePerson and !ifCarpetFromCompanyPartner) {
//            User did not choose the pickup option
            return false
        }

        if (ifCarpetFromPrivatePerson) {
            if (carpetOwnerSurname.isEmpty() and carpetOwnerPhoneNum.isEmpty()) {
//                User did choose private person option but did not fill any contact info
                return false
            }
        }

        if (ifCarpetFromCompanyPartner) {
            if (partnerPickupPoint.isEmpty() or carpetNumber.isEmpty()) {
//                User did choose company partner option but did not fill all of the data
                return false
            }
        }
        return true
    }

    private fun saveCarpetToDatabase() {
        val now = LocalDateTime.now()
//            Average image from a few photos from the camera server
        val bitmapAvgImage = BitmapFactory.decodeFile(averageImageFilePath)
        val baosAvgImg = ByteArrayOutputStream()
        bitmapAvgImage.compress(Bitmap.CompressFormat.JPEG, 100, baosAvgImg)
        val dataAvgImg = baosAvgImg.toByteArray()
        val base64AvgImg: String = Base64.encodeToString(dataAvgImg, Base64.DEFAULT)
//            Average photo with measured carpet
        val bitmapMeasuredImg = BitmapFactory.decodeFile(measuredImageFilePath)
        val baosMeasuredImg = ByteArrayOutputStream()
        bitmapMeasuredImg.compress(Bitmap.CompressFormat.JPEG, 100, baosMeasuredImg)
        val dataMeasuredImg = baosMeasuredImg.toByteArray()
        val base64MeasuredImg: String = Base64.encodeToString(dataMeasuredImg, Base64.DEFAULT)

        val databaseHandler = DatabaseHandler()
        val dbReference = databaseHandler.getDatabaseReference()
        dbReference.let { userReference ->

            // Generate a unique carpet ID using timestamp
            timestamp = System.currentTimeMillis()

            // Set the timestamp as a value under the generated route ID
            userReference.child("carpets").child(timestamp.toString()).push().setValue(timestamp)

            val carpetToAdd = CarpetModel(
//                Basic metric data
                length = carpetLength.toDouble(),
                width = carpetWidth.toDouble(),
                metricArea = carpetArea.toDouble(),
//                Individual client or partner company
                ifPrivateClient = ifCarpetFromPrivatePerson,
                ifCompanyPartner = ifCarpetFromCompanyPartner,
//                Individual client data
                ownerSurname = carpetOwnerSurname,
                ownerPhoneNumber = carpetOwnerPhoneNum,
//                Partner company data
                pickUpPoint = partnerPickupPoint,
                carpetNumber = carpetNumber,
//                Other necessary data
                date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                price = carpetFinalPrice,
//                Carpet photos
                photoBefore = base64AvgImg,
                photoMeasured = base64MeasuredImg,
                photoAfter = null
            )

            val carpetMap: HashMap<String, Any?> = hashMapOf(
                "length" to carpetToAdd.length,
                "width" to carpetToAdd.width,
                "metricArea" to carpetToAdd.metricArea,
                "ifPrivateClient" to carpetToAdd.ifPrivateClient,
                "ifCompanyPartner" to carpetToAdd.ifCompanyPartner,
                "ownerSurname" to carpetToAdd.ownerSurname,
                "ownerPhoneNumber" to carpetToAdd.ownerPhoneNumber,
                "pickUpPoint" to carpetToAdd.pickUpPoint,
                "carpetNumber" to carpetToAdd.carpetNumber,
                "date" to carpetToAdd.date,
                "price" to carpetToAdd.price,
                "photoBefore" to carpetToAdd.photoBefore,
                "photoMeasured" to carpetToAdd.photoMeasured,
                "photoAfter" to carpetToAdd.photoAfter
            )

            userReference.child("carpets").child(timestamp.toString()).setValue(carpetMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Carpet successfully added to the database.")
                    val message = "Carpet successfully added to the database."
                    val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Adding carpet to the database has failed.")
                    val message = "Adding carpet to the database has failed."
                    val duration = Toast.LENGTH_LONG // or Toast.LENGTH_SHORT
                    val toast = Toast.makeText(context, message, duration)
                    toast.show()
                }
        }

    }

    private fun selectButton(button: MaterialButton) {
        button.cornerRadius = 90
        button.strokeWidth = 6
        button.strokeColor = ColorStateList.valueOf(Color.CYAN)
    }

    private fun resetButtonStroke(button: MaterialButton) {
        button.strokeWidth = 0
        button.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
    }
}
