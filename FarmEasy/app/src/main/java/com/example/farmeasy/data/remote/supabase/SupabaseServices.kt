package com.example.farmeasy.data.remote.supabase

import com.example.farmeasy.domain.model.Farmer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseAuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@Serializable
data class SupabaseAuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    val user: SupabaseUserDto? = null
)

@Serializable
data class SupabaseUserDto(
    val id: String,
    val email: String? = null,
    @SerialName("user_metadata") val userMetadata: Map<String, String>? = null
)

class SupabaseAuthService(private val client: HttpClient) {

    suspend fun signUp(
        farmer: Farmer,
        password: String,
        supabaseUrl: String,
        anonKey: String
    ): Result<Farmer> {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            return Result.failure(IllegalStateException("Supabase URL or Anon Key not configured in App Settings."))
        }

        return try {
            val endpoint = "$supabaseUrl/auth/v1/signup"
            val email = if (farmer.email.isNotBlank()) farmer.email else "${farmer.phone}@farmeasy.app"
            val response: SupabaseAuthResponse = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                setBody(
                    SupabaseAuthRequest(
                        email = email,
                        password = password,
                        data = mapOf(
                            "name" to farmer.name,
                            "phone" to farmer.phone,
                            "state" to farmer.state,
                            "district" to farmer.district,
                            "language" to farmer.preferredLanguage
                        )
                    )
                )
            }.body()

            val uid = response.user?.id ?: farmer.id
            Result.success(farmer.copy(id = uid, isDemoUser = false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(
        emailOrPhone: String,
        password: String,
        supabaseUrl: String,
        anonKey: String
    ): Result<Farmer> {
        if (supabaseUrl.isBlank() || anonKey.isBlank()) {
            return Result.failure(IllegalStateException("Supabase URL or Anon Key not configured in App Settings."))
        }

        return try {
            val endpoint = "$supabaseUrl/auth/v1/token?grant_type=password"
            val email = if (emailOrPhone.contains("@")) emailOrPhone else "$emailOrPhone@farmeasy.app"
            val response: SupabaseAuthResponse = client.post(endpoint) {
                contentType(ContentType.Application.Json)
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                setBody(SupabaseAuthRequest(email = email, password = password))
            }.body()

            val uid = response.user?.id ?: "supabase_user_id"
            val metadata = response.user?.userMetadata ?: emptyMap()
            val farmer = Farmer(
                id = uid,
                name = metadata["name"] ?: "Verified Farmer",
                phone = metadata["phone"] ?: emailOrPhone,
                email = email,
                state = metadata["state"] ?: "Maharashtra",
                district = metadata["district"] ?: "Pune",
                preferredLanguage = metadata["language"] ?: "English",
                isDemoUser = false
            )
            Result.success(farmer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
