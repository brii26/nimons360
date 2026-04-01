package com.tit.nimonsapp.data.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): ApiResponse<LoginResponseDto>

    @GET("api/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String,
    ): ApiResponse<GetMeResponseDto>

    @PATCH("api/me")
    suspend fun updateMe(
        @Header("Authorization") authorization: String,
        @Body request: UpdateMeRequestDto,
    ): ApiResponse<UpdateMeResponseDto>
}

interface FamilyApi {
    @GET("api/families")
    suspend fun getFamilies(
        @Header("Authorization") authorization: String,
    ): ApiResponse<List<GetFamiliesResponseDto>>

    @GET("api/me/families")
    suspend fun getMyFamilies(
        @Header("Authorization") authorization: String,
    ): ApiResponse<List<GetMyFamiliesResponseDto>>

    @GET("api/families/discover")
    suspend fun getDiscoverFamilies(
        @Header("Authorization") authorization: String,
    ): ApiResponse<List<GetDiscoverFamiliesResponseDto>>

    @GET("api/families/{familyId}")
    suspend fun getFamilyDetail(
        @Header("Authorization") authorization: String,
        @Path("familyId") familyId: Int,
    ): ApiResponse<GetFamilyDetailResponseDto>

    @POST("api/families")
    suspend fun createFamily(
        @Header("Authorization") authorization: String,
        @Body request: CreateFamilyRequestDto,
    ): ApiResponse<CreateFamilyResponseDto>

    @POST("api/families/join")
    suspend fun joinFamily(
        @Header("Authorization") authorization: String,
        @Body request: JoinFamilyRequestDto,
    ): ApiResponse<JoinFamilyResponseDto>

    @POST("api/families/leave")
    suspend fun leaveFamily(
        @Header("Authorization") authorization: String,
        @Body request: LeaveFamilyRequestDto,
    ): ApiResponse<LeaveFamilyResponseDto>
}

object Api {
    private const val BASE_URL = "https://mad.labpro.hmif.dev/"

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val okHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    private val retrofit =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            ).build()

    val auth: AuthApi = retrofit.create(AuthApi::class.java)
    val family: FamilyApi = retrofit.create(FamilyApi::class.java)
}
