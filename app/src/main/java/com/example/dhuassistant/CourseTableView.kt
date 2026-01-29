package com.example.dhuassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

// 基准高度定义
val baseSlotHeight = 72.dp * 0.75f

// 课程颜色池
val courseColors = listOf(
    Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFFFB74D),
    Color(0xFFBA68C8), Color(0xFFF06292), Color(0xFF4DB6AC), Color(0xFFFFD54F)
)

fun getCourseColor(courseName: String): Color {
    val index = Math.abs(courseName.hashCode()) % courseColors.size
    return courseColors[index]
}

@Composable
fun CourseTableView(
    week: Int,
    table: Map<Int, Map<Int, List<Course>>>, // 【修改点1】这里必须是 List<Course>
    startDate: LocalDate
) {
    val mondayOfThisWeek = remember(week) {
        startDate.plusWeeks((week - 1).toLong())
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // --- 标题区域 ---
        Text(
            text = "第 $week 周",
            fontSize = 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color(0xFF2196F3),
            modifier = Modifier
                .padding(vertical = 12.dp)
                .align(Alignment.CenterHorizontally)
        )

        // --- 星期与日期行 ---
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5)).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("星期", fontSize = 11.sp, color = Color.Gray)
                Text("日期", fontSize = 11.sp, color = Color.Gray)
            }

            val days = listOf("一", "二", "三", "四", "五", "六", "日")
            days.forEachIndexed { index, dayName ->
                val dateOfColumn = mondayOfThisWeek.plusDays(index.toLong())
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = dayName, fontSize = 14.sp)
                    Text(text = "${dateOfColumn.monthValue}/${dateOfColumn.dayOfMonth}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // --- 可滚动课程区域 ---
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row {
                // 左侧时间轴
                Column(modifier = Modifier.width(56.dp)) {
                    timeSlots.forEach { slot ->
                        TimeSlotGap(slot.index, baseSlotHeight)
                        Column(
                            modifier = Modifier.height(baseSlotHeight),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(slot.start, fontSize = 11.sp)
                            Text(slot.end, fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }

                // 右侧课程格子
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 1..7) {
                        Column(modifier = Modifier.weight(1f)) {
                            val renderedSlots = mutableSetOf<Int>()

                            timeSlots.forEach { slot ->
                                if (slot.index in renderedSlots) return@forEach

                                TimeSlotGap(slot.index, baseSlotHeight)

                                // 【修改点2】显式指定 emptyList 的类型防止编译器迷惑
                                val coursesInSlot = table[day]?.get(slot.index) ?: emptyList<Course>()

                                if (coursesInSlot.isNotEmpty()) {
                                    val firstCourse = coursesInSlot[0]
                                    val span = calculateSpan(firstCourse, table[day], slot.index)
                                    Box(
                                        modifier = Modifier
                                            .height(baseSlotHeight * span)
                                            .padding(2.dp)
                                    ) {
                                        // 关键：如果一个格子有多门课，我们要确保它们展示在同一个 Box 里
                                        // 方案：使用 Column 配合 weight(1f)，但要确保这个 Column 填满整个 span 的高度
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            coursesInSlot.forEach { course ->
                                                Box(modifier = Modifier.weight(1f).padding(vertical = 1.dp)) {
                                                    CourseCard(course)
                                                }
                                            }
                                        }
                                        for (i in 0 until span) renderedSlots.add(slot.index + i)
                                    }
                                } else {
                                    Box(modifier = Modifier.height(baseSlotHeight).fillMaxWidth())
                                    renderedSlots.add(slot.index)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun CourseCard(course: Course) {
//    Surface(
//        color = getCourseColor(course.name),
//        shape = RoundedCornerShape(6.dp),
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Column(
//            modifier = Modifier.padding(4.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(text = course.name, fontSize = 10.sp, color = Color.White, textAlign = TextAlign.Center, lineHeight = 11.sp)
//            if (!course.location.isNullOrBlank()) {
//                Text(text = "@${course.location}", fontSize = 8.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
//            }
//        }
//    }
//}
@Composable
fun CourseCard(course: Course, isCompact: Boolean = false) {
    // 根据是否紧凑模式动态调整字号
    val titleFontSize = if (isCompact) 9.sp else 11.sp
    val locationFontSize = if (isCompact) 8.sp else 9.sp
    val contentPadding = if (isCompact) 2.dp else 4.dp

    Surface(
        color = getCourseColor(course.name),
        shape = RoundedCornerShape(4.dp), // 稍微减小圆角，让空间更大
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = course.name,
                fontSize = titleFontSize,
                lineHeight = if (isCompact) 11.sp else 13.sp, // 紧凑模式收紧行间距
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = if (isCompact) 4 else 5, // 允许更多行数以显示全名
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            if (!course.location.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.location, // 去掉 @ 符号节省空间
                    fontSize = locationFontSize,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

// 【修改点3】这里的参数类型也必须匹配 Map<Int, List<Course>>
fun calculateSpan(current: Course, dayMap: Map<Int, List<Course>>?, currentIndex: Int): Int {
    var span = 1
    while (dayMap?.get(currentIndex + span)?.any { it.name == current.name && it.weeks == current.weeks } == true) {
        span++
        if (span > 4) break
    }
    return span
}

@Composable
fun TimeSlotGap(index: Int, baseHeight: androidx.compose.ui.unit.Dp) {
    when (index) {
        5, 10 -> Spacer(modifier = Modifier.height(baseHeight * 0.3f))
    }
}