package ru.netology.laliapp.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.laliapp.api.UserApiService
import ru.netology.laliapp.dto.PhotoUpload
import ru.netology.laliapp.dto.Token
import ru.netology.laliapp.errors.ApiError
import ru.netology.laliapp.model.PhotoModel
import ru.netology.laliapp.model.StateModel
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userApiService: UserApiService,
) : ViewModel() {

    val data = MutableLiveData<Token>()

    private val noPhoto = PhotoModel()

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _dataState = MutableLiveData<StateModel>()
    val dataState: LiveData<StateModel>
        get() = _dataState

    fun registrationUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            _dataState.postValue(StateModel(loading = true))
            try {
                val response = userApiService.registrationUser(
                    login.toRequestBody("text/plain".toMediaType()),
                    password.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    photo.value?.uri?.toFile()?.let {
                        val upload = PhotoUpload(it)

                        MultipartBody.Part.createFormData(
                            "file",
                            upload.file.name,
                            upload.file.asRequestBody()
                        )
                    }
                )
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                data.value = Token(body.id, body.token)
                _dataState.postValue(StateModel())
            } catch (e: IOException) {
                _dataState.postValue(StateModel(error = true))
            } catch (e: Exception) {
                throw UnknownError()
            }
        }
        _photo.value = noPhoto
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }
}