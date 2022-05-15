package com.ai.camxmobile.screens

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ai.camxmobile.R
import com.ai.camxmobile.databinding.FragmentCameraBinding
import com.ai.camxmobile.databinding.FragmentImageDataBinding
import com.ai.camxmobile.viewmodels.CameraViewModel
import com.bumptech.glide.Glide
import com.google.android.material.composethemeadapter.MdcTheme
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
            runLabelDetection(camViewModel.capturedBitmap.value!!)
        }else{
            findNavController().navigateUp()
        }
    }

    private var labelList  = ArrayList<ImageLabel>()
    private fun runLabelDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                labelList.addAll(labels)
                binding.composeView.setContent {
                    MdcTheme {
                        Body()
                    }
                }
            }
            .addOnFailureListener { e ->

            }
    }

    @Composable
    private fun Body(){
        Column(
            modifier = Modifier.fillMaxSize(),
        ){
            Row(modifier = Modifier) {
                Text(
                    text = "1 - ",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth(),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.W500,
                )

                Text(
                    text = "Entity errorString!!",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth(),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.W500,
                )
            }
            Text(
                text = "Confidence Level",
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(start = 25.dp)
                    .wrapContentHeight()
                    .wrapContentWidth(),
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = FontWeight.W500,
            )
        }
    }

    @Composable
    private fun Item(imageLabel: ImageLabel, pos : Int){

    }
}