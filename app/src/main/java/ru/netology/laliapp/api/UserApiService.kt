package ru.netology.laliapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.laliapp.dto.Token
import ru.netology.laliapp.dto.User

interface UserApiService {

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>
    
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("password") pass: String,
    ): Response<Token>

    @Multipart
    @POST("users/registration")
    suspend fun registrationUser(
        @Part("login") login: RequestBody,
        @Part("password") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part?,
    ): Response<Token>
}