package com.tit.nimonsapp.data.repository

import com.tit.nimonsapp.data.network.Api
import com.tit.nimonsapp.data.network.CreateFamilyRequestDto
import com.tit.nimonsapp.data.network.CreateFamilyResponseDto
import com.tit.nimonsapp.data.network.GetDiscoverFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetFamiliesResponseDto
import com.tit.nimonsapp.data.network.GetFamilyDetailResponseDto
import com.tit.nimonsapp.data.network.GetMyFamiliesResponseDto
import com.tit.nimonsapp.data.network.JoinFamilyRequestDto
import com.tit.nimonsapp.data.network.JoinFamilyResponseDto
import com.tit.nimonsapp.data.network.LeaveFamilyRequestDto
import com.tit.nimonsapp.data.network.LeaveFamilyResponseDto

class FamilyRepository {
    suspend fun getFamilies(token: String): List<GetFamiliesResponseDto> =
        Api.family
            .getFamilies(
                authorization = "Bearer $token",
            ).data

    suspend fun getMyFamilies(token: String): List<GetMyFamiliesResponseDto> =
        Api.family
            .getMyFamilies(
                authorization = "Bearer $token",
            ).data

    suspend fun getDiscoverFamilies(token: String): List<GetDiscoverFamiliesResponseDto> =
        Api.family
            .getDiscoverFamilies(
                authorization = "Bearer $token",
            ).data

    suspend fun getFamilyDetail(
        token: String,
        familyId: Int,
    ): GetFamilyDetailResponseDto =
        Api.family
            .getFamilyDetail(
                authorization = "Bearer $token",
                familyId = familyId,
            ).data

    suspend fun createFamily(
        token: String,
        name: String,
        iconUrl: String,
    ): CreateFamilyResponseDto =
        Api.family
            .createFamily(
                authorization = "Bearer $token",
                request =
                    CreateFamilyRequestDto(
                        name = name,
                        iconUrl = iconUrl,
                    ),
            ).data

    suspend fun joinFamily(
        token: String,
        familyId: Int,
        familyCode: String,
    ): JoinFamilyResponseDto =
        Api.family
            .joinFamily(
                authorization = "Bearer $token",
                request =
                    JoinFamilyRequestDto(
                        familyId = familyId,
                        familyCode = familyCode,
                    ),
            ).data

    suspend fun leaveFamily(
        token: String,
        familyId: Int,
    ): LeaveFamilyResponseDto =
        Api.family
            .leaveFamily(
                authorization = "Bearer $token",
                request =
                    LeaveFamilyRequestDto(
                        familyId = familyId,
                    ),
            ).data
}
