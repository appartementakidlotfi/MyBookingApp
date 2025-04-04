package com.example.mybookingapp

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.YearMonth
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: BookingViewModel) {
    val context = LocalContext.current
    val bookings by viewModel.bookingsFlow.collectAsState(initial = emptyList())

    val totalPages = 240
    val startPage = totalPages / 2
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { totalPages })
    val baseMonth = YearMonth.now().minusMonths(startPage.toLong())
    val currentMonth = baseMonth.plusMonths(pagerState.currentPage.toLong())

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedBookingForInfo by remember { mutableStateOf<Booking?>(null) }
    var showAdminScreen by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (showAdminScreen) {
        AdminPanelScreen(
            viewModel = viewModel,
            onClose = { showAdminScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${currentMonth.month.name.lowercase().replaceFirstChar(Char::titlecase)} ${currentMonth.year}")
                },
                actions = {
                    IconButton(onClick = { showPinDialog = true }) {
                        Icon(Icons.Default.Lock, contentDescription = "Admin Panel")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) { page ->
            val month = baseMonth.plusMonths(page.toLong())
            CalendarMonthView(
                bookings = bookings,
                currentMonth = month,
                onLongPressBooking = { selectedBooking = it },
                onTapBooking = { selectedBookingForInfo = it }
            )
        }
    }

    if (showAddDialog) {
        AddBookingDialog(
            onDismiss = { showAddDialog = false },
            viewModel = viewModel,
            initialMonth = currentMonth
        )
    }

    selectedBooking?.let { booking ->
        AlertDialog(
            onDismissRequest = { selectedBooking = null },
            title = { Text("Delete booking?") },
            text = { Text("Are you sure you want to delete the booking for ${booking.guestName}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBooking(booking)
                    Toast.makeText(context, "Booking deleted", Toast.LENGTH_SHORT).show()
                    selectedBooking = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    selectedBookingForInfo?.let { booking ->
        AlertDialog(
            onDismissRequest = { selectedBookingForInfo = null },
            title = { Text("Booking Details") },
            text = {
                Column {
                    Text("Guest: ${booking.guestName}")
                    //Text("Phone: ${booking.guestPhone}")

                    //val context = LocalContext.current
                    val phoneNumber = booking.guestPhone.filter { it.isDigit() }
                    val waUrl = "https://wa.me/$phoneNumber"

                    val annotatedText = buildAnnotatedString {
                        append("Phone: ")
                        pushStringAnnotation(tag = "WA_LINK", annotation = waUrl)
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(booking.guestPhone)
                        }
                        pop()
                    }

                    Text(
                        text = annotatedText,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, waUrl.toUri()).apply {
                                setPackage("com.whatsapp.w4b") // target WhatsApp Business
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp Business not installed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    Text("Price: ${booking.price} DZD")
                    Text("From: ${LocalDate.ofEpochDay(booking.startDate)}")
                    Text("To: ${LocalDate.ofEpochDay(booking.endDate)}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookingForInfo = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPinDialog) {
        AdminPinDialog(
            onSuccess = {
                showPinDialog = false
                showAdminScreen = true
            },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
fun AdminPinDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Admin Access") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = false
                    },
                    label = { Text("Enter PIN") },
                    isError = error,
                    singleLine = true
                )
                if (error) {
                    Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (pin == "1234") {
                    onSuccess()
                } else {
                    error = true
                }
            }) {
                Text("Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}