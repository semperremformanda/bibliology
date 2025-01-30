package com.example.helloandroid.network


import com.example.helloandroid.BookInfo
import com.example.helloandroid.Comment
import com.example.helloandroid.Coupon
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

data class UserInfo(val id: String, val password: String, val name: String,
                    val age: String, val gender: String, val email: String,
                    val cardType:String, val cardNumber:String, val address:String,
                    val phone: String,
                    val birthdate: String, var purchasedBooks: MutableList<BookInfo> = mutableListOf(),
                    var cartBooks: MutableList<BookInfo> = mutableListOf(),
                    var comments: MutableList<Comment> = mutableListOf(),
                    var favorits: MutableList<String> = mutableListOf(),
                    var coupons: MutableMap<Int, Coupon> = mutableMapOf(),
                    var appreciated: MutableMap<String, Int> = mutableMapOf(),
                    var searchHistory: MutableList<String> = mutableListOf(), )
data class LoginRequest(val userId: String, val password: String)
data class LoginResponse(val message: String, val success: Boolean, val user: UserInfo?)

interface ApiService {
    @POST("api/register")
    fun registerUser(@Body user: UserInfo): Call<UserInfo>

    @POST("api/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("api/users")
    fun getAllUsers(): Call<List<UserInfo>>

    @POST("api/Id_such")
    fun findUserId(@Query("email") email: String): Call<String>

    @POST("api/password_such")
    fun findPassword(@Query("email") email: String): Call<String>

    @PUT("api/users/{id}")
    fun updateUserInfo(@Body user: UserInfo): Call<UserInfo>
}
