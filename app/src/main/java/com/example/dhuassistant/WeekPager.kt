package com.example.dhuassistant

import androidx.compose.runtime.Composable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import java.time.LocalDate
@Composable
fun WeekPager(
    totalWeeks: Int = 20,
    coursesByWeek: Map<Int, List<Course>>,
    startDate: LocalDate
) {

    val pagerState = rememberPagerState(pageCount = { totalWeeks })

    HorizontalPager(
        state = pagerState
    ) { page ->

        val week = page + 1
        val courses = coursesByWeek[week] ?: emptyList()
        val table = buildCourseTable(courses)

        CourseTableView(
            week = week,
            table = table,
            startDate = startDate
        )
    }
}