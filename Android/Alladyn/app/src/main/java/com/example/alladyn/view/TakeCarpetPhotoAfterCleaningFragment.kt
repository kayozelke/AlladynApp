package com.example.alladyn.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.alladyn.databinding.FragmentTakeCarpetPhotoAfterCleaningBinding

class TakeCarpetPhotoAfterCleaningFragment : Fragment() {

    private var _binding: FragmentTakeCarpetPhotoAfterCleaningBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTakeCarpetPhotoAfterCleaningBinding.inflate(inflater,container,false)

        binding.addNewCarpetButton.setOnClickListener {
            val action = TakeCarpetPhotoAfterCleaningFragmentDirections.actionTakeCarpetPhotoAfterCleaningFragmentToTakeCarpetPhotoFragment()
            findNavController().navigate(action)
        }

        binding.carpetPhotoAfterButton.setOnClickListener {

            makeCarpetPhotoAfterCleaning()
            savePhotoAfterCleaningToDatabase()
        }

        binding.goToCarpetsListButton.setOnClickListener {
//            val action =
        }

        binding.goToRaportsButon.setOnClickListener {
//            val action =
        }

        binding.goToMainMenuButton.setOnClickListener {
            val action = TakeCarpetPhotoAfterCleaningFragmentDirections.actionTakeCarpetPhotoAfterCleaningFragmentToMainMenuFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun makeCarpetPhotoAfterCleaning() {

    }

    private fun savePhotoAfterCleaningToDatabase(){

    }

}