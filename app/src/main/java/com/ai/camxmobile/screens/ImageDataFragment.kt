package com.ai.camxmobile.screens

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ai.camxmobile.R
import com.ai.camxmobile.databinding.FragmentCameraBinding
import com.ai.camxmobile.databinding.FragmentImageDataBinding
import com.ai.camxmobile.viewmodels.CameraViewModel
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.File

class ImageDataFragment : Fragment() {
    private lateinit var binding: FragmentImageDataBinding
    private val camViewModel: CameraViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageDataBinding.inflate(inflater,container,false)
        setUpUI()
        return binding.root
    }

    private fun setUpUI(){
        if(camViewModel.capturedBitmap.value != null){
            binding.capturedImage.setImageBitmap(camViewModel.capturedBitmap.value)
        }else{
            findNavController().navigateUp()
        }
    }

    private var labelList : ArrayList<ImageLabel>()
    private fun runLabelDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                labels.forEachIndexed { index, imageLabel ->

                }
            }
            .addOnFailureListener { e ->

            }
    }
}