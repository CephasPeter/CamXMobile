package com.ai.camxmobile.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ai.camxmobile.databinding.FragmentImageHistoryBinding
import com.ai.camxmobile.models.ItemModel
import com.ai.camxmobile.viewmodels.CameraViewModel
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ImageHistoryFragment : Fragment() {
    private lateinit var binding: FragmentImageHistoryBinding
    private val camViewModel: CameraViewModel by activityViewModels()
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageHistoryBinding.inflate(inflater,container,false)
        setUpUI()
        return binding.root
    }

    private var labelList  = ArrayList<ItemModel>()
    private var bitmapMap  = ArrayMap<String, Bitmap>()
    private fun setUpUI(){
        binding.toolBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        if(labelList.isEmpty()){
            val observer: Observer<ArrayList<ItemModel>> = object : Observer<ArrayList<ItemModel>> {
                override fun onChanged(it: ArrayList<ItemModel>) {
                    if(it.isNotEmpty()){
                        ioScope.launch {
                            labelList.clear()
                            //labelList.addAll(it)
                            it.forEach { item ->
                                labelList.add(item)
                                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, Uri.parse(item.uri)))
                                } else {
                                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, Uri.parse(item.uri))
                                }
                                bitmapMap[item.id] = bitmap
                            }
                            mainScope.launch {
                                binding.loading.visibility = View.GONE
                                binding.composeView.setContent {
                                    MdcTheme {
                                        Body()
                                    }
                                }
                            }
                        }
                    }else{
                        binding.emptyText.visibility = View.VISIBLE
                    }
                    camViewModel.itemList.removeObserver(this)
                }
            }

            camViewModel.itemList.observe(requireActivity(),observer)
            if(camViewModel.itemList.value == null){
                camViewModel.getAllStoredData()
            }
        }else{
            binding.composeView.setContent {
                MdcTheme {
                    Body()
                }
            }
        }
    }

    @Composable
    private fun Body(){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
        ){
            items(
                items = labelList,
                key = {
                    it.id
                },
                itemContent = {
                    Item(it,labelList.indexOf(it)+1)
                }
            )
        }
    }

    @Composable
    private fun Item(item: ItemModel, pos : Int){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp, start = 10.dp, end = 10.dp).clickable {
                    camViewModel.capturedBitmap.value = bitmapMap[item.id]!!
                    camViewModel.capturedUri.value = Uri.parse(item.uri)
                    camViewModel.capturedName.value = item.name.toString()
                    
                    val action = ImageHistoryFragmentDirections.actionImageHistoryFragmentToImageDataFragment()
                    findNavController().navigate(action)
                },
        ){
            Row(modifier = Modifier) {
                Text(
                    text = pos.toString(),
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth().padding(end = 5.dp),
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.W500,
                    color = MaterialTheme.colors.primary
                )

                Image(
                    bitmap = bitmapMap[item.id]!!.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier.height(60.dp).width(60.dp).padding(end = 10.dp).clip(
                        RoundedCornerShape(10.dp)
                    ),
                    contentScale = ContentScale.FillBounds
                )
                Column(modifier = Modifier) {
                    Text(
                        text = item.name.toString(),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier
                            .wrapContentHeight()
                            .wrapContentWidth(),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colors.primary
                    )

                    Text(
                        text = item.imageLabel!!.size.toString() .plus(if(item.imageLabel!!.size > 1){" Entities"}else{" Entity"}),
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier
                            .wrapContentHeight()
                            .wrapContentWidth(),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.W500,
                    )

                    Text(
                        text = convertLongToTime(item.createdDate!!),
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier
                            .wrapContentHeight()
                            .wrapContentWidth(),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.W500,
                    )
                }
            }
        }
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
        return format.format(date)
    }
}