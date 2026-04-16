package com.tit.nimonsapp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class PingDto(
    val type: String = "ping",
    val payload: Map<String, String> = emptyMap(),
    val timestamp: String
)

@Serializable
data class PongDto(
    val type: String = "pong",
    val payload: Map<String, String> = emptyMap(),
    val timestamp: String
)


@Serializable
data class UpdatePresenceDto(
    val type: String = "update_presence",
    val payload: PresencePayload,
    val timestamp: String
)

@Serializable
data class PresencePayload(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class MemberPresenceUpdatedDto(
    val type: String = "member_presence_updated",
    val payload: MemberPresencePayload,
    val timestamp: String
)


@Serializable
data class MemberPresencePayload(
    val userId: Int,
    val email: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    val rotation: Double,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val internetStatus: String,
    val metadata: Map<String, String> = emptyMap()
)