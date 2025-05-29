package com.example.helloandroid.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.helloandroid.network.LoginRequest
import com.example.helloandroid.network.LoginResponse
import com.example.helloandroid.network.RetrofitClient
import com.example.helloandroid.network.UserInfo
import com.example.helloandroid.network.BookInfo as NetworkBookInfo // 🎯 핵심: as 키워드 사용
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.example.helloandroid.network.Cart
import com.example.helloandroid.network.CartItem
import com.example.helloandroid.network.CheckIdResponse
import com.example.helloandroid.network.Comment
import com.example.helloandroid.network.Coupon
import com.example.helloandroid.network.CouponRequest
import com.example.helloandroid.network.Order
import com.example.helloandroid.network.OrderItemRequest
import com.example.helloandroid.network.OrderRequest
import com.example.helloandroid.network.Payment
import com.example.helloandroid.network.RatingResponse
import com.example.helloandroid.network.SearchHistory
import com.google.gson.Gson
import kotlinx.coroutines.launch


class UserViewModel : ViewModel() {
    var loggedInUser: UserInfo? = null
    private val _bookList = mutableStateOf<List<NetworkBookInfo>>(emptyList())
    val bookList: State<List<NetworkBookInfo>> = _bookList
    private val _userCoupons = mutableStateOf<List<Coupon>>(emptyList())
    val userCoupons: State<List<Coupon>> = _userCoupons
    private val _userInterests = mutableStateOf<List<String>>(emptyList())
    val userInterests: State<List<String>> = _userInterests

    private val _userOrders = mutableStateOf<List<Order>>(emptyList())
    val userOrders: State<List<Order>> = _userOrders
    private val _userComments = mutableStateOf<List<Comment>>(emptyList())
    val userComments: State<List<Comment>> = _userComments
    private val _bookComments = mutableStateOf<List<Comment>>(emptyList())
    val bookComments: State<List<Comment>> = _bookComments
    private val _userCart = mutableStateOf<List<CartItem>>(emptyList())  // ✅ 변경
    val userCart: State<List<CartItem>> = _userCart

    private val _searchHistory = mutableStateOf<List<SearchHistory>>(emptyList()) // 검색 기록 저장
    val searchHistory: State<List<SearchHistory>> = _searchHistory

