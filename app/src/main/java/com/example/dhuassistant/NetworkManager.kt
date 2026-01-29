package com.example.dhuassistant

import android.util.Log // 别忘了导入这个
import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.concurrent.TimeUnit

class NetworkManager {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val cookieStr = CookieManager.getInstance().getCookie(url.toString()) ?: return emptyList()
                return cookieStr.split(";").mapNotNull { Cookie.parse(url, it.trim()) }
            }

            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                val cm = CookieManager.getInstance()
                cookies.forEach { cm.setCookie(url.toString(), it.toString()) }
                cm.flush()
            }
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // 1. 添加 suspend 关键字
    suspend fun getCourseTableJson(): String = withContext(Dispatchers.IO) { // 2. 强制切换到 IO 线程
        val formBody = FormBody.Builder()
            .add("studentCode", "230200130")
            .add("yearTermId", "85")
            .add("yearTermName", "20252026s")
            .build()

        val request = Request.Builder()
            .url("https://jwgl.dhu.edu.cn/dhu/StudentCourseTable/getData")
            .post(formBody)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Referer", "https://jwgl.dhu.edu.cn/dhu/student/for-std/course-table")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string() ?: "Response body is null"
            } else {
                "HTTP Error: ${response.code}"
            }
        } catch (e: Exception) {
            // 3. 打印详细堆栈，这样以后如果是网络超时，你能一眼看出来
            Log.e("NETWORK_ERROR", "Request failed", e)
            "Error: ${e.javaClass.simpleName} - ${e.message}"
        }
    }
}