package com.tit.nimonsapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tit.nimonsapp.data.network.Api
import com.tit.nimonsapp.data.network.NetworkMonitor
import com.tit.nimonsapp.data.network.WebSocketManager
import com.tit.nimonsapp.data.network.WebSocketRepository
import com.tit.nimonsapp.data.repository.SessionRepository
import com.tit.nimonsapp.databinding.ActivityMainBinding
import com.tit.nimonsapp.ui.common.NetworkDisconnectedDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sessionRepository: SessionRepository
    private lateinit var networkMonitor: NetworkMonitor
    private var isOffline: Boolean = false
    private var previousDestinationId: Int? = null

    private val ignoreNetworkPopupDestinations =
        setOf(
            R.id.splashFragment,
        )

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

    private fun observeNetworkState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collectLatest { online ->
                    val wasOffline = isOffline
                    isOffline = !online

                    if (!online && !wasOffline) {
                        showDisconnectedPopup()
                    }

                    if (online) {
                        dismissDisconnectedPopup()
                    }
                }
            }
        }
    }

    private fun showDisconnectedPopup() {
        val existing =
            supportFragmentManager.findFragmentByTag(NetworkDisconnectedDialogFragment.TAG)

        if (existing == null) {
            NetworkDisconnectedDialogFragment()
                .show(supportFragmentManager, NetworkDisconnectedDialogFragment.TAG)
        }
    }

    private fun dismissDisconnectedPopup() {
        val existing =
            supportFragmentManager.findFragmentByTag(NetworkDisconnectedDialogFragment.TAG)
                as? DialogFragment

        existing?.dismissAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        Api.initialize(this)
        sessionRepository = SessionRepository(this)
        networkMonitor = NetworkMonitor(applicationContext)

        val client =
            OkHttpClient
                .Builder()
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

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            val isBottomNav = destination.id in bottomNavDestinations
            val wasBottomNav = previousDestinationId in bottomNavDestinations

            when {
                isBottomNav && wasBottomNav -> {
                }
                isBottomNav && !wasBottomNav -> {
                    controller.currentBackStackEntry?.lifecycle?.addObserver(
                        LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                binding.bottomNav.visibility = View.VISIBLE
                                binding.bottomNavDivider.visibility = View.VISIBLE
                            }
                        },
                    )
                }
                else -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.bottomNavDivider.visibility = View.GONE
                }
            }

            previousDestinationId = destination.id

            if (destination.id !in ignoreNetworkPopupDestinations && isOffline) {
                showDisconnectedPopup()
            }
        }

        observeNetworkState()
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
