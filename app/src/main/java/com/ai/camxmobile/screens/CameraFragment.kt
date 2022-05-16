package com.ai.camxmobile.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ai.camxmobile.R
import com.ai.camxmobile.databinding.FragmentCameraBinding
import com.ai.camxmobile.viewmodels.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

@AndroidEntryPoint
class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val camViewModel: CameraViewModel by activityViewModels()

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater,container,false)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.shutterForeground.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = requireActivity().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator

                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            }else{
                val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(100)
                }
            }

            binding.shutterForeground.visibility = View.GONE
            binding.shutterEffectView.visibility = View.VISIBLE
            takePhoto()
        }

        binding.flipCamera.setOnClickListener {
            if(camViewModel.lensFacing.value == CameraSelector.DEFAULT_FRONT_CAMERA){
                camViewModel.lensFacing.value = CameraSelector.DEFAULT_BACK_CAMERA
            }else{
                camViewModel.lensFacing.value = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        }

        binding.choosePicture.setOnClickListener {
            selectImageFromGalleryResult.launch("image/*")
        }

        binding.viewSaved.setOnClickListener {
            val action = CameraFragmentDirections.actionCameraFragmentToImageHistoryFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val imageUri: Uri = it

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri))
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            }
            val fileName = getFileName(imageUri).toString()

            val storagePath = File(requireContext().filesDir.path + "/Images/")
            storagePath.mkdirs()
            val myImage = File(storagePath, fileName)

            try {
                val out = FileOutputStream(myImage)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()

                camViewModel.capturedBitmap.value = bitmap
                camViewModel.capturedUri.value = Uri.fromFile(myImage)
                camViewModel.capturedName.value = fileName

                val action = CameraFragmentDirections.actionCameraFragmentToImageDataFragment()
                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = "CAMX-"+System.currentTimeMillis().toString()
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CamX")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(requireActivity().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireActivity()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(requireContext(),"Unable To Take Photo",Toast.LENGTH_SHORT).show()
                binding.shutterForeground.visibility = View.VISIBLE
                binding.shutterEffectView.visibility = View.GONE
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults){
                if(output.savedUri != null){
                    binding.shutterEffectView.visibility = View.GONE
                    binding.shutterForeground.visibility = View.VISIBLE
                    val imageUri: Uri = output.savedUri!!

                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, imageUri))
                    } else {
                        MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                    }

                    val fileName = getFileName(imageUri).toString()
                    val storagePath = File(requireContext().filesDir.path + "/Images/")
                    storagePath.mkdirs()
                    val myImage = File(storagePath, fileName)

                    try {
                        val out = FileOutputStream(myImage)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.flush()
                        out.close()

                        camViewModel.capturedBitmap.value = bitmap
                        camViewModel.capturedUri.value = Uri.fromFile(myImage)
                        camViewModel.capturedName.value = fileName

                        val action = CameraFragmentDirections.actionCameraFragmentToImageDataFragment()
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        if(camViewModel.lensFacing.value !=null){
            lensFacing = camViewModel.lensFacing.value!!
        }else{
            camViewModel.lensFacing.value = lensFacing
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        mainScope.launch {
                            binding.light.text = luma.toInt().toString()
                        }
                        //Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            var camera: Camera? = null
            var cameraInfo: CameraInfo? = null
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(this, lensFacing, preview, imageCapture, imageAnalyzer)
                cameraInfo = camera.cameraInfo
            } catch(exc: Exception) {
                exc.printStackTrace()
            }

            camViewModel.lensFacing.observe(requireActivity()) {
                try {
                    cameraProvider.unbindAll()
                    // Bind use cases to camera
                    camera = cameraProvider.bindToLifecycle(this, it!!, preview, imageCapture, imageAnalyzer)
                    cameraInfo = camera!!.cameraInfo
                } catch(exc: Exception) {
                    exc.printStackTrace()                }
            }

            camViewModel.flashEnabled.observe(requireActivity()){
                if(camera!=null && it !=null){
                    val cameraControl = camera!!.cameraControl
                    cameraControl.enableTorch(it)
                    val flashState = cameraInfo!!.torchState
                    if (flashState.value == 1){
                        binding.flash.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_round_flash_on_24))
                    }else{
                        binding.flash.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_round_flash_off_24))
                    }
                }
            }

            val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    if(camera!=null){
                        val cameraControl = camera!!.cameraControl
                        val scale = cameraInfo!!.zoomState.value!!.zoomRatio * detector.scaleFactor
                        cameraControl.setZoomRatio(scale)
                    }
                    return true
                }
            }

            val scaleGestureDetector = ScaleGestureDetector(requireActivity(), listener)

            binding.flash.setOnClickListener {
                if(camera!=null){
                    val cameraControl = camera!!.cameraControl
                    camViewModel.flashEnabled.value = camViewModel.flashEnabled.value != true
                }
            }

            binding.viewFinder.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = requireActivity().contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }
}