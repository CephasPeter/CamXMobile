package com.ai.camxmobile.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ai.camxmobile.R
import com.ai.camxmobile.databinding.FragmentCameraBinding
import com.ai.camxmobile.databinding.FragmentImageDataBinding

class ImageDataFragment : Fragment() {
    private lateinit var binding: FragmentImageDataBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageDataBinding.inflate(inflater,container,false)
        return binding.root
    }
}