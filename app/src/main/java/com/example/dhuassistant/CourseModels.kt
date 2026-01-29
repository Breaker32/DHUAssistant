package com.example.dhuassistant

data class CourseTableResponse(
    val success: Boolean,
    val content: String
)

data class Course(
    val name: String,
    val teacher: String,
    val weeks: String,
    val weekList: List<Int>, // 解析后的列表，如 [2, 3, 4, 5, 6, 7, 8, 9]
    val day: Int,          // 1=周一 ... 7=周日
    val startLesson: Int,  // 起始节次
    val duration: Int,     // 连续几节
    val location: String
)
