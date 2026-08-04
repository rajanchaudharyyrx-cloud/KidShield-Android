package com.kidshield.agent.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kidshield.agent.R
import com.kidshield.agent.service.KidShieldService
import com.kidshield.agent.ui.pairing.PairingActivity
import com.kidshield.agent.ui.permissions.PermissionActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val isPaired = viewModel.isPaired
    val battery by viewModel.batteryInfo.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val lastSync by viewModel.lastSync.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        if (isPaired) {
            KidShieldService.start(context = androidx.compose.ui.platform.LocalContext.current)
        }
    }

    if (!isPaired) {
        PairingRequiredScreen()
    } else {
        DashboardScreen(
            battery = battery,
            isOnline = isOnline,
            lastSync = lastSync,
            connectionState = connectionState,
            onSync = { viewModel.syncNow() },
            onRefreshBattery = { viewModel.refreshBattery() }
        )
    }
}

@Composable
fun PairingRequiredScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Kid Shield",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Device not paired yet",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                context.startActivity(android.content.Intent(context, PairingActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enter Pairing Code")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = {
                context.startActivity(android.content.Intent(context, PermissionActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permissions")
        }
    }
}

@Composable
fun DashboardScreen(
    battery: com.kidshield.agent.utils.BatteryInfo?,
    isOnline: Boolean,
    lastSync: Long,
    connectionState: Boolean,
    onSync: () -> Unit,
    onRefreshBattery: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kid Shield",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (connectionState) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (connectionState) "Connected" else "Disconnected",
                    fontSize = 12.sp,
                    color = if (connectionState) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Cards
        StatusCard(
            icon = Icons.Default.PhoneAndroid,
            title = stringResource(R.string.device_connected),
            value = "Active",
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusCard(
            icon = Icons.Default.People,
            title = stringResource(R.string.parent_connected),
            value = if (connectionState) "Yes" else "No",
            color = if (connectionState) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusCard(
            icon = Icons.Default.Schedule,
            title = stringResource(R.string.last_sync),
            value = if (lastSync > 0) {
                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastSync))
            } else "Never",
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Battery
        val batteryPercent = battery?.percentage ?: 0
        StatusCard(
            icon = if (battery?.isCharging == true) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
            title = stringResource(R.string.battery),
            value = "$batteryPercent% ${if (battery?.isCharging == true) "(Charging)" else ""}",
            color = when {
                batteryPercent > 50 -> Color(0xFF4CAF50)
                batteryPercent > 20 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusCard(
            icon = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
            title = stringResource(R.string.internet_status),
            value = if (isOnline) stringResource(R.string.online) else stringResource(R.string.offline),
            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Button(
            onClick = onSync,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Sync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Now")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRefreshBattery,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Battery")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                context.startActivity(android.content.Intent(context, PermissionActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Permissions")
        }
    }
}

@Composable
fun StatusCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}
