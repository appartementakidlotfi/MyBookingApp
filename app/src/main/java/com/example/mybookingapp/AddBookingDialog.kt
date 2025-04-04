package com.example.mybookingapp

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookingDialog(
    onDismiss: () -> Unit,
    viewModel: BookingViewModel,
    initialMonth: YearMonth
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("6000") }

    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    val dateState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = dateState.selectedStartDateMillis
                    val endMillis = dateState.selectedEndDateMillis

                    if (startMillis != null && endMillis != null) {
                        startDate = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        endDate = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }

                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(state = dateState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Booking") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Visitor Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (DZD)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(
                        text = if (startDate != null && endDate != null)
                            "${startDate!!.format(dateFormatter)} - ${endDate!!.format(dateFormatter)}"
                        else "Select Date Range"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || phone.isBlank() ||  startDate == null || endDate == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                coroutineScope.launch {
                    val success = viewModel.addBookingIfAvailable(
                        startDate!!.toEpochDay(),
                        endDate!!.toEpochDay(),
                        name,
                        phone,
                        price.toIntOrNull() ?: 6000
                    )
                    if (success) {
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Date conflict with existing booking", Toast.LENGTH_LONG).show()
                    }
                }
            }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