    private val _bookRating = mutableStateOf<Double?>(null)
    // ✅ 책에 평점 남기기
    fun rateBook(userId: Long, bookId: Long, score: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            RetrofitClient.apiService.rateBook(userId, bookId, score)
                .enqueue(object : Callback<RatingResponse> {
                    override fun onResponse(call: Call<RatingResponse>, response: Response<RatingResponse>) {
                        if (response.isSuccessful) {
                            Log.d("checkDDD", "평점 등록 성공: ${response.body()}")
                            onResult(true)
                        } else {
                            Log.e("checkDDD", "평점 등록 실패: ${response.errorBody()?.string()}")
                            onResult(false)
                        }
                    }

                    override fun onFailure(call: Call<RatingResponse>, t: Throwable) {
                        Log.e("checkDDD", "평점 등록 네트워크 오류: ${t.message}")
                        onResult(false)
                    }
                })
        }
    }
    fun fetchAverageRating(bookId: Long, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            RetrofitClient.apiService.getAverageRating(bookId)
                .enqueue(object : Callback<Double> {
                    override fun onResponse(call: Call<Double>, response: Response<Double>) {
                        if (response.isSuccessful) {
                            val newScore = response.body() ?: 0.0
                            _bookRating.value = newScore
                            Log.d("checkDDD", "평균 평점 조회 성공: $newScore")

                            // ✅ bookList에서 해당 책의 score 업데이트
                            _bookList.value = _bookList.value.map { book ->
                                if (book.id == bookId) book.copy(score = newScore) else book
                            }
                            onResult(newScore)
                        } else {
                            Log.e("checkDDD", "평균 평점 조회 실패: ${response.errorBody()?.string()}")
                            onResult(0.0)
                        }
                    }

                    override fun onFailure(call: Call<Double>, t: Throwable) {
                        Log.e("checkDDD", "평균 평점 네트워크 오류: ${t.message}")
                    }
                })
        }
    }


    fun checkDuplicateUserId(userId: String, onResult: (Boolean, String?) -> Unit) {
        RetrofitClient.apiService.checkDuplicateUserId(userId)
            .enqueue(object : Callback<CheckIdResponse> {
                override fun onResponse(call: Call<CheckIdResponse>, response: Response<CheckIdResponse>) {
                    if (response.isSuccessful && response.body()?.available == true) {
                        onResult(true, response.body()?.message)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val msg = response.body()?.message ?: errorBody ?: "이미 사용중인 아이디입니다."
                        onResult(false, msg)
                    }
                }

                override fun onFailure(call: Call<CheckIdResponse>, t: Throwable) {
                    onResult(false, "네트워크 오류: ${t.message}")
                }
            })
    }

    fun fetchBookComments(bookId: Long) {
        RetrofitClient.apiService.getCommentsByBook(bookId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    _bookComments.value = response.body() ?: emptyList()
                    Log.d("checkDDD", "책 ID: $bookId 댓글 불러오기 성공 -> ${_bookComments.value.size}개")
                } else {
                    Log.e("checkDDD", "책 ID: $bookId 댓글 불러오기 실패 -> ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                Log.e("checkDDD", "책 ID: $bookId 댓글 요청 실패 -> ${t.message}")
            }
        })
    }
    fun fetchUserOrders(userId: Long) {
        RetrofitClient.apiService.getUserOrders(userId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful) {
                    _userOrders.value = response.body() ?: emptyList()
                    Log.d("checkDDD", "주문 내역 불러오기 성공: ${_userOrders.value}")
                } else {
                    Log.e("checkDDD", "주문 내역 불러오기 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
            }
        })
    }
    fun processRefund(orderId: Long, userId: Long, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.processRefund(orderId, userId).enqueue(object : Callback<Payment> {
            override fun onResponse(call: Call<Payment>, response: Response<Payment>) {
                if (response.isSuccessful) {
                    Log.d("checkDDD", "환불 성공: ${response.body()?.id}")
                    onResult(true)
                } else {
                    Log.e("checkDDD", "환불 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Payment>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                onResult(false)
            }
        })
    }

    fun register(user: UserInfo, onResult: (Boolean,String) -> Unit) {
        RetrofitClient.apiService.registerUser(user).enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                val errorMessage = response.errorBody()?.string()
                if(response.isSuccessful){
                onResult(response.isSuccessful,errorMessage?:"")}
                else{

                    Log.e("checkDDD", "회원가입 안됨: $errorMessage")
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Log.e("checkDDD", "회원가입 실패: ${t.message}")
                onResult(false,"통신이 원활하지 않습니다.")
            }
        })
    }
    fun login(userId: String, password: String, onResult: (Boolean,String) -> Unit) {
        val request = LoginRequest(userId, password)
        RetrofitClient.apiService.loginUser(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val errorMessage = response.errorBody()?.string()
                if (response.isSuccessful && response.body()?.success == true) {
                    onResult(true,errorMessage?:"")
                    Log.d("checkDDD","로그인 성공"+"${errorMessage}")
                } else {
                    onResult(false,errorMessage?:"")
                    Log.d("checkDDD","로그인 실패!"+"${errorMessage}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("checkDDD", "로그인 실패: ${t.message}")
                onResult(false,"통신이 원활하지 않습니다.")
            } }) }
    fun infofind(id: String, onResult: (UserInfo?) -> Unit) {
        RetrofitClient.apiService.infofind(id).enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    Log.d("checkDDD", "불러오기 성공: ${userInfo?.id}")
                    fetchUserSearchHistory(userInfo!!.tid!!)
                    onResult(userInfo)
                } else {
                    val errorMessage = response.errorBody()?.string()
                    Log.e("checkDDD", "불러오기 실패: $errorMessage")
                    onResult(null) // 실패 시 null 반환
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                onResult(null) // 네트워크 오류 발생 시 null 반환
            } }) }

    fun placeOrder(userId: Long, bookId: Long,Price:Double,Coupon:String?,quantity: Int, onResult: (Boolean, Long?) -> Unit) {
        var orderRequest  =OrderRequest(items=listOf(OrderItemRequest(bookId, quantity)), userId = userId,
            address = loggedInUser?.address ?: "", totalPrice=Price, coupon = Coupon)

        RetrofitClient.apiService.placeOrder(userId, orderRequest).enqueue(object : Callback<Order> {
            override fun onResponse(call: Call<Order>, response: Response<Order>) {
                if (response.isSuccessful) {
                    val order2 = response.body()
                    val gson = Gson()
                    val jsonResponse = gson.toJson(order2)
                    Log.e("checkDDD", "서버 응답 JSON: $jsonResponse")
                    val order = response.body()
                    Log.d("checkDDD", "주문 생성 성공: ${order?.id}")
                    Log.e("checkDDD", "${response.body()?.toString()}")
                    onResult(true, order?.id)
                } else {
                    Log.e("checkDDD", "주문 생성 실패: ${response.errorBody()?.string()}")
                    onResult(false, null)
                }
            }

            override fun onFailure(call: Call<Order>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                onResult(false, null)
            }
        })
    }
    fun processPayment(orderId: Long, method: String, userId: Long, couponCode: String?, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.processPayment(orderId, method, userId, couponCode)
            .enqueue(object : Callback<Payment> {
                override fun onResponse(call: Call<Payment>, response: Response<Payment>) {
                    if (response.isSuccessful) {
                        Log.d("checkDDD", "결제 성공: ${response.body()?.id}")
                        onResult(true)
                    } else {
                        Log.e("checkDDD", "결제 실패: ${response.errorBody()?.string()}")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<Payment>, t: Throwable) {
                    Log.e("checkDDD", "결제 네트워크 오류: ${t.message}")
                    onResult(false)
                }
            })
    }


    fun updateUserInfo(updatedUser: UserInfo, currentPassword: String, onResult: (Boolean, String?) -> Unit) {
        RetrofitClient.apiService.updateMyPage(updatedUser.id, currentPassword, updatedUser)
            .enqueue(object : Callback<UserInfo> {
                override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                    if (response.isSuccessful) {
                        loggedInUser = response.body()

                        Log.d("checkDDD","update success")
                        onResult(true, null)
                    } else {
                        onResult(false, response.errorBody()?.string() ?: "업데이트 실패")
                        Log.d("checkDDD","update fail")
                    }
                }

                override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                    onResult(false, t.message+"!")
                    Log.d("checkDDD","connect fail")
                }
            })
    }
    fun deleteUser(userId: String, password: String, onResult: (Boolean, String?) -> Unit) {
        RetrofitClient.apiService.deleteUser(userId, password)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        Log.d("checkDDD","탈퇴 성공")
                        onResult(true, response.body()?.get("message"))
                    } else {
                        onResult(false, response.errorBody()?.string() ?: "탈퇴 실패")
                        Log.d("checkDDD","탈퇴 실패")
                    }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    onResult(false, t.message ?: "네트워크 오류")
                }
            })
    }
    fun sendVerificationCode(email: String, onResult: (Boolean, String?) -> Unit) {
        RetrofitClient.apiService.sendVerificationCode(email).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    val message = responseData?.get("message")
                    Log.d("checkDDD", "이메일 발송 성공: $message")
                    onResult(true, null)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "서버 오류 발생"
                    Log.e("checkDDD", "이메일 발송 실패: $errorMessage")
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                onResult(false, "네트워크 오류 발생")
            }
        })
    }
    fun verifyCode(email: String, code: String, onResult: (Boolean, String?) -> Unit) {
        RetrofitClient.apiService.verifyCode(email, code).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    val password = responseData?.get("password") ?: "비밀번호를 찾을 수 없습니다."
                    onResult(true, password)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "인증 코드가 틀렸습니다."
                    Log.e("checkDDD", "인증 코드 검증 실패: $errorMessage")
                    onResult(false, errorMessage)
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                onResult(false, "네트워크 오류 발생: ${t.message}")
            }
        })
    }
    fun fetchAllBooks() {
        RetrofitClient.apiService.getAllBooks().enqueue(object : Callback<List<NetworkBookInfo>> {
            override fun onResponse(call: Call<List<NetworkBookInfo>>, response: Response<List<NetworkBookInfo>>) {
                if (response.isSuccessful) {
                    _bookList.value = response.body() ?: emptyList()
                } else {
                    Log.e("checkDDD", "책 목록 가져오기 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<NetworkBookInfo>>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
            }
        })
    }
    fun fetchUserCoupons(userId: Long) {
        RetrofitClient.apiService.getUserCoupons(userId).enqueue(object : Callback<List<Coupon>> {
            override fun onResponse(call: Call<List<Coupon>>, response: Response<List<Coupon>>) {
                if (response.isSuccessful) {
                    val coupons = response.body() ?: emptyList()
                    _userCoupons.value = coupons // 📌 `MutableState` 업데이트
                    Log.d("checkDDD", "서버 응답: $coupons")
                } else {
                    Log.e("checkDDD", "쿠폰 불러오기 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Coupon>>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
            }
        })
    }
    fun issueWelcomeCoupon(userId: Long) {
        val couponRequest = CouponRequest(
            userId = userId,
            code ="welcome",
            discountPercent = 30.0, // 30% 할인 쿠폰
            minOrderAmount = 0.0, // 최소 주문 금액 00원
            expiryDate = "2025-12-31 23:59:59" // 예제 만료일 설정
        )

        RetrofitClient.apiService.createCoupon(couponRequest).enqueue(object : Callback<Coupon> {
            override fun onResponse(call: Call<Coupon>, response: Response<Coupon>) {
                if (response.isSuccessful) {
                    Log.d("checkDDD", "회원가입 쿠폰 발급 성공")
                } else {
                    Log.e("checkDDD", "쿠폰 발급 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Coupon>, t: Throwable) {
                Log.e("checkDDD", "네트워크 오류로 쿠폰 발급 실패: ${t.message}")
            }
        })
    }


    fun fetchUserInterests(userId: Long) {
        RetrofitClient.apiService.getUserInterests(userId)
            .enqueue(object : Callback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    if (response.isSuccessful) {
                        _userInterests.value = response.body() ?: emptyList()
                        Log.d("checkDDD", "관심 장르 조회 성공: ${_userInterests.value}")
                    } else {
                        Log.e("checkDDD", "관심 장르 조회 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    Log.e("checkDDD", "네트워크 오류: ${t.message}")
                }
            })
    }
    fun addUserInterest(userId: Long, genre: String) {
        RetrofitClient.apiService.addUserInterest(userId, genre)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("checkDDD", "관심 장르 추가 성공: $genre")
                        fetchUserInterests(userId)  // 추가 후 관심 장르 목록 새로고침
                    } else {
                        Log.e("checkDDD", "관심 장르 추가 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("checkDDD", "네트워크 오류: ${t.message}")
                }
            })
    }
    fun deleteUserInterest(userId: Long, genre: String) {
        RetrofitClient.apiService.deleteUserInterest(userId, genre)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("checkDDD", "관심 장르 삭제 성공: $genre")
                        fetchUserInterests(userId)  // 삭제 후 관심 장르 목록 새로고침
                    } else {
                        Log.e("checkDDD", "관심 장르 삭제 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("checkDDD", "네트워크 오류: ${t.message}")
                }
            })
    }

    fun addToCart(userId: Long, bookId: Long, quantity: Int, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.addToCart(userId, bookId, quantity).enqueue(object : Callback<Cart> {
            override fun onResponse(call: Call<Cart>, response: Response<Cart>) {
                if (response.isSuccessful) {
                    response.body()?.let { cart ->
                        _userCart.value = cart.items  // ✅ `List<CartItem>`을 저장
                    }
                    Log.d("checkDDD", "장바구니 추가 성공: ${_userCart.value}")

                    val order2 = response.body()
                    val gson = Gson()
                    val jsonResponse = gson.toJson(order2)
                    Log.e("checkDDD", "주문 JSON: $jsonResponse")
                    Log.e("checkDDD", "${response.body()?.toString()}")

                    onResult(true)
                } else {
                    Log.e("checkDDD", "장바구니 추가 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                Log.e("checkDDD", "장바구니 추가 네트워크 오류: ${t.message}")
                onResult(false)
            }
        })
    }
    fun fetchCart(userId: Long) {
        RetrofitClient.apiService.getCart(userId).enqueue(object : Callback<Cart> {
            override fun onResponse(call: Call<Cart>, response: Response<Cart>) {
                if (response.isSuccessful) {
                    response.body()?.let { cart ->
                        _userCart.value = cart.items  // ✅ `List<CartItem>`을 저장
                    }
                    Log.d("checkDDD", "장바구니 조회 성공: ${_userCart.value}")
                } else {
                    Log.e("checkDDD", "장바구니 조회 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                Log.e("checkDDD", "장바구니 조회 네트워크 오류: ${t.message}")
            }
        })
    }
    fun removeCartItem(userId: Long, cartItemId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.removeCartItem(cartItemId, userId) // ✅ 순서 변경
                if (response.isSuccessful) {
                    val updatedCart = response.body()
                    _userCart.value = updatedCart?.items ?: emptyList() // ✅ UI 즉시 업데이트
                    Log.d("checkDDD", "장바구니 개별 항목 삭제 성공: ${_userCart.value}")
                    onResult(true)
                } else {
                    Log.e("checkDDD", "장바구니 개별 항목 삭제 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("checkDDD", "장바구니 개별 항목 삭제 네트워크 오류: ${e.message}")
                onResult(false)
            }
        }
    }


    fun updateLocalCartAfterRemove(cartItemId: Long) {
        _userCart.value = _userCart.value.filterNot { it.id == cartItemId }
    }


    fun clearCart(userId: Long, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.clearCart(userId).enqueue(object : Callback<Cart> {
            override fun onResponse(call: Call<Cart>, response: Response<Cart>) {
                if (response.isSuccessful) {
                    response.body()?.let { cart ->
                        _userCart.value = cart.items  // ✅ `List<CartItem>`을 저장
                    }
                    Log.d("checkDDD", "장바구니 전체 비우기 성공: ${_userCart.value}")
                    onResult(true)
                } else {
                    Log.e("checkDDD", "장바구니 전체 비우기 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<Cart>, t: Throwable) {
                Log.e("checkDDD", "장바구니 전체 비우기 네트워크 오류: ${t.message}")
                onResult(false)
            }
        })
    }



    fun fetchUserComments(userId: Long) {
            RetrofitClient.apiService.getUserComments(userId).enqueue(object : Callback<List<Comment>> {
                override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                    if (response.isSuccessful) {
                        _userComments.value = response.body() ?: emptyList()
                        Log.d("checkDDD", "댓글 조회 성공: ${_userComments.value}")
                    } else {
                        Log.e("checkDDD", "댓글 조회 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                    Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                }
            })
        }
    fun addComment(bookId: Long, userId: Long, content: String, onResult: (Boolean) -> Unit) {
            RetrofitClient.apiService.addComment(bookId, userId, content)
                .enqueue(object : Callback<Comment> {
                    override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                        if (response.isSuccessful) {
                            Log.d("checkDDD", "댓글 추가 성공: ${response.body()?.id}")
                            fetchUserComments(userId) // 댓글 목록 업데이트
                            onResult(true)
                        } else {
                            Log.e("checkDDD", "댓글 추가 실패: ${response.errorBody()?.string()}")
                            onResult(false)
                        }
                    }

                    override fun onFailure(call: Call<Comment>, t: Throwable) {
                        Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                        onResult(false)
                    }
                })
        }
       /* fun updateComment(commentId: Long, newContent: String, onResult: (Boolean) -> Unit) {
            RetrofitClient.apiService.updateComment(commentId, newContent)
                .enqueue(object : Callback<Comment> {
                    override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                        if (response.isSuccessful) {
                            Log.d("checkDDD", "댓글 수정 성공: ${response.body()?.id}")
                            fetchUserComments(response.body()?.userId ?: 0) // 업데이트된 댓글 목록 불러오기
                            onResult(true)
                        } else {
                            Log.e("checkDDD", "댓글 수정 실패: ${response.errorBody()?.string()}")
                            onResult(false)
                        }
                    }

                    override fun onFailure(call: Call<Comment>, t: Throwable) {
                        Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                        onResult(false)
                    }
                })
        }*/
    fun deleteComment(commentId: Long, userId: Long, onResult: (Boolean) -> Unit) {
        RetrofitClient.apiService.deleteComment(commentId, userId) // ✅ userId 추가
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("checkDDD", "댓글 삭제 성공")
                        onResult(true)
                    } else {
                        Log.e("checkDDD", "댓글 삭제 실패: ${response.errorBody()?.string()}")
                        onResult(false)
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("checkDDD", "네트워크 오류 발생: ${t.message}")
                    onResult(false)
                }
            })
    }


    private val _searchResults = mutableStateOf<List<NetworkBookInfo>>(emptyList())  // 검색 결과 저장
        val searchResults: State<List<NetworkBookInfo>> = _searchResults




    fun deleteAllSearchHistory(userId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteAllSearchHistory(userId)
                if (response.isSuccessful) {
                    _searchHistory.value = emptyList() // ✅ UI 즉시 반영
                    Log.d("checkDDD", "전체 검색 기록 삭제 성공")
                    onResult(true)
                } else {
                    Log.e("checkDDD", "전체 검색 기록 삭제 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("checkDDD", "검색 기록 삭제 네트워크 오류 발생: ${e.message}")
                onResult(false)
            }
        }
    }

    fun deleteSearchHistoryByKeyword(userId: Long, keyword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteSearchHistoryByKeyword(userId, keyword)
                if (response.isSuccessful) {
                    // ✅ 삭제 후 최신 검색 기록 불러오기
                    Log.d("checkDDD", "검색 기록 키워드 삭제 성공")
                    onResult(true)
                } else {
                    Log.e("checkDDD", "검색 기록 키워드 삭제 실패: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("checkDDD", "검색 기록 키워드 삭제 네트워크 오류 발생: ${e.message}")
                onResult(false)
            }
        }
    }
    fun searchBooks(userId: Long?, title: String?, author: String?, publisher: String?, genre: String?) {
        RetrofitClient.apiService.searchBooks(userId, title, author, publisher, genre)
            .enqueue(object : Callback<List<NetworkBookInfo>> {
                override fun onResponse(call: Call<List<NetworkBookInfo>>, response: Response<List<NetworkBookInfo>>) {
                    if (response.isSuccessful) {
                        _searchResults.value = response.body() ?: emptyList()
                        Log.d("checkDDD", "검색 결과: ${_searchResults.value}")

                        // ✅ 검색이 성공하면 검색 기록 다시 불러오기
                        userId?.let {
                            fetchUserSearchHistory(it)
                        }
                    } else {
                        Log.e("checkDDD", "검색 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<NetworkBookInfo>>, t: Throwable) {
                    Log.e("checkDDD", "검색 네트워크 오류 발생: ${t.message}")
                }
            })
    }
        fun fetchUserSearchHistory(userId: Long) {
            RetrofitClient.apiService.getUserSearchHistory(userId)
                .enqueue(object : Callback<List<SearchHistory>> {
                    override fun onResponse(call: Call<List<SearchHistory>>, response: Response<List<SearchHistory>>) {
                        if (response.isSuccessful) {
                            _searchHistory.value = response.body() ?: emptyList()
                            Log.d("checkDDD", "검색 기록 조회 성공: ${_searchHistory.value}")
                        } else {
                            Log.e("checkDDD", "검색 기록 조회 실패: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<List<SearchHistory>>, t: Throwable) {
                        Log.e("checkDDD", "검색 기록 네트워크 오류 발생: ${t.message}")
                    }
                })
        }
    }



