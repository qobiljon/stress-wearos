package io.github.qobiljon.stressapp.utils

import android.content.Context
import io.github.qobiljon.stressapp.R
import io.github.qobiljon.stressapp.core.api.ApiInterface
import io.github.qobiljon.stressapp.core.api.requests.SignInRequest
import io.github.qobiljon.stressapp.core.api.requests.SubmitOffBodyRequest
import io.github.qobiljon.stressapp.core.data.OffBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

object Api {
    private fun getApiInterface(context: Context): ApiInterface {
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(context.getString(R.string.api_base_url)).build().create(ApiInterface::class.java)
    }

    suspend fun signIn(context: Context, email: String, password: String): Boolean {
        return try {
            val result = getApiInterface(context).signIn(SignInRequest(email = email, password = password))
            val resultBody = result.body()
            if (result.errorBody() == null && result.isSuccessful && resultBody != null) {
                Storage.setAuthToken(context, authToken = resultBody.token)
                true
            } else false
        } catch (e: ConnectException) {
            false
        } catch (e: SocketTimeoutException) {
            false
        }
    }

    suspend fun submitOffBody(context: Context, token: String, offBody: OffBody): Boolean {
        return try {
            val result = getApiInterface(context).submitOffBodyData(
                token = "Token $token", SubmitOffBodyRequest(
                    timestamp = offBody.timestamp,
                    is_off_body = offBody.is_off_body,
                )
            )
            result.errorBody() == null && result.isSuccessful
        } catch (e: ConnectException) {
            false
        } catch (e: SocketTimeoutException) {
            false
        }
    }

    suspend fun submitAccFile(context: Context, token: String, file: File): Boolean {
        return try {
            val result = getApiInterface(context).submitAccData(
                token = "Token $token",
                file = MultipartBody.Part.createFormData("file", file.name, RequestBody.create(MediaType.parse("text/plain"), file)),
            )
            result.errorBody() == null && result.isSuccessful
        } catch (e: ConnectException) {
            false
        } catch (e: SocketTimeoutException) {
            false
        }
    }

    suspend fun submitPPGFile(context: Context, token: String, file: File): Boolean {
        return try {
            val result = getApiInterface(context).submitPPGData(
                token = "Token $token",
                file = MultipartBody.Part.createFormData("file", file.name, RequestBody.create(MediaType.parse("text/plain"), file)),
            )
            result.errorBody() == null && result.isSuccessful
        } catch (e: ConnectException) {
            false
        } catch (e: SocketTimeoutException) {
            false
        }
    }
}