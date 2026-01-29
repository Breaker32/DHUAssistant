//// TodayCourseView.kt
//package com.example.dhuassistant
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.util.Calendar
//import androidx.compose.foundation.shape.RoundedCornerShape // 【关键】添加此导入
//
//@Composable
//fun TodayCourseView(todayCourses: Map<Int, Course>?) { // 修正：接收单日课程Map
//    // 获取今天是星期几 (1=周一, 7=周日)
//    val calendar = Calendar.getInstance()
//    var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
//    if (dayOfWeek == 0) dayOfWeek = 7
//
//    // 按节次排序
//    val sortedCourses = todayCourses?.toList()
//        ?.sortedBy { it.first }
//        ?.map { it.second }
//        ?: emptyList()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "今日课程（周${listOf("一", "二", "三", "四", "五", "六", "日")[dayOfWeek - 1]}）",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (sortedCourses.isEmpty()) {
//            Text("今天没有课哦～", color = Color.Gray, fontSize = 16.sp)
//        } else {
//            sortedCourses.forEach { course ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = getCourseColor(course.name)
//                    ),
//                    shape = RoundedCornerShape(8.dp) // 现在可正常识别
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text(
//                            text = course.name,
//                            color = Color.White,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium
//                        )
//                        if (!course.location.isNullOrBlank()) {
//                            Text(
//                                text = "@${course.location}",
//                                color = Color.White.copy(alpha = 0.85f),
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(top = 4.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}