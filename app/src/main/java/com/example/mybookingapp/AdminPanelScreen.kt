package com.example.mybookingapp

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: BookingViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    // File picker launcher for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.importFromUri(context, it)
            }
        }
    )
    var showConfirmClear by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                viewModel.exportToCsv(context)
                Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
            }) {
                Text("Export to CSV")
            }

            Button(onClick = {
                importLauncher.launch("text/csv")

            }) {
                Text("Import from CSV")
            }

            Button(onClick = { showConfirmClear = true }, colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )) {
                Text("Clear All Bookings", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        if (showConfirmClear) {
            AlertDialog(
                onDismissRequest = { showConfirmClear = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete all bookings? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearAllBookings()
                        Toast.makeText(context, "All bookings cleared", Toast.LENGTH_SHORT).show()
                        showConfirmClear = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmClear = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    BackHandler { onClose() }
}