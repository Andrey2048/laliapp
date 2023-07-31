package ru.netology.laliapp.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import ru.netology.laliapp.BuildConfig
import ru.netology.laliapp.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var auth: AppAuth

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }

}