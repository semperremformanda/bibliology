package com.example.helloandroid.network


import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

data class UserInfo(
    @SerializedName("userId") val id: String,
    @SerializedName("id") val tid: Long? = null,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("email") val email: String,
    @SerializedName("cardType") val cardType: String?= null,
    @SerializedName("cardNumber") val cardNumber: String?= null,
    @SerializedName("bankAccount") val bank: String?= null,
    @SerializedName("address") val address: String,
    @SerializedName("phoneNumber") val phone: String,
    @SerializedName("birthDate") val birthdate: String,
)

data class LoginRequest(@SerializedName("userId") val userId: String, @SerializedName("password") val password: String)
data class LoginResponse(val message: String, val success: Boolean, val user: String)
data class BookInfo(
    @SerializedName("bookId") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String,
    @SerializedName("commentCount") val count: Int,
    @SerializedName("publisher") val publisher: String, // 추가
    @SerializedName("genre") val genre: String,
    @SerializedName("price") val price: Double,
    @SerializedName("imageUrl") val imageUrl: String,  // 추가
    val score: Double ?= 0.0
)

data class Coupon(
    val id: Long,
    val code: String,
    val discountAmount: Double,
    val discountPercent: Double,
    val expiryDate: String?, // DateTime(6) 타입을 String으로 처리
    @SerializedName("used") val isUsed: Boolean,
    val minOrderAmount: Double,
    val userId: Long
)
data class CouponRequest(
    @SerializedName("userId") val userId: Long,
    @SerializedName("discountPercent") val discountPercent: Double,
    @SerializedName("minOrderAmount") val minOrderAmount: Double,
    @SerializedName("expiryDate") val expiryDate: String?,
    @SerializedName("code") val code: String?
)


data class Order(
    @SerializedName("id") val id: Long,
    @SerializedName("orderItems") val orderItems: List<OrderItemDetails>?,  // 주문 상품 리스트
    @SerializedName("payment") val payment: Payment?,  // 결제 정보
    @SerializedName("delivery") val delivery: Delivery?,  // 배송 정보
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("coupon") val coupon: String?,  // 쿠폰 (nullable)
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("discountedAmount") val discountedAmount: Double,
    @SerializedName("address") val address: String
)

data class OrderItemDetails(
    @SerializedName("bookId") val bookId: Long,
    @SerializedName("bookTitle") val title: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("price") val price: Long,
)

data class OrderRequest(
    @SerializedName("userId") val userId: Long,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("couponCode") val coupon : String?,
    @SerializedName("items") val items: List<OrderItemRequest>,
    @SerializedName("address") val address: String
)
data class OrderItemRequest(
    @SerializedName("bookId") val bookId: Long,
    @SerializedName("quantity") val quantity: Int
)

data class Payment(
    @SerializedName("id") val id: Long,
    @SerializedName("paymentMethod") val paymentMethod: String?,
    @SerializedName("paymentDate") val paymentDate: String?,
    @SerializedName("paid") val paid: Boolean
)

data class Delivery(
    @SerializedName("id") val id: Long,
    @SerializedName("address") val address: String,
    @SerializedName("trackingNumber") val trackingNumber: String?,  // `null` 가능
    @SerializedName("deliveryStatus") val deliveryStatus: String
)

data class Comment(
    @SerializedName("commentId") val id: Long,
    @SerializedName("book") val book: BookInfo,  // ✅ book은 객체로 처리
    @SerializedName("user") val user: UserInfo,  // ✅ user을 `User` 객체로 변경
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String
)

data class Cart(
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: UserInfo,
    @SerializedName("items") val items: List<CartItem>
)

data class CartItem(
    @SerializedName("id") val id: Long,
    @SerializedName("book") val book: BookInfo,
    @SerializedName("quantity") val quantity: Int
)



data class SearchHistory(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("author") val author: String?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("searchedAt") val searchedAt: String // LocalDateTime → String 변환
)

data class CheckIdResponse(
    val message: String,
    val available: Boolean? // null일 수 있음 (400일 때)
)

data class RatingResponse(
    val id: Long,
    val userId: Long,
    val bookId: Long,
    val score: Int
)


interface ApiService {
    @POST("/ratings")
    fun rateBook(
        @Query("userId") userId: Long,
        @Query("bookId") bookId: Long,
        @Query("score") score: Int
    ): Call<RatingResponse> // 응답을 받을 데이터 모델 (아래에서 정의)

    // ✅ 특정 책의 평균 평점 가져오기
    @GET("/ratings/average")
    fun getAverageRating(
        @Query("bookId") bookId: Long
    ): Call<Double>








    @DELETE("/api/search-history/{userId}")
    suspend fun deleteAllSearchHistory(
        @Path("userId") userId: Long
    ): Response<String>

    // ✅ 특정 키워드 검색 기록 삭제
    @DELETE("/api/search-history/{userId}/keyword")
    suspend fun deleteSearchHistoryByKeyword(
        @Path("userId") userId: Long,
        @Query("keyword") keyword: String
    ): Response<String>


        // ✅ 장바구니 개별 항목 삭제
        @DELETE("/api/cart/remove/{bookId}")
        suspend fun removeCartItem(
            @Path("bookId") bookId: Long, // ✅ `@Path` 먼저
            @Query("userId") userId: Long // ✅ `@Query` 뒤에 배치
        ): Response<Cart>


