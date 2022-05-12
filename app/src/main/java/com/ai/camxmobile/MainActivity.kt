package com.ai.camxmobile

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ai.camxmobile.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors

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
        /*when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val color = MaterialColors.getColor(this, R.attr.colorSecondaryContainer, Color.TRANSPARENT)
                window.navigationBarColor = getColorWithAlpha(color,0.36f)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                val color = MaterialColors.getColor(this, R.attr.colorSecondaryContainer, Color.TRANSPARENT)
                window.navigationBarColor = getColorWithAlpha(color,0.54f)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                val color = MaterialColors.getColor(this, R.attr.colorSecondaryContainer, Color.TRANSPARENT)
                window.navigationBarColor = getColorWithAlpha(color,0.54f)
            }
        }*/
        val color = MaterialColors.getColor(this, R.attr.backgroundColor, Color.TRANSPARENT)
        window.navigationBarColor = color
    }
}