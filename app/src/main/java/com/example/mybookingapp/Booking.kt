package com.example.mybookingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startDate: Long,
    val endDate: Long,
    val guestName: String,
    val guestPhone: String,
    val price: Int
)