package com.tit.nimonsapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tit.nimonsapp.data.network.Api
import com.tit.nimonsapp.data.network.WebSocketManager
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.repository.SessionRepository
import com.tit.nimonsapp.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionRepository: SessionRepository

    companion object {
        lateinit var webSocketRepository: WebSocketRepository
            private set
    }

    private val bottomNavDestinations =
        setOf(
            R.id.homeFragment,
            R.id.mapFragment,
            R.id.familiesFragment,
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize API with context for AuthInterceptor
        Api.initialize(this)
        sessionRepository = SessionRepository(this)

        // Initialize Shared WebSocket Repository
        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        val webSocketManager = WebSocketManager(client)
        webSocketRepository = WebSocketRepository(webSocketManager)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility =
                if (destination.id in bottomNavDestinations) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        // Manage WebSocket connection lifecycle globally
        observeSessionForWebSocket()
    }

    private fun observeSessionForWebSocket() {
        lifecycleScope.launch {
            sessionRepository.tokenFlow.collectLatest { token ->
                if (token != null) {
                    webSocketRepository.connect(token)
                } else {
                    webSocketRepository.disconnect()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketRepository.disconnect()
    }
}
