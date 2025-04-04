package com.example.mybookingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybookingapp.ui.theme.BookingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookingAppTheme {
                val bookingViewModel: BookingViewModel = viewModel()
                CalendarScreen(viewModel = bookingViewModel)
            }
        }
    }
}