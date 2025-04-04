package com.example.mybookingapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarMonthView(
    bookings: List<Booking>,
    currentMonth: YearMonth,
    modifier: Modifier = Modifier,
    onLongPressBooking: (Booking) -> Unit,
    onTapBooking: (Booking) -> Unit
) {
    val bookingRoleMap = remember(bookings) {
        bookings.flatMap { booking ->
            val start = LocalDate.ofEpochDay(booking.startDate)
            val end = LocalDate.ofEpochDay(booking.endDate)
            generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .map { date ->
                    val role = when {
                        start == end -> "single"
                        date == start -> "start"
                        date == end -> "end"
                        else -> "middle"
                    }
                    date to (booking to role)
                }
        }.toMap()
    }

    val firstDay = currentMonth.atDay(1)
    val totalDays = currentMonth.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7 // Sunday = 0

    val days = List(startOffset) { null } +
            (1..totalDays).map { currentMonth.atDay(it) }

    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(days) { date ->
                val (booking, role) = bookingRoleMap[date] ?: (null to null)

                val backgroundColor = when {
                    date == null -> Color.Transparent
                    role == "start" || role == "middle" || role == "end"|| role == "single" -> Color(0xFFFFCDD2)
                    else -> Color(0xFFC8E6C9)
                }

                val shape = when (role) {
                    "single" -> RoundedCornerShape(50.dp)
                    "start" -> RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                    "end" -> RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                    "middle" -> RectangleShape
                    else -> RectangleShape
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(backgroundColor)
                        .combinedClickable(
                            onClick = {
                                booking?.let { onTapBooking(it) }
                            },
                            onLongClick = {
                                booking?.let { onLongPressBooking(it) }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    date?.dayOfMonth?.let {
                        Text(
                            text = it.toString(),
                            color = if (booking != null) Color.Black else Color.DarkGray,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}