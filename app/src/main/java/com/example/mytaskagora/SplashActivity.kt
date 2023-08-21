package com.example.mytaskagora

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed(

        4000){


            val intent = Intent(this,MainActivity::class.java)
            val progressBar = findViewById<View>(R.id.spinkit) as ProgressBar
            val doubleBounce: Sprite = DoubleBounce()
            progressBar.indeterminateDrawable = doubleBounce
            startActivity(intent)

        }






    }
}