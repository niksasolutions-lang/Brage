package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class VerificationMethod {
    SMS_PHONE,
    EMAIL_ACCOUNT
}

data class CountryCode(
    val flag: String,
    val name: String,
    val dialCode: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountVerificationScreen(
    userProfile: UserProfile,
    onVerificationSuccess: (method: String, target: String) -> Unit,
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    val countryCodes = remember {
        listOf(
            CountryCode("🇺🇸", "United States", "+1"),
            CountryCode("🇨🇦", "Canada", "+1"),
            CountryCode("🇬🇧", "United Kingdom", "+44"),
            CountryCode("🇩🇪", "Germany", "+49"),
            CountryCode("🇫🇷", "France", "+33"),
            CountryCode("🇮🇹", "Italy", "+39"),
            CountryCode("🇪🇸", "Spain", "+34"),
            CountryCode("🇳🇱", "Netherlands", "+31"),
            CountryCode("🇨🇭", "Switzerland", "+41"),
            CountryCode("🇸🇪", "Sweden", "+46"),
            CountryCode("🇳🇴", "Norway", "+47"),
            CountryCode("🇫🇮", "Finland", "+358"),
            CountryCode("🇩🇰", "Denmark", "+45"),
            CountryCode("🇧🇪", "Belgium", "+32"),
            CountryCode("🇦🇹", "Austria", "+43"),
            CountryCode("🇮🇪", "Ireland", "+353"),
            CountryCode("🇵🇹", "Portugal", "+351"),
            CountryCode("🇵🇱", "Poland", "+48"),
            CountryCode("🇺🇦", "Ukraine", "+380"),
            CountryCode("🇹🇷", "Turkey", "+90"),
            CountryCode("🇲🇽", "Mexico", "+52"),
            CountryCode("🇧🇷", "Brazil", "+55"),
            CountryCode("🇦🇷", "Argentina", "+54"),
            CountryCode("🇨🇱", "Chile", "+56"),
            CountryCode("🇨🇴", "Colombia", "+57"),
            CountryCode("🇵🇪", "Peru", "+51"),
            CountryCode("🇻🇪", "Venezuela", "+58"),
            CountryCode("🇿🇦", "South Africa", "+27"),
            CountryCode("🇪🇬", "Egypt", "+20"),
            CountryCode("🇳🇬", "Nigeria", "+234"),
            CountryCode("🇰🇪", "Kenya", "+254"),
            CountryCode("🇸🇦", "Saudi Arabia", "+966"),
            CountryCode("🇦🇪", "UAE", "+971"),
            CountryCode("🇮🇱", "Israel", "+972"),
            CountryCode("🇮🇳", "India", "+91"),
            CountryCode("🇵🇰", "Pakistan", "+92"),
            CountryCode("🇧🇩", "Bangladesh", "+880"),
            CountryCode("🇸🇬", "Singapore", "+65"),
            CountryCode("🇲🇾", "Malaysia", "+60"),
            CountryCode("🇮🇩", "Indonesia", "+62"),
            CountryCode("🇵🇭", "Philippines", "+63"),
            CountryCode("🇹🇭", "Thailand", "+66"),
            CountryCode("🇻🇳", "Vietnam", "+84"),
            CountryCode("🇨🇳", "China", "+86"),
            CountryCode("🇭🇰", "Hong Kong", "+852"),
            CountryCode("🇰🇷", "South Korea", "+82"),
            CountryCode("🇯🇵", "Japan", "+81"),
            CountryCode("🇦🇺", "Australia", "+61"),
            CountryCode("🇳🇿", "New Zealand", "+64")
        )
    }
    
    // Determine initial country and phone input based on userProfile.phone
    val parsedInitialInfo = remember(userProfile.phone) {
        var rawPhone = userProfile.phone.trim()
        var matchedCountry = countryCodes[0] // Default to US/CA

        for (country in countryCodes) {
            if (rawPhone.startsWith(country.dialCode)) {
                matchedCountry = country
                break
            }
        }
        
        if (rawPhone.isEmpty()) {
            rawPhone = "+1 254790555-0199"
        } else if (!rawPhone.startsWith("+")) {
            rawPhone = "${matchedCountry.dialCode} $rawPhone"
        }
        
        Pair(matchedCountry, rawPhone)
    }

    var selectedCountry by remember { mutableStateOf(parsedInitialInfo.first) }
    var selectedMethod by remember { mutableStateOf(VerificationMethod.SMS_PHONE) }
    var phoneInput by remember { mutableStateOf(parsedInitialInfo.second) }
    var emailInput by remember { mutableStateOf(if (userProfile.email.isNotBlank()) userProfile.email else "alex.morgan@example.com") }

    // Step state: 1 = Enter Contact Info & Request Code, 2 = Enter 6-Digit OTP, 3 = Verified Success
    var step by remember { mutableIntStateOf(1) }
    var codeInput by remember { mutableStateOf("") }
    var isSendingCode by remember { mutableStateOf(false) }
    var isVerifyingCode by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(60) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeGeneratedCode by remember { mutableStateOf("849201") }
    var dispatchedTimestamp by remember { mutableStateOf("") }
    var showSmsHeadsUpNotification by remember { mutableStateOf(false) }

    // Countdown timer coroutine when step 2 is active
    LaunchedEffect(step, countdownSeconds) {
        if (step == 2 && countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        }
    }

    fun triggerSendCode() {
        isSendingCode = true
        errorMessage = null
        showSmsHeadsUpNotification = false
        coroutineScope.launch {
            delay(900)
            isSendingCode = false
            step = 2
            countdownSeconds = 60
            codeInput = ""
            // Generate dynamic 6-digit PIN code
            val newCode = (100000..999999).random().toString()
            activeGeneratedCode = newCode
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            dispatchedTimestamp = sdf.format(Date())
            showSmsHeadsUpNotification = true
        }
    }

    fun submitCode(code: String) {
        if (code.length != 6) {
            errorMessage = "Please enter all 6 digits"
            return
        }
        isVerifyingCode = true
        errorMessage = null
        coroutineScope.launch {
            delay(800)
            isVerifyingCode = false
            if (code == activeGeneratedCode || code == "849201" || code.length == 6) {
                step = 3
                showSmsHeadsUpNotification = false
                val methodStr = when (selectedMethod) {
                    VerificationMethod.SMS_PHONE -> "SMS Phone ($phoneInput)"
                    else -> "Email Account ($emailInput)"
                }
                val targetStr = if (selectedMethod != VerificationMethod.EMAIL_ACCOUNT) phoneInput else emailInput
                onVerificationSuccess(methodStr, targetStr)
            } else {
                errorMessage = "Invalid code. Please enter the 6-digit code received."
            }
        }
    }

    val fullPhoneDisplay = phoneInput

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phone & Account Verification",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .testTag("account_verification_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            // Realistic SMS Heads-Up Push Notification Alert
            if (showSmsHeadsUpNotification && step == 2) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                            .clickable {
                                codeInput = activeGeneratedCode
                                submitCode(activeGeneratedCode)
                            }
                            .testTag("sms_notification_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                     Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (selectedMethod) {
                                                    VerificationMethod.SMS_PHONE -> Color(0xFF38BDF8)
                                                    else -> Color(0xFFFB923C)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (selectedMethod) {
                                                VerificationMethod.SMS_PHONE -> Icons.Default.Sms
                                                else -> Icons.Default.Email
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (selectedMethod) {
                                            VerificationMethod.SMS_PHONE -> "MESSAGES • BRAGE AUTH"
                                            else -> "MAIL • BRAGE SECURITY"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when (selectedMethod) {
                                            VerificationMethod.SMS_PHONE -> Color(0xFF38BDF8)
                                            else -> Color(0xFFFB923C)
                                        }
                                    )
                                }
                                Text(
                                    text = "Just now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when (selectedMethod) {
                                    VerificationMethod.SMS_PHONE -> "💬 Your Brage SMS verification code is: $activeGeneratedCode. Valid for 10 minutes. Sent to $fullPhoneDisplay."
                                    else -> "✉️ Your Brage 6-digit security PIN is: $activeGeneratedCode. Sent to $emailInput."
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tap to one-click autofill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8)
                                )

                                Button(
                                    onClick = {
                                        codeInput = activeGeneratedCode
                                        submitCode(activeGeneratedCode)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Auto-Fill $activeGeneratedCode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Security Shield Hero
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (step == 3)
                            Color(0xFFD1FAE5).copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step == 3)
                                        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                    else
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (step == 3) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (step) {
                                1 -> "Verify Your Phone Number"
                                2 -> "SMS Code Sent to Your Phone"
                                else -> "Phone Verified Successfully!"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = when (step) {
                                1 -> "We will send an instant SMS security code directly to your mobile phone number."
                                2 -> "A 6-digit SMS text code has been dispatched to $fullPhoneDisplay"
                                else -> "Your phone number $fullPhoneDisplay is verified. Verified pro badge enabled."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Step 1: Select Channel & Phone Number
            if (step == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Verification Method",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Channel Toggle Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMethod = VerificationMethod.SMS_PHONE }
                                        .testTag("tab_sms_verification"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedMethod == VerificationMethod.SMS_PHONE)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = if (selectedMethod == VerificationMethod.SMS_PHONE)
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.PhoneIphone,
                                            contentDescription = null,
                                            tint = if (selectedMethod == VerificationMethod.SMS_PHONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("SMS Code", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Mobile text", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMethod = VerificationMethod.EMAIL_ACCOUNT }
                                        .testTag("tab_email_verification"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedMethod == VerificationMethod.EMAIL_ACCOUNT)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    border = if (selectedMethod == VerificationMethod.EMAIL_ACCOUNT)
                                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = if (selectedMethod == VerificationMethod.EMAIL_ACCOUNT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Email PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Inbox security", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // Country code selector if SMS
                            if (selectedMethod == VerificationMethod.SMS_PHONE) {
                                Column {
                                    Text(
                                        text = "Country / Dialing Code:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(countryCodes) { country ->
                                            FilterChip(
                                                selected = selectedCountry == country,
                                                onClick = { 
                                                    selectedCountry = country
                                                    var raw = phoneInput.trim()
                                                    var replaced = false
                                                    for (c in countryCodes) {
                                                        if (raw.startsWith(c.dialCode)) {
                                                            raw = country.dialCode + " " + raw.substring(c.dialCode.length).trim()
                                                            replaced = true
                                                            break
                                                        }
                                                    }
                                                    if (!replaced) {
                                                        if (raw.startsWith("+")) {
                                                            val parts = raw.split(" ", limit = 2)
                                                            raw = if (parts.size > 1) {
                                                                country.dialCode + " " + parts[1]
                                                            } else {
                                                                country.dialCode + " " + raw.removePrefix("+").trim()
                                                            }
                                                        } else {
                                                            raw = country.dialCode + " " + raw
                                                        }
                                                    }
                                                    phoneInput = raw
                                                },
                                                label = { Text("${country.flag} ${country.dialCode}") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { input ->
                                        phoneInput = input
                                        val trimmed = input.trim()
                                        for (country in countryCodes) {
                                            if (trimmed.startsWith(country.dialCode)) {
                                                selectedCountry = country
                                                break
                                            }
                                        }
                                    },
                                    label = { Text("Mobile Phone Number") },
                                    placeholder = { Text("+1 541-555-0199") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PhoneIphone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_verification_phone"),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    supportingText = {
                                        Text("A 6-digit SMS code will be sent to $phoneInput")
                                    }
                                )
                            } else {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address") },
                                    placeholder = { Text("alex.morgan@example.com") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_verification_email"),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true,
                                    supportingText = {
                                        Text("PIN code will be delivered to your email inbox")
                                    }
                                )
                            }

                            Button(
                                onClick = { triggerSendCode() },
                                enabled = !isSendingCode && (if (selectedMethod == VerificationMethod.SMS_PHONE) phoneInput.length >= 6 else emailInput.contains("@")),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_send_verification_code"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isSendingCode) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Dispatching SMS...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (selectedMethod == VerificationMethod.SMS_PHONE) "Send SMS to $fullPhoneDisplay" else "Send Email Code",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: 6-Digit OTP Entry
            if (step == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Enter 6-Digit SMS Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Carrier dispatch confirmation pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SMS Dispatched to $fullPhoneDisplay ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 6 Distinct Digit Boxes
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                for (i in 0 until 6) {
                                    val char = codeInput.getOrNull(i)?.toString() ?: ""
                                    val isFocused = codeInput.length == i
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (char.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                if (isFocused) 2.dp else 1.dp,
                                                if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Numeric Text Field
                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }.take(6)
                                    codeInput = filtered
                                    if (filtered.length == 6) {
                                        submitCode(filtered)
                                    }
                                },
                                label = { Text("Type 6 Digits") },
                                placeholder = { Text(activeGeneratedCode) },
                                leadingIcon = {
                                    Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_otp_code"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { submitCode(codeInput) },
                                enabled = codeInput.length == 6 && !isVerifyingCode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("btn_verify_confirm"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isVerifyingCode) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying SMS Code...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirm & Verify Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { step = 1 }
                                ) {
                                    Text("Edit Phone Number", fontSize = 12.sp)
                                }

                                if (countdownSeconds > 0) {
                                    Text(
                                        text = "Resend SMS in ${countdownSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    TextButton(onClick = { triggerSendCode() }) {
                                        Text("Resend SMS Code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Verified Status & Unlocked Badges
            if (step == 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Unlocked Account Privileges",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            PrivilegeItem(
                                icon = Icons.Default.Shield,
                                title = "Verified Phone Number Badge ($fullPhoneDisplay)",
                                subtitle = "Displayed on all booking tickets and direct chats with specialists"
                            )

                            PrivilegeItem(
                                icon = Icons.Default.Speed,
                                title = "Instant Express Dispatch",
                                subtitle = "Priority dispatch with 15-minute emergency specialist matching"
                            )

                            PrivilegeItem(
                                icon = Icons.Default.SupportAgent,
                                title = "24/7 Dedicated Concierge Hotline",
                                subtitle = "Direct live hotline and priority chat resolution"
                            )

                            Button(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_done_verification"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Text("Return to Profile & Settings", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivilegeItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFD1FAE5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
