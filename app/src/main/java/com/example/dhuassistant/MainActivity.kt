package com.example.dhuassistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import android.util.Log
import java.time.LocalDate
class MainActivity : ComponentActivity() {

    private val networkManager = NetworkManager()
    private val semesterStartDate = LocalDate.of(2025, 3, 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启动即跳转 CAS 登录
        startActivityForResult(
            Intent(this, LoginActivity::class.java),
            1001
        )

        setContent {
            // 登录前占位
            androidx.compose.material3.Text("请先完成登录…")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                try {
                    // 1. 获取数据
                    val json = networkManager.getCourseTableJson()
                    Log.d("DHU_DEBUG", "收到数据: $json")

                    if (json.startsWith("Error") || json.isEmpty()) {
                        setContent { androidx.compose.material3.Text("获取失败: $json") }
                        return@launch
                    }

                    // 2. 解析 JSON
                    // 重点：如果返回的不是 JSON 而是 HTML（比如登录失效了），这里会直接崩溃
                    val response = Gson().fromJson(json, CourseTableResponse::class.java)

                    // 3. 解析 HTML 内容获取全量课程
                    val allCourses = CourseTableParser.parse(response.content)

                    // 关键：将全量课程分发到每一周
                    val totalWeeks = 20
                    val coursesByWeek = mutableMapOf<Int, List<Course>>()

                    for (w in 1..totalWeeks) {
                        // 过滤出：在第 w 周有课的所有课程
                        val currentWeekCourses = allCourses.filter { course ->
                            course.weekList.contains(w)
                        }
                        coursesByWeek[w] = currentWeekCourses
                    }

                    // 4. 渲染 UI
                    setContent {
                        WeekPager(
                            totalWeeks = totalWeeks,
                            coursesByWeek = coursesByWeek,
                            startDate = semesterStartDate
                        )
                    }
                } catch (e: Exception) {
                    // 捕获所有异常并打印，防止闪退
                    Log.e("DHU_ERROR", "发生崩溃", e)
                    setContent {
                        androidx.compose.material3.Text("程序出错了：${e.localizedMessage}")

                    }
                }
            }
        }
    }
}
