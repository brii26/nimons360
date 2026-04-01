package com.tit.nimonsapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val nim: String? = null,
    val email: String,
    val fullName: String,
)

@Serializable
data class FamilyMemberDto(
    val id: Int? = null,
    val fullName: String,
    val email: String,
    val joinedAt: String? = null,
)

@Serializable
data class MaskedFamilyMemberDto(
    val fullName: String,
    val email: String,
)

@Serializable
data class FamilyDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val isMember: Boolean? = null,
    val familyCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<FamilyMemberDto>? = null,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val expiresAt: String,
    val user: UserDto,
)

@Serializable
data class GetMeResponseDto(
    val id: Int,
    val nim: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class UpdateMeRequestDto(
    val fullName: String,
)

@Serializable
data class UpdateMeResponseDto(
    val id: Int,
    val nim: String,
    val email: String,
    val fullName: String,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class GetFamiliesResponseDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
)

@Serializable
data class GetMyFamiliesResponseDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val familyCode: String,
    val createdAt: String,
    val updatedAt: String,
    val members: List<FamilyMemberDto>,
)

@Serializable
data class GetDiscoverFamiliesResponseDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val createdAt: String,
    val members: List<MaskedFamilyMemberDto>,
)

@Serializable
data class GetFamilyDetailResponseDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val isMember: Boolean,
    val familyCode: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val members: List<MaskedFamilyMemberDto>,
)

@Serializable
data class CreateFamilyRequestDto(
    val name: String,
    val iconUrl: String,
)

@Serializable
data class CreateFamilyResponseDto(
    val id: Int,
    val name: String,
    val iconUrl: String,
    val isMember: Boolean,
    val familyCode: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val members: List<MaskedFamilyMemberDto>,
)

@Serializable
data class JoinFamilyRequestDto(
    val familyId: Int,
    val familyCode: String,
)

@Serializable
data class JoinFamilyResponseDto(
    val joined: Boolean,
)

@Serializable
data class LeaveFamilyRequestDto(
    val familyId: Int,
)

@Serializable
data class LeaveFamilyResponseDto(
    val left: Boolean,
)
