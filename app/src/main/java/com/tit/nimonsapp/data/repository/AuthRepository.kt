package com.tit.nimonsapp.data.repository

import com.tit.nimonsapp.data.network.Api
import com.tit.nimonsapp.data.network.GetMeResponseDto
import com.tit.nimonsapp.data.network.LoginRequestDto
import com.tit.nimonsapp.data.network.LoginResponseDto
import com.tit.nimonsapp.data.network.UpdateMeRequestDto
import com.tit.nimonsapp.data.network.UpdateMeResponseDto

class AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): LoginResponseDto =
        Api.auth
            .login(
                LoginRequestDto(
                    email = email,
                    password = password,
                ),
            ).data

    suspend fun getMe(token: String): GetMeResponseDto =
        Api.auth
            .getMe(
                authorization = "Bearer $token",
            ).data

    suspend fun updateMe(
        token: String,
        fullName: String,
    ): UpdateMeResponseDto =
        Api.auth
            .updateMe(
                authorization = "Bearer $token",
                request =
                    UpdateMeRequestDto(
                        fullName = fullName,
                    ),
            ).data
}
