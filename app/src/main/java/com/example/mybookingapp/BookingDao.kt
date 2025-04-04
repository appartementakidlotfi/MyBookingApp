package com.example.mybookingapp

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT COUNT(*) FROM bookings WHERE startDate <= :newEnd AND endDate >= :newStart")
    suspend fun countOverlap(newStart: Long, newEnd: Long): Int

    @Insert
    suspend fun insertBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("DELETE FROM bookings")
    suspend fun clearAll()

    @Query("SELECT * FROM bookings")
    fun getAllSync(): List<Booking>

    @Insert
    suspend fun insertAll(bookings: List<Booking>)
}