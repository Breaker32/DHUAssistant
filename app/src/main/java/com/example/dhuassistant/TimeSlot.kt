package com.example.dhuassistant
data class TimeSlot(
    val index: Int,
    val start: String,
    val end: String
)

val timeSlots = listOf(
    TimeSlot(1, "08:15", "09:00"),
    TimeSlot(2, "09:00", "09:45"),
    TimeSlot(3, "10:05", "10:50"),
    TimeSlot(4, "10:50", "11:35"),
    TimeSlot(5, "13:00", "13:45"),
    TimeSlot(6, "13:45", "14:30"),
    TimeSlot(7, "14:50", "15:35"),
    TimeSlot(8, "15:35", "16:20"),
    TimeSlot(9, "16:20", "17:05"),
    TimeSlot(10, "18:00", "18:45"),
    TimeSlot(11, "18:45", "19:30"),
    TimeSlot(12, "19:50", "20:35"),
    TimeSlot(13, "20:35", "21:20"),
)
