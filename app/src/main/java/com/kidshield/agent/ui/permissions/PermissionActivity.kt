package com.kidshield.agent.ui.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PermissionScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var usageGranted by remember { mutableStateOf(viewModel.isUsageAccessGranted()) }
    var overlayGranted by remember { mutableStateOf(viewModel.isOverlayGranted()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grant all permissions for Kid Shield to work properly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            PermissionItem(
                icon = Icons.Default.Accessibility,
                title = "Accessibility Service",
                description = "Required for app monitoring and blocking",
                granted = false,
                onGrant = { viewModel.requestAccessibility() }
            )

            PermissionItem(
                icon = Icons.Default.BarChart,
                title = "Usage Access",
                description = "Required for screen time tracking",
                granted = usageGranted,
                onGrant = {
                    viewModel.requestUsageAccess()
                    usageGranted = viewModel.isUsageAccessGranted()
                }
            )

            PermissionItem(
                icon = Icons.Default.Notifications,
                title = "Notification Access",
                description = "Required for reading notifications",
                granted = false,
                onGrant = { viewModel.requestNotificationAccess() }
            )

            PermissionItem(
                icon = Icons.Default.Layers,
                title = "Overlay Permission",
                description = "Required for blocking screens",
                granted = overlayGranted,
                onGrant = {
                    viewModel.requestOverlay()
                    overlayGranted = viewModel.isOverlayGranted()
                }
            )

            PermissionItem(
                icon = Icons.Default.BatteryFull,
                title = "Battery Optimization",
                description = "Required to keep service running",
                granted = false,
                onGrant = { viewModel.requestIgnoreBattery() }
            )

            PermissionItem(
                icon = Icons.Default.LocationOn,
                title = "Location",
                description = "Required for location tracking",
                granted = false,
                onGrant = { /* Request in activity */ }
            )

            PermissionItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera",
                description = "Required for remote camera access",
                granted = false,
                onGrant = { /* Request in activity */ }
            )

            PermissionItem(
                icon = Icons.Default.Mic,
                title = "Microphone",
                description = "Required for audio streaming",
                granted = false,
                onGrant = { /* Request in activity */ }
            )
        }
    }
}

@Composable
fun PermissionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (granted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                TextButton(onClick = onGrant) {
                    Text("Grant")
                }
            }
        }
    }
}
