package com.ai.camxmobile.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ai.camxmobile.databinding.FragmentCameraBinding
import com.ai.camxmobile.viewmodels.CameraViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import com.ai.camxmobile.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService

    private val camViewModel: CameraViewModel by activityViewModels()

    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater,container,false)

        //val usageStatsManager = requireActivity().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
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

        return binding.root
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())
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
                Toast.makeText(requireContext(),"An Error Occurred",Toast.LENGTH_SHORT).show()
                binding.shutterForeground.visibility = View.VISIBLE
                binding.shutterEffectView.visibility = View.GONE
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults){
                binding.shutterEffectView.visibility = View.GONE
                binding.shutterForeground.visibility = View.VISIBLE
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
                        Log.d(TAG, "Average luminosity: $luma")
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
                Log.e(TAG, "Use case binding failed", exc)
            }

            camViewModel.lensFacing.observe(requireActivity()) {
                try {
                    cameraProvider.unbindAll()
                    // Bind use cases to camera
                    camera = cameraProvider.bindToLifecycle(this, it!!, preview, imageCapture, imageAnalyzer)
                    cameraInfo = camera!!.cameraInfo
                } catch(exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
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

    private fun requestPermission(){

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
        private const val TAG = "CamX AI"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
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

    private fun runObjectDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        val objectDetector = ObjectDetection.getClient(options)
        objectDetector.process(image)
            .addOnSuccessListener {
                // Task completed successfully
                if(it.isNotEmpty()){
                    Log.i("Object",it[0].boundingBox.toString())
                }
            }
            .addOnFailureListener {
                // Task failed with an exception
                Log.e(TAG, it.message.toString())
            }
    }
}