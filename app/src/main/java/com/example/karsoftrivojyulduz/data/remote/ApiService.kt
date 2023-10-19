package com.example.karsoftrivojyulduz.data.remote

import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderAndHistoryResponseData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesRequestData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.OrderImagesResponseData
import com.example.karsoftrivojyulduz.domain.model.ordersandhistories.SimpleOrderResponseData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInRequestData
import com.example.karsoftrivojyulduz.domain.model.signin.SignInResponseData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun signIn(@Body body: SignInRequestData): Response<SignInResponseData>

    @Multipart
    @POST("api/v1/measurer/details")
    suspend fun insertOrdersImages(
        @Part("order_id") orderId: RequestBody,
        @Part("description") description: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<OrderImagesResponseData>

    @GET("api/v1/measurer/orders")
    suspend fun getAllOrders(@Query("status") status: String): Response<OrderAndHistoryResponseData>

    @GET("api/v1/measurer/orders/{id}")
    suspend fun getSimpleOrder(@Path("id") orderId: Int): Response<SimpleOrderResponseData>
}