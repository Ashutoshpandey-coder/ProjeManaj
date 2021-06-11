package com.example.projemanaj.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.projemanaj.activities.firebase.FireStoreClass
import com.example.projemanaj.databinding.ActivitySplashBinding
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN

        )
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUserId = FireStoreClass().getCurrentUserId()
            if (currentUserId.isNotEmpty()){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else{
                startActivity(Intent(this,IntroActivity::class.java))
            }
            finish()
        },2500)

        val typeFace : Typeface = Typeface.createFromAsset(assets,"PlayfairDisplay-Black.ttf")
        binding.tvAppName.typeface = typeFace
    }
}