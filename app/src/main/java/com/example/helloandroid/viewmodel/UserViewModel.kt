package com.example.helloandroid.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.helloandroid.network.ApiService
import com.example.helloandroid.network.LoginRequest
import com.example.helloandroid.network.LoginResponse
import com.example.helloandroid.network.RetrofitClient
import com.example.helloandroid.network.UserInfo
import androidx.compose.runtime.State

class UserViewModel : ViewModel() {
    var loggedInUser: UserInfo? = null

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    // 비밀번호 업데이트 함수
    fun setPassword(newPassword: String) {
        _password.value = newPassword
    }



    fun register(user: UserInfo, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.registerUser(user).enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                onResult(response.isSuccessful)
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Log.e("API_ERROR", "회원가입 실패: ${t.message}")
                onResult(false)
            }
        })
    }

    fun login(userId: String, password: String, onResult: (Boolean) -> Unit) {
        val request = LoginRequest(userId, password)
        RetrofitClient.apiService.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    loggedInUser = response.body()?.user
                    onResult(true)
                } else {
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("API_ERROR", "로그인 실패: ${t.message}")
                onResult(false)
            }
        })
    }

    fun getAllUsers(onResult: (List<UserInfo>?) -> Unit) {
        RetrofitClient.apiService.getAllUsers().enqueue(object : Callback<List<UserInfo>> {
            override fun onResponse(call: Call<List<UserInfo>>, response: Response<List<UserInfo>>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<List<UserInfo>>, t: Throwable) {
                Log.e("API_ERROR", "사용자 목록 가져오기 실패: ${t.message}")
                onResult(null)
            }
        })
    }

    fun findUserId(email: String, onResult: (String?) -> Unit) {
        RetrofitClient.apiService.findUserId(email).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("API_ERROR", "아이디 찾기 실패: ${t.message}")
                onResult(null)
            }
        })
    }

    fun findPassword(email: String, onResult: (String?) -> Unit) {
        RetrofitClient.apiService.findPassword(email).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("API_ERROR", "비밀번호 찾기 실패: ${t.message}")
                onResult(null)
            }
        })
    }


}
