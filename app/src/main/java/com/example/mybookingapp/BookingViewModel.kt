package com.example.mybookingapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import java.io.File
import java.time.LocalDate
import android.os.Environment
import android.widget.Toast
import android.net.Uri

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    //private val bookingDao = BookingDatabase.getInstance(application).bookingDao()
    private val bookingDao: BookingDao

    init {
        val db = BookingDatabase.getInstance(application.applicationContext)
        bookingDao = db.bookingDao()
    }

    val bookingsFlow: Flow<List<Booking>> = bookingDao.getAllBookings()

    suspend fun addBookingIfAvailable(
        startDateEpoch: Long,
        endDateEpoch: Long,
        name: String,
        phone: String,
        price: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val conflictCount = bookingDao.countOverlap(startDateEpoch, endDateEpoch)
        if (conflictCount > 0) {
            false
        } else {
            bookingDao.insertBooking(
                Booking(0, startDateEpoch, endDateEpoch, name, phone, price)
            )
            true
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch(Dispatchers.IO) {
            bookingDao.deleteBooking(booking)
        }
    }
    fun clearAllBookings(){
        viewModelScope.launch(Dispatchers.IO) {
            bookingDao.clearAll()
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bookings = bookingDao.getAllSync()

            val csvContent = buildString {
                appendLine("Date,GuestName,GuestPhone,Price")
                bookings.forEach { booking ->
                    val start = LocalDate.ofEpochDay(booking.startDate)
                    val end = LocalDate.ofEpochDay(booking.endDate)
                    var date = start
                    while (!date.isAfter(end)) {
                        appendLine("${date},${booking.guestName},${booking.guestPhone},${booking.price}")
                        date = date.plusDays(1)
                    }
                }
            }

            try {
                val filename = "bookings_by_day.csv"+LocalDate.now().toString()
                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloads, filename)
                file.writeText(csvContent)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exported to Downloads", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /*fun importFromCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.getExternalFilesDir(null), "bookings_by_day.csv")
                if (!file.exists()) return@launch

                val lines = file.readLines().drop(1) // skip header

                // Parse each line into a record
                data class CsvRow(val date: LocalDate, val name: String, val phone: String, val price: Int)
                val rows = lines.mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 4) {
                        val date = LocalDate.parse(parts[0])
                        val name = parts[1]
                        val phone = parts[2]
                        val price = parts[3].toIntOrNull() ?: 6000
                        CsvRow(date, name, phone, price)
                    } else null
                }.sortedBy { it.date }

                // Group rows by guest info
                val grouped = rows.groupBy { Triple(it.name, it.phone, it.price) }

                // Convert each group into bookings
                val bookings = mutableListOf<Booking>()
                for ((_, guestRows) in grouped) {
                    var start = guestRows.first().date
                    var prev = start

                    for (i in 1 until guestRows.size) {
                        val curr = guestRows[i].date
                        if (curr == prev.plusDays(1)) {
                            prev = curr
                        } else {
                            // End of range
                            bookings.add(
                                Booking(
                                    id = 0,
                                    startDate = start.toEpochDay(),
                                    endDate = prev.toEpochDay(),
                                    guestName = guestRows[i - 1].name,
                                    guestPhone = guestRows[i - 1].phone,
                                    price = guestRows[i - 1].price
                                )
                            )
                            start = curr
                            prev = curr
                        }
                    }

                    // Final booking
                    bookings.add(
                        Booking(
                            id = 0,
                            startDate = start.toEpochDay(),
                            endDate = prev.toEpochDay(),
                            guestName = guestRows.last().name,
                            guestPhone = guestRows.last().phone,
                            price = guestRows.last().price
                        )
                    )
                }

                bookingDao.insertAll(bookings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/

    fun importFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val lines = inputStream?.bufferedReader()?.readLines()?.drop(1) ?: return@launch

                data class CsvRow(val date: LocalDate, val name: String, val phone: String, val price: Int)

                val rows = lines.mapNotNull { line ->
                    val parts = line.split(",")
                    if (parts.size == 4) {
                        val date = LocalDate.parse(parts[0].trim())
                        val name = parts[1].trim()
                        val phone = parts[2].trim()
                        val price = parts[3].trim().toIntOrNull() ?: 6000
                        CsvRow(date, name, phone, price)
                    } else null
                }.sortedBy { it.date }

                // Group rows by guest identity and price
                val grouped = rows.groupBy { Triple(it.name, it.phone, it.price) }

                val bookings = mutableListOf<Booking>()

                for ((guestKey, guestRows) in grouped) {
                    val sortedDates = guestRows.map { it.date }.sorted()

                    var start = sortedDates.first()
                    var prev = start

                    for (i in 1 until sortedDates.size) {
                        val curr = sortedDates[i]
                        if (curr == prev.plusDays(1)) {
                            prev = curr
                        } else {
                            bookings.add(
                                Booking(
                                    id = 0,
                                    startDate = start.toEpochDay(),
                                    endDate = prev.toEpochDay(),
                                    guestName = guestKey.first,
                                    guestPhone = guestKey.second,
                                    price = guestKey.third
                                )
                            )
                            start = curr
                            prev = curr
                        }
                    }

                    // Final booking range
                    bookings.add(
                        Booking(
                            id = 0,
                            startDate = start.toEpochDay(),
                            endDate = prev.toEpochDay(),
                            guestName = guestKey.first,
                            guestPhone = guestKey.second,
                            price = guestKey.third
                        )
                    )
                }

                bookingDao.insertAll(bookings)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Imported ${bookings.size} bookings", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}