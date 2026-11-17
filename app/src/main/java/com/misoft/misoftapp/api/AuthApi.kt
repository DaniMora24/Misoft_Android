
package com.misoft.misoftapp.api

import com.google.gson.JsonObject
import com.misoft.misoftapp.models.LoginRequest
import com.misoft.misoftapp.models.LoginResponse
import com.misoft.misoftapp.models.RegisterRequest
import com.misoft.misoftapp.models.RegisterResponse
import com.misoft.misoftapp.models.RegistrarBiometriaRequest
import com.misoft.misoftapp.models.RegistrarBiometriaResponse
import com.misoft.misoftapp.models.SolicitarDesafioRequest
import com.misoft.misoftapp.models.DesafioBiometricoResponse
import com.misoft.misoftapp.models.AutenticarBiometriaRequest

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    // Inicio de sesión tradicional
    @POST("api/Auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Registro de usuarios
    @POST("api/Auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    // Registrar dispositivo biométrico
    @POST("api/Auth/biometria/registrar")
    fun registrarBiometria(
        @Body request: RegistrarBiometriaRequest
    ): Call<RegistrarBiometriaResponse>

    // Solicitar desafío biométrico
    @POST("api/Auth/biometria/desafio")
    fun solicitarDesafio(
        @Body request: SolicitarDesafioRequest
    ): Call<DesafioBiometricoResponse>

    // Autenticar mediante biometría
    @POST("api/Auth/biometria/autenticar")
    fun autenticarBiometria(
        @Body request: AutenticarBiometriaRequest
    ): Call<LoginResponse>

    // Enviar código de verificación al correo
    @POST("api/Auth/recuperar-password")
    fun recuperarPassword(
        @Body request: JsonObject
    ): Call<JsonObject>

    // Validar código y cambiar contraseña
    @POST("api/Auth/restablecer-password")
    fun restablecerPassword(
        @Body request: JsonObject
    ): Call<JsonObject>
}
