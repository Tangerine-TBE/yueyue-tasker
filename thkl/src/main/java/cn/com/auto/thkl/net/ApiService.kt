package cn.com.auto.thkl.net

import cn.com.auto.thkl.net.info.BaseRsp
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET ("/api/client/getClientInfo")
    suspend fun getClientInfo(@Query("osName") osName:String):BaseRsp

    @GET("/api/taskDevice/queryMaintainInfo")
    suspend fun queryMaintainInfo(@Header("access_token") token: String): BaseRsp

    @POST("/api/taskDevice/appExecutionFeedback")
     fun appExecutionFeedback(
        @Header("access_token") token: String,
        @Body files: MultipartBody
    ): Observable<BaseRsp>

    @GET("/api/taskDevice/queryAppTask")
    suspend fun queryAppTask(
        @Header("access_token") token: String,
        @Query("updateTask") query: Boolean
    ):BaseRsp

    @GET("/api/taskDevice/scriptDetail")
    suspend fun scriptDetail(
        @Header("access_token") token: String,
        @Query("scriptId") scriptId: String
    ):BaseRsp

    @FormUrlEncoded
    @POST("/api/taskDevice/scriptExecutionFeedback")
     suspend fun scriptExecutionFeedback(
        @Header("access_token") token: String?,
        @Field("result") result: String?,
        @Field("runRecordId") recordId: Int,
        @Field("status") status: Int,
        @Field("resultBase64") resultBase64:String,
        @Field("title") title:String
    ): BaseRsp

    @FormUrlEncoded
    @POST("/api/taskLogin/taskSignIn")
    suspend  fun taskSignIn(
        @Field("password") password: String?,
        @Field("username") username: String?,
        @Field("validationType") validationType: Int
    ): BaseRsp

    @FormUrlEncoded
    @POST("/api/taskDevice/deviceReport")
    suspend fun deviceReport(
        @Header("access_token") token: String?,
        @Field("deviceId") deviceId: Int,
        @Field("electricity") electricity: Int,
        @Field("queryExecute") queryExecute: Boolean,
        @Field("signal") signal: Int,
        @Field("state") state: Boolean
    ): BaseRsp

    @Streaming
    @GET
    suspend fun download(@Url url: String?): Response<ResponseBody>

}