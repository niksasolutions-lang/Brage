package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ServiceContractScreen(
    serviceName: String = "Electric Motor Repair",
    providerName: String = "Marcus Chen",
    price: String = "$150.00",
    onBackClick: () -> Unit,
    onAcceptAndSign: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F14))
            .padding(16.dp)
            .padding(top = 28.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF00E5FF))
            }
            Text("Service Contract", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Service Details Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A24)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service Details", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Service: $serviceName", color = Color.White)
                    Text("Provider: $providerName", color = Color.White)
                    Text("Total: $price", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detailed Terms
            Text("Terms of Service", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Booking Confirmation: Your booking is subject to provider availability and confirmation.\n\n2. Cancellation Policy: Cancellations made within 24 hours of the scheduled service time may incur a fee.\n\n3. Service Guarantee: Brage guarantees the quality of service as defined in our service-level agreement.",
                color = Color.LightGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Disclaimer Section
            Text("Disclaimer", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Brage acts solely as a platform to facilitate connections. We are not responsible for direct interactions, service outcomes, or disputes between providers and customers. Use of this service is at your own risk.",
                color = Color(0xFFE57373), // Reddish color for disclaimer
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Sticky Button
        Button(
            onClick = onAcceptAndSign,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Accept & Sign", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
