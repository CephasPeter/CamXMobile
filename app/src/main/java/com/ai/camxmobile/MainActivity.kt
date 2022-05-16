package com.ai.camxmobile

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ai.camxmobile.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setUpUI()
    }

    private fun setUpUI(){
        val color = MaterialColors.getColor(this, R.attr.backgroundColor, Color.TRANSPARENT)
        window.navigationBarColor = color
        window.addFlags (WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}