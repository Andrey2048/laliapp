package ru.netology.laliapp.activity


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.laliapp.R
import ru.netology.laliapp.auth.AppAuth
import ru.netology.laliapp.databinding.ActivityAppBinding
import ru.netology.laliapp.viewmodel.AuthViewModel
import ru.netology.laliapp.viewmodel.UserViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailabilityLight

    private val authViewModel by viewModels<AuthViewModel>()

    private val userViewModel by viewModels<UserViewModel>()

    private lateinit var binding: ActivityAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.itemIconTintList = null

        val navController = findNavController(R.id.nav_host_fragment_activity_app)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_feed,
                R.id.nav_users,
                R.id.nav_events,
                R.id.nav_profile,
                -> {
                    navView.visibility = View.VISIBLE
                }
                else -> {
                    navView.visibility = View.GONE
                }
            }
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_feed,
                R.id.nav_users,
                R.id.nav_events,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)

        authViewModel.data.observe(this) { auth ->
            invalidateOptionsMenu()
            if (auth.id != 0L) userViewModel.getUserById(auth.id)

            navView.menu.findItem(R.id.nav_profile).setOnMenuItemClickListener {
                if (!authViewModel.authorized) {
                    findNavController(R.id.nav_host_fragment_activity_app)
                        .navigate(R.id.nav_sign_in_fragment)
                    true
                } else {
                    userViewModel.getUserById(auth.id)
                    val bundle = Bundle().apply {
                        userViewModel.user.value?.id?.let {
                            putLong("id", it)
                        }
                        putString("avatar", userViewModel.user.value?.avatar)
                        putString("name", userViewModel.user.value?.name)
                    }

                    findNavController(R.id.nav_host_fragment_activity_app).popBackStack()

                    findNavController(R.id.nav_host_fragment_activity_app)
                        .navigate(R.id.nav_profile, bundle)
                    true
                }
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.let {
            it.setGroupVisible(R.id.unauthentificated, !authViewModel.authorized)
            it.setGroupVisible(R.id.authentificated, authViewModel.authorized)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment_activity_app).navigateUp()
            }
            R.id.sign_in -> {
                findNavController(R.id.nav_host_fragment_activity_app)
                    .navigate(R.id.nav_sign_in_fragment)
                true
            }
            R.id.sign_up -> {
                findNavController(R.id.nav_host_fragment_activity_app)
                    .navigate(R.id.nav_sign_up_fragment)
                true
            }
            R.id.sign_out -> {
                appAuth.removeAuth()
                findNavController(R.id.nav_host_fragment_activity_app)
                    .navigate(R.id.nav_feed)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

}