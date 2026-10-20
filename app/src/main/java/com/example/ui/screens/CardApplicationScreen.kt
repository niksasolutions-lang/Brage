package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedCardInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardApplicationScreen(
    savedCards: List<SavedCardInfo>,
    onBackClick: () -> Unit,
    onSaveCard: (SavedCardInfo) -> Unit,
    onRemoveCard: (String) -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Input Card Form, 1: Brage Pro Rewards Card Application

    // Standard Card Form State
    var cardholderName by remember { mutableStateOf("") }
    var cardNumberRaw by remember { mutableStateOf("") }
    var expiryInput by remember { mutableStateOf("") }
    var cvvInput by remember { mutableStateOf("") }
    var billingStreet by remember { mutableStateOf("") }
    var billingZip by remember { mutableStateOf("") }
    var cardNickname by remember { mutableStateOf("") }
    var setAsDefault by remember { mutableStateOf(true) }

    // Brage Pro Card Application State
    var annualIncome by remember { mutableStateOf("$75,000 - $100,000") }
    var employmentStatus by remember { mutableStateOf("Employed Full-Time") }
    var ssnLastFour by remember { mutableStateOf("") }
    var agreeToCreditTerms by remember { mutableStateOf(true) }
    var cardSignLater by remember { mutableStateOf(false) }

    // Submission & Feedback State
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Card Type Detector
    val detectedBrand = remember(cardNumberRaw) {
        val clean = cardNumberRaw.replace(" ", "")
        when {
            clean.startsWith("4") -> "Visa"
            clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") -> "Mastercard"
            clean.startsWith("34") || clean.startsWith("37") -> "American Express"
            clean.startsWith("6011") || clean.startsWith("65") -> "Discover"
            else -> if (selectedTab == 1) "Brage Pro" else "Card"
        }
    }

    // Formatted Card Number for Display
    val formattedCardNumber = remember(cardNumberRaw) {
        val clean = cardNumberRaw.filter { it.isDigit() }.take(16)
        clean.chunked(4).joinToString(" ")
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Payment & Card Application",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage Cards & Apply for Brage Financing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("card_application_screen"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Selector Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0; errorMessage = null },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Card", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1; errorMessage = null },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFD97706))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Brage Pro Card", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            // Interactive Dynamic Card Visual Preview
            item {
                CardPreviewBox(
                    cardholderName = if (cardholderName.isNotBlank()) cardholderName.uppercase() else "YOUR FULL NAME",
                    formattedNumber = if (formattedCardNumber.isNotBlank()) {
                        formattedCardNumber.padEnd(19, '•')
                    } else {
                        "•••• •••• •••• ••••"
                    },
                    expiry = if (expiryInput.isNotBlank()) expiryInput else "MM/YY",
                    brand = detectedBrand,
                    isBragePro = selectedTab == 1
                )
            }

            // Perks Banner if Brage Pro Card
            if (selectedTab == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Brage Pro Credit & Rewards Perks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            listOf(
                                "5% Instant Cashback on all Brage home & professional services",
                                "0% Intro APR for 12 months on emergency home repairs & large jobs",
                                "$0 Annual Fee & Instant virtual card issuance into app wallet",
                                "Direct Brage Concierge priority booking queue"
                            ).forEach { perk ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(perk, style = MaterialTheme.typography.labelSmall, color = Color(0xFFE0E7FF))
                                }
                            }
                        }
                    }
                }
            }

            // Input Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = if (selectedTab == 0) "Cardholder & Billing Details" else "Brage Pro Card Application Form",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Cardholder Name
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text("Name on Card") },
                            placeholder = { Text("e.g. Alex J. Morgan") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_holder_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true
                        )

                        // Card Number (Only if Tab 0)
                        if (selectedTab == 0) {
                            OutlinedTextField(
                                value = cardNumberRaw,
                                onValueChange = {
                                    val digitsOnly = it.filter { char -> char.isDigit() }.take(16)
                                    cardNumberRaw = digitsOnly.chunked(4).joinToString(" ")
                                },
                                label = { Text("Card Number (16 Digits)") },
                                placeholder = { Text("4000 1234 5678 9010") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_number_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                trailingIcon = {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = detectedBrand,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                },
                                singleLine = true
                            )

                            // Row: Expiry + CVV
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = expiryInput,
                                    onValueChange = {
                                        val digits = it.filter { c -> c.isDigit() }.take(4)
                                        expiryInput = if (digits.length >= 3) {
                                            "${digits.substring(0, 2)}/${digits.substring(2)}"
                                        } else {
                                            digits
                                        }
                                    },
                                    label = { Text("Expiry (MM/YY)") },
                                    placeholder = { Text("08/29") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("card_expiry_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = cvvInput,
                                    onValueChange = { cvvInput = it.filter { c -> c.isDigit() }.take(4) },
                                    label = { Text("CVV / CVC") },
                                    placeholder = { Text("123") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("card_cvv_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    singleLine = true
                                )
                            }
                        }

                        // Additional fields for Brage Pro Card Application (Tab 1)
                        if (selectedTab == 1) {
                            Text("Financial & Identity Verification", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                            // Annual Income
                            Column {
                                Text("Estimated Annual Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("< $50k", "$50k - $100k", "$100k+").forEach { tier ->
                                        FilterChip(
                                            selected = annualIncome.contains(tier.take(4)),
                                            onClick = { annualIncome = tier },
                                            label = { Text(tier, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            // Employment Status
                            OutlinedTextField(
                                value = employmentStatus,
                                onValueChange = { employmentStatus = it },
                                label = { Text("Employment Status") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            // SSN last 4 digits
                            OutlinedTextField(
                                value = ssnLastFour,
                                onValueChange = { ssnLastFour = it.filter { c -> c.isDigit() }.take(4) },
                                label = { Text("Last 4 digits of SSN / Tax ID") },
                                placeholder = { Text("1234") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ssn_last_four_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = agreeToCreditTerms,
                                    onCheckedChange = { agreeToCreditTerms = it }
                                )
                                Text(
                                    text = "I authorize Brage to conduct a soft credit inquiry that will not impact my credit score.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Sign Later Toggle for Card Registration
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (cardSignLater) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (cardSignLater) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Draw,
                                                contentDescription = null,
                                                tint = if (cardSignLater) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Sign Agreement Later",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (cardSignLater) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Register card application now and sign terms before first charge",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (cardSignLater) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = cardSignLater,
                                            onCheckedChange = { cardSignLater = it }
                                        )
                                    }
                                }
                            }
                        }

                        // Billing Address
                        Text("Billing Address & Postal Verification", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                        OutlinedTextField(
                            value = billingStreet,
                            onValueChange = { billingStreet = it },
                            label = { Text("Billing Street Address") },
                            placeholder = { Text("123 Market St, Apt 4B") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("billing_street_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = billingZip,
                                onValueChange = { billingZip = it.take(10) },
                                label = { Text("Postal / ZIP Code") },
                                placeholder = { Text("97401") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("billing_zip_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = cardNickname,
                                onValueChange = { cardNickname = it },
                                label = { Text("Card Nickname") },
                                placeholder = { Text("e.g. Everyday Card") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Default Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Set as Default Payment Method", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("Use automatically for future bookings & dispatched services", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = setAsDefault,
                                onCheckedChange = { setAsDefault = it }
                            )
                        }

                        // Error Banner
                        errorMessage?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (cardholderName.isBlank()) {
                                    errorMessage = "Please enter the cardholder's name."
                                    return@Button
                                }
                                if (selectedTab == 0) {
                                    val cleanNum = cardNumberRaw.replace(" ", "")
                                    if (cleanNum.length < 15) {
                                        errorMessage = "Please enter a valid 16-digit card number."
                                        return@Button
                                    }
                                    if (expiryInput.length < 4) {
                                        errorMessage = "Please enter a valid expiration date (MM/YY)."
                                        return@Button
                                    }
                                    if (cvvInput.length < 3) {
                                        errorMessage = "Please enter a valid 3 or 4-digit CVV code."
                                        return@Button
                                    }
                                } else {
                                    if (ssnLastFour.length != 4) {
                                        errorMessage = "Please enter the last 4 digits of your SSN."
                                        return@Button
                                    }
                                    if (!agreeToCreditTerms) {
                                        errorMessage = "Please accept the credit inquiry terms."
                                        return@Button
                                    }
                                }

                                isSubmitting = true
                                errorMessage = null
                                coroutineScope.launch {
                                    delay(1200)
                                    isSubmitting = false
                                    if (selectedTab == 0) {
                                        val lastFour = cardNumberRaw.replace(" ", "").takeLast(4)
                                        val newCard = SavedCardInfo(
                                            id = "card_${System.currentTimeMillis()}",
                                            cardholderName = cardholderName.trim().uppercase(),
                                            cardNumberMasked = "•••• •••• •••• $lastFour",
                                            expiry = expiryInput,
                                            cardType = detectedBrand,
                                            nickname = cardNickname.ifBlank { "$detectedBrand ending in $lastFour" },
                                            isDefault = setAsDefault,
                                            isBrageProCard = false
                                        )
                                        onSaveCard(newCard)
                                        successDialogMessage = "Your $detectedBrand ending in $lastFour has been securely added to your Brage wallet and encrypted with 256-bit AES protection."
                                    } else {
                                        val newCard = SavedCardInfo(
                                            id = "brage_pro_${System.currentTimeMillis()}",
                                            cardholderName = cardholderName.trim().uppercase(),
                                            cardNumberMasked = "•••• •••• •••• 9940",
                                            expiry = "10/30",
                                            cardType = "Brage Pro Card",
                                            nickname = "Brage Pro 5% Cashback Rewards",
                                            isDefault = setAsDefault,
                                            isBrageProCard = true
                                        )
                                        onSaveCard(newCard)
                                        successDialogMessage = "Congratulations! Your Brage Pro Card Application has been APPROVED with a $5,000 instant credit line and 5% cashback on all service categories."
                                    }
                                    showSuccessDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_card_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(if (selectedTab == 0) "Encrypting & Verifying Card..." else "Processing Instant Approval...")
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedTab == 0) "Save Card to Wallet" else "Submit Application for Instant Approval",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Security Assurance
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PCI-DSS Level 1 Compliant • 256-Bit Bank Encryption • $0 Fraud Liability",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Existing Saved Cards Section
            item {
                Text(
                    text = "Saved Payment Cards (${savedCards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(savedCards) { card ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (card.isBrageProCard) Color(0xFF1E1B4B) else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (card.isBrageProCard) Icons.Default.WorkspacePremium else Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = if (card.isBrageProCard) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = card.nickname,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (card.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "DEFAULT",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${card.cardType} • ${card.cardNumberMasked} • Exp ${card.expiry}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onRemoveCard(card.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Card", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // Success Modal Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (selectedTab == 0) "Card Saved Successfully" else "Application Approved!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Return to Settings")
                }
            }
        )
    }
}

@Composable
fun CardPreviewBox(
    cardholderName: String,
    formattedNumber: String,
    expiry: String,
    brand: String,
    isBragePro: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isBragePro) {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F172A),
                                Color(0xFF1E1B4B),
                                Color(0xFF312E81)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1E3A8A),
                                Color(0xFF2563EB),
                                Color(0xFF3B82F6)
                            )
                        )
                    }
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isBragePro) Color(0xFFFBBF24) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBragePro) "BRAGE PRO PLATINUM" else "BRAGE PAY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // EMV Chip & Contactless Symbol
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Golden Chip
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFDE68A),
                        modifier = Modifier
                            .width(34.dp)
                            .height(26.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF59E0B).copy(alpha = 0.4f))
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Contactless",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 16 Digit Card Number
                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                // Bottom Row: Cardholder & Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = cardholderName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "VALID THRU",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = expiry,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
