package com.tit.nimonsapp.data.network

import android.content.Context
import android.content.Intent
import com.tit.nimonsapp.MainActivity
import com.tit.nimonsapp.data.repository.SessionRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
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

class AuthInterceptor(
    private val context: Context,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 409 || response.code == 401) {
            val sessionRepository = SessionRepository(context)
            runBlocking {
                sessionRepository.clearToken()
            }

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            context.startActivity(intent)
        }
        return response
    }
}

object Api {
    private const val BASE_URL = "https://mad.labpro.hmif.dev/"
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val okHttpClient by lazy {
        val builder =
            OkHttpClient
                .Builder()
                .addInterceptor(loggingInterceptor)

        appContext?.let {
            builder.addInterceptor(AuthInterceptor(it))
        }

        builder.build()
    }

    private val retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType()),
            ).build()
    }

    val auth: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val family: FamilyApi by lazy { retrofit.create(FamilyApi::class.java) }
}