    @GET("/api/check-id")
    fun checkDuplicateUserId(
        @Query("userId") userId: String
    ): Call<CheckIdResponse> // Call로 선언 (enqueue에서 사용)


    // ✅ 장바구니 전체 비우기
        @DELETE("/api/cart/clear")
        fun clearCart(@Query("userId") userId: Long): Call<Cart>

        // ✅ 장바구니 수량 변경
        @PUT("/api/cart/update/{bookId}")
        fun updateCartItemQuantity(
            @Query("userId") userId: Long,
            @Path("bookId") bookId: Long,
            @Query("quantity") quantity: Int
        ): Call<Cart>

        // ✅ 장바구니 조회
        @GET("/api/cart/{userId}")
        fun getCart(@Path("userId") userId: Long): Call<Cart>



    // ✅ 장바구니 도서 추가
        @POST("/api/cart/add")
        fun addToCart(
            @Query("userId") userId: Long,
            @Query("bookId") bookId: Long,
            @Query("quantity") quantity: Int
        ): Call<Cart>



        @POST("/payments/refund_process")
    fun processRefund(
        @Query("orderId") orderId: Long,
        @Query("userId") userId: Long
    ): Call<Payment>




    // 검색 API
    @GET("api/books/search")
    fun searchBooks(
        @Query("userId") userId: Long? = null,
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
        @Query("publisher") publisher: String? = null,
        @Query("genre") genre: String? = null
    ): Call<List<BookInfo>>

    // 특정 유저의 검색 기록 조회
    @GET("api/books/search/history")
    fun getUserSearchHistory(@Query("userId") userId: Long): Call<List<SearchHistory>>





        // 📌 특정 책의 전체 댓글 조회 API
        @GET("/api/comments/book/{bookId}")
        fun getCommentsByBook(@Path("bookId") bookId: Long): Call<List<Comment>>

    // 특정 사용자의 댓글 조회
    @GET("/api/comments/user/{userId}")
    fun getUserComments(@Path("userId") userId: Long): Call<List<Comment>>



        // ✅ 댓글 추가 API (백엔드에 맞는 URL 수정)
        @POST("/api/comments/{bookId}")
        fun addComment(
            @Path("bookId") bookId: Long,  // URL 경로에 bookId 포함
            @Query("userId") userId: Long,  // `@Query`로 전달
            @Query("content") content: String
        ): Call<Comment>


        @PUT("/api/comments/{commentId}")
        fun updateComment(
            @Path("commentId") commentId: Long,
            @Query("newContent") newContent: String
        ): Call<Comment>

        // 댓글 삭제
        @DELETE("/api/comments/{commentId}")
        fun deleteComment(
            @Path("commentId") commentId: Long,
            @Query("userId") userId: Long  // ✅ userId 추가
        ): Call<Void>




    @GET("/orders/user/{userId}")
    fun getUserOrders(@Path("userId") userId: Long): Call<List<Order>>



    @GET("/coupons/user/{userId}")
    fun getUserCoupons(@Path("userId") userId: Long): Call<List<Coupon>>

    @POST("/coupons/create")
    fun createCoupon(@Body couponRequest: CouponRequest): Call<Coupon>


    @POST("/orders/create")
    fun placeOrder(@Query("userId") userId: Long, @Body orderRequest: OrderRequest): Call<Order>



    @POST("/payments/pay_process")
    @FormUrlEncoded
    fun processPayment(
        @Field("orderId") orderId: Long,
        @Field("method") method: String,   // 결제 방식 (카드, 계좌이체 등)
        @Field("userId") userId: Long,
        @Field("couponCode") couponCode: String? // 선택적 (쿠폰 적용 시)
    ): Call<Payment>





    @POST("api/register")
    fun registerUser(@Body user: UserInfo): Call<UserInfo>

    @POST("api/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/infofind")
    fun infofind(@Query("userId") userId: String): Call<UserInfo>


    @DELETE("member/MyPage/{userId}")
    fun deleteUser(
        @Path("userId") userId: String,
        @Query("password") password: String
    ): Call<Map<String, String>>

    @PUT("member/MyPage/{userId}")
    fun updateMyPage(
        @Path("userId") userId: String,
        @Query("password") password: String,
        @Body updatedUser: UserInfo
    ): Call<UserInfo>

    @POST("api/password_such")
    fun sendVerificationCode(@Query("email") email: String): Call<Map<String, String>>

    @POST("api/verify-code")
    fun verifyCode(
        @Query("email") email: String,
        @Query("authenticationCode") authenticationCode: String
    ): Call<Map<String, String>>


    @GET("api/books")
    fun getAllBooks(): Call<List<BookInfo>>


        // 특정 사용자의 관심 장르 조회
    @GET("member/MyPage/{userId}/interests")
        fun getUserInterests(@Path("userId") userId: Long): Call<List<String>>

        // 관심 장르 추가
        @POST("member/MyPage/{userId}/add_interests")
        fun addUserInterest(
            @Path("userId") userId: Long,
            @Query("genre") genre: String
        ): Call<Void>

        // 관심 장르 삭제
        @DELETE("member/MyPage/{userId}/delete_interests")
        fun deleteUserInterest(
            @Path("userId") userId: Long,
            @Query("genre") genre: String
        ): Call<Void>

}
