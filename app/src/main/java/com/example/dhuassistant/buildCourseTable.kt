package com.example.dhuassistant


fun buildCourseTable(
    courses: List<Course>
): Map<Int, Map<Int, List<Course>>> {

    val table = mutableMapOf<Int, MutableMap<Int, MutableList<Course>>>()

    for (course in courses) {
        val day = course.day
        val start = course.startLesson
        val duration = course.duration

        val dayMap = table.getOrPut(day) { mutableMapOf() }

        // 将这门课填入它占用的每一个节次
        for (i in 0 until duration) {
            val slotIndex = start + i
            val slotList = dayMap.getOrPut(slotIndex) { mutableListOf() }
            // 防止重复添加（针对跨节课）
            if (!slotList.any { it.name == course.name && it.weeks == course.weeks }) {
                slotList.add(course)
            }
        }
    }
    return table
}