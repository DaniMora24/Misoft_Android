package com.misoft.misoftapp.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.misoft.misoftapp.R

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private val abrirLogin = Runnable {
        if (!isFinishing && !isDestroyed) {
            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Mostrar nuestro Splash personalizado
        setContentView(R.layout.activity_splash)

        // Duración del diseño personalizado
        handler.postDelayed(abrirLogin, 1500)
    }

    override fun onDestroy() {
        handler.removeCallbacks(abrirLogin)
        super.onDestroy()
    }
}
