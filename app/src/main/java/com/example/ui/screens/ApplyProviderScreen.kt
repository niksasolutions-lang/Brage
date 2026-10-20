package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProviderApplicationInfo
import com.example.data.model.ServiceCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyProviderScreen(
    categories: List<ServiceCategory>,
    onBackClick: () -> Unit,
    onSubmitApplication: (ProviderApplicationInfo) -> Unit
) {
    BackHandler {
        onBackClick()
    }

    val coroutineScope = rememberCoroutineScope()

    // Form State: Business & Contact Info
    var businessName by remember { mutableStateOf("") }
    var legalStructure by remember { mutableStateOf("LLC (Limited Liability Company)") }
    var contactPersonName by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var businessEmail by remember { mutableStateOf("") }
    var businessWebsite by remember { mutableStateOf("") }

    // Service Category & Description
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_repairs") }
    var serviceDescription by remember { mutableStateOf("") }
    var equipmentDetails by remember { mutableStateOf("") }
    var ecoFriendlyMaterials by remember { mutableStateOf(true) }

    // Service Area Coverage
    var serviceRadiusMiles by remember { mutableStateOf(25f) }
    var primaryMetroArea by remember { mutableStateOf("Greater Springfield & Eugene Metro Area") }
    var serviceZipCodes by remember { mutableStateOf("97401, 97402, 97403, 97405, 97477, 97478") }

    // Hourly Rates & Pricing
    var hourlyRateInput by remember { mutableStateOf("75") }
    var minimumCalloutFeeInput by remember { mutableStateOf("95") }
    var emergencySurchargeInput by remember { mutableStateOf("35") }

    // Credentials, License & Insurance
    var yearsExperience by remember { mutableStateOf("8") }
    var licenseNumber by remember { mutableStateOf("") }
    var insuranceCarrier by remember { mutableStateOf("") }
    var insurancePolicyNumber by remember { mutableStateOf("") }

    // Verification & Agreements
    var backgroundCheckConsent by remember { mutableStateOf(true) }
    var agreeToTerms by remember { mutableStateOf(true) }
    var signLater by remember { mutableStateOf(false) }
    var digitalSignatureName by remember { mutableStateOf("") }

    // Submission states
    var isSubmitting by remember { mutableStateOf(false) }
    var submittedApplicationId by remember { mutableStateOf<String?>(null) }
    var submittedSignLater by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedCategoryTitle = categories.find { it.id == selectedCategoryId }?.title ?: "Home Repairs & Trade"

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
                            text = "Apply as Service Provider",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Partner with Brage & Grow Your Local Business",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (submittedApplicationId != null) {
            // Success State Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (submittedSignLater) "Application Registered • Sign Later" else "Application Submitted!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (submittedSignLater) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (submittedSignLater) "STATUS: SIGNATURE PENDING • ID: $submittedApplicationId" else "REFERENCE ID: $submittedApplicationId",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (submittedSignLater) Color(0xFF92400E) else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (submittedSignLater) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Agreement Signature Pending", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                Text("You chose 'Sign Later' during registration. Your application review has started. You can complete your electronic signature anytime from your Profile settings before taking jobs.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF78350F), fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Next Steps for ${businessName.ifBlank { "Your Business" }}:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("1. Application Review: Our Brage Trust & Safety Team will verify your trade license and insurance policy within 24 hours.", style = MaterialTheme.typography.bodySmall)
                        Text("2. Dispatch Onboarding: You will receive an SMS and email with your Brage Specialist Portal credentials.", style = MaterialTheme.typography.bodySmall)
                        Text("3. Accept Dispatches: Set your real-time availability and start receiving high-paying client requests in your area.", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Brage Settings", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Application Form
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("apply_service_provider_screen"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Join the Brage Pro Network", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text("Keep 90%+ of your earnings • Verified leads", style = MaterialTheme.typography.labelSmall, color = Color(0xFF93C5FD))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProAdvantageItem(icon = Icons.Default.Payments, title = "$1,800+/wk", subtitle = "Average Earnings")
                                ProAdvantageItem(icon = Icons.Default.Shield, title = "$1M Coverage", subtitle = "Liability Shield")
                                ProAdvantageItem(icon = Icons.Default.Bolt, title = "Instant Pay", subtitle = "Direct Deposit")
                            }
                        }
                    }
                }

                // Section 1: Business Identity & Contact Info
                item {
                    ProviderFormSection(
                        title = "1. Business Name & Contact Information",
                        icon = Icons.Default.Storefront
                    ) {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Trade Name *") },
                            placeholder = { Text("e.g. Apex Master Plumbing LLC") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_business_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = legalStructure,
                            onValueChange = { legalStructure = it },
                            label = { Text("Legal Entity Type") },
                            placeholder = { Text("LLC, Sole Proprietor, Corporation") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = contactPersonName,
                            onValueChange = { contactPersonName = it },
                            label = { Text("Lead Specialist / Contact Person *") },
                            placeholder = { Text("e.g. Marcus Vance") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = businessPhone,
                                onValueChange = { businessPhone = it },
                                label = { Text("Direct Phone *") },
                                placeholder = { Text("+1 (555) 382-9912") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("provider_phone_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = businessEmail,
                                onValueChange = { businessEmail = it },
                                label = { Text("Business Email *") },
                                placeholder = { Text("pro@apexplumbing.com") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("provider_email_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = businessWebsite,
                            onValueChange = { businessWebsite = it },
                            label = { Text("Website or Portfolio URL (Optional)") },
                            placeholder = { Text("https://apexmasterplumbing.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            singleLine = true
                        )
                    }
                }

                // Section 2: Primary Category & Description of Services
                item {
                    ProviderFormSection(
                        title = "2. Description of Services & Category",
                        icon = Icons.Default.Category
                    ) {
                        Text("Select Primary Service Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                        // Category Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categories.chunked(2).forEach { rowCats ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCats.forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategoryId == cat.id,
                                            onClick = { selectedCategoryId = cat.id },
                                            label = { Text(cat.title, fontSize = 12.sp, maxLines = 1) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = serviceDescription,
                            onValueChange = { serviceDescription = it },
                            label = { Text("Detailed Description of Services *") },
                            placeholder = { Text("Describe the full scope of services you offer, typical turnaround, and why clients choose your business...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("provider_service_description_input"),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5
                        )

                        OutlinedTextField(
                            value = equipmentDetails,
                            onValueChange = { equipmentDetails = it },
                            label = { Text("Tools & Commercial Equipment Standards") },
                            placeholder = { Text("e.g. Rigid camera snakes, commercial HEPA vacs, high-pressure steamers...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = ecoFriendlyMaterials,
                                onCheckedChange = { ecoFriendlyMaterials = it }
                            )
                            Text(
                                text = "We provide child, pet-safe and eco-certified materials upon client request",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section 3: Service Area Coverage
                item {
                    ProviderFormSection(
                        title = "3. Service Area & Travel Policy",
                        icon = Icons.Default.Map
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Operating Travel Radius:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${serviceRadiusMiles.toInt()} MILES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Slider(
                            value = serviceRadiusMiles,
                            onValueChange = { serviceRadiusMiles = it },
                            valueRange = 5f..60f,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = primaryMetroArea,
                            onValueChange = { primaryMetroArea = it },
                            label = { Text("Primary Metro Region / Municipality *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = serviceZipCodes,
                            onValueChange = { serviceZipCodes = it },
                            label = { Text("Covered ZIP / Postal Codes (Comma Separated) *") },
                            placeholder = { Text("97401, 97402, 97403, 97477") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                // Section 4: Hourly Rates & Package Pricing
                item {
                    ProviderFormSection(
                        title = "4. Hourly Rates & Package Pricing",
                        icon = Icons.Default.Sell
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = hourlyRateInput,
                                onValueChange = { hourlyRateInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Hourly Rate ($) *") },
                                placeholder = { Text("75") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("provider_hourly_rate_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("$") },
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = minimumCalloutFeeInput,
                                onValueChange = { minimumCalloutFeeInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Min. Callout ($)") },
                                placeholder = { Text("95") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix = { Text("$") },
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = emergencySurchargeInput,
                            onValueChange = { emergencySurchargeInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Same-Day / Emergency Surcharge ($)") },
                            placeholder = { Text("35") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("$") },
                            singleLine = true
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Brage Tip: You can adjust your hourly rates, create multi-tier service packages, and offer seasonal promotional discounts anytime after account verification.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Section 5: Credentials, Licenses & Insurance
                item {
                    ProviderFormSection(
                        title = "5. Credentials & Insurance Verification",
                        icon = Icons.Default.Verified
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = yearsExperience,
                                onValueChange = { yearsExperience = it.filter { c -> c.isDigit() } },
                                label = { Text("Years Experience *") },
                                placeholder = { Text("8") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = licenseNumber,
                                onValueChange = { licenseNumber = it },
                                label = { Text("Trade License # *") },
                                placeholder = { Text("CCB-294018") },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("provider_license_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = insuranceCarrier,
                                onValueChange = { insuranceCarrier = it },
                                label = { Text("Insurance Carrier") },
                                placeholder = { Text("Liberty Mutual") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = insurancePolicyNumber,
                                onValueChange = { insurancePolicyNumber = it },
                                label = { Text("Policy # ($1M+)") },
                                placeholder = { Text("POL-8840219") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Background Check Consent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = backgroundCheckConsent,
                                onCheckedChange = { backgroundCheckConsent = it }
                            )
                            Text(
                                text = "I authorize Brage to perform standard identity verification and background screening.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Terms
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreeToTerms,
                                onCheckedChange = { agreeToTerms = it }
                            )
                            Text(
                                text = "I agree to the Brage Service Provider Partner Agreement, 24-hr response standard, and client satisfaction pledge.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        // Digital Signature & Sign Later Options
                        Text(
                            text = "Partner Agreement Signature",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Sign Now Card Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { signLater = false }
                                    .testTag("sign_now_option_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!signLater) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = if (!signLater) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = !signLater, onClick = { signLater = false })
                                        Text("Sign Now", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Input digital signature immediately", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // Sign Later Card Option
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { signLater = true }
                                    .testTag("sign_later_option_card"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (signLater) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = if (signLater) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD97706)) else null
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = signLater, onClick = { signLater = true })
                                        Text("Sign Later", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (signLater) Color(0xFF92400E) else Color.Unspecified)
                                    }
                                    Text("Skip signature for now & sign later", style = MaterialTheme.typography.labelSmall, color = if (signLater) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (!signLater) {
                            OutlinedTextField(
                                value = digitalSignatureName,
                                onValueChange = { digitalSignatureName = it },
                                label = { Text("Full Legal Name (Electronic Signature)") },
                                placeholder = { Text(contactPersonName.ifBlank { "e.g. Alexander Morgan" }) },
                                leadingIcon = {
                                    Icon(Icons.Default.Draw, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("digital_signature_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Text(
                                text = "By typing your full legal name, you execute a binding electronic signature in accordance with the ESIGN Act.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign Later Selected: You can proceed with registration now. Your background check and license verification will proceed immediately. You can sign your agreement from your Profile or before accepting jobs.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Banner
                errorMessage?.let { err ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }

                // Submit Buttons (Primary + Save & Sign Later)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (businessName.isBlank()) {
                                    errorMessage = "Please enter your business or trade name."
                                    return@Button
                                }
                                if (contactPersonName.isBlank()) {
                                    errorMessage = "Please enter the lead specialist contact name."
                                    return@Button
                                }
                                if (businessPhone.isBlank() || businessEmail.isBlank()) {
                                    errorMessage = "Please provide both business phone and email."
                                    return@Button
                                }
                                if (licenseNumber.isBlank()) {
                                    errorMessage = "Please provide your trade license or certification number."
                                    return@Button
                                }
                                if (!signLater && digitalSignatureName.isBlank()) {
                                    errorMessage = "Please enter your electronic signature or select 'Sign Later'."
                                    return@Button
                                }
                                if (!agreeToTerms) {
                                    errorMessage = "Please agree to the Brage Provider Partner Agreement."
                                    return@Button
                                }

                                isSubmitting = true
                                errorMessage = null

                                coroutineScope.launch {
                                    delay(1400)
                                    isSubmitting = false
                                    val refId = "BRG-PRO-${(10000..99999).random()}"
                                    val appInfo = ProviderApplicationInfo(
                                        id = refId,
                                        businessName = businessName.trim(),
                                        contactName = contactPersonName.trim(),
                                        phone = businessPhone.trim(),
                                        email = businessEmail.trim(),
                                        categoryId = selectedCategoryId,
                                        categoryTitle = selectedCategoryTitle,
                                        serviceAreaRadiusMiles = serviceRadiusMiles.toInt(),
                                        hourlyRate = hourlyRateInput.toDoubleOrNull() ?: 75.0,
                                        licenseNumber = licenseNumber.trim(),
                                        insurancePolicyNumber = insurancePolicyNumber.trim().ifBlank { "VERIFICATION_PENDING" },
                                        experienceYears = yearsExperience.toIntOrNull() ?: 5,
                                        description = serviceDescription.trim().ifBlank { "Licensed specialist in $selectedCategoryTitle." },
                                        submittedAt = "Just now",
                                        status = if (signLater) "PENDING_SIGNATURE" else "UNDER_REVIEW",
                                        signLater = signLater,
                                        digitalSignature = if (signLater) "" else digitalSignatureName.trim()
                                    )
                                    onSubmitApplication(appInfo)
                                    submittedSignLater = signLater
                                    submittedApplicationId = refId
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_provider_application_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Registering Provider Application...")
                            } else {
                                Icon(if (signLater) Icons.Default.Schedule else Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (signLater) "Register Application (Sign Later)" else "Submit Service Provider Application",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Secondary Quick "Save & Sign Later" Action Button
                        if (!signLater) {
                            OutlinedButton(
                                onClick = {
                                    signLater = true
                                    if (businessName.isBlank()) {
                                        errorMessage = "Please enter your business or trade name."
                                        return@OutlinedButton
                                    }
                                    if (contactPersonName.isBlank()) {
                                        errorMessage = "Please enter the lead specialist contact name."
                                        return@OutlinedButton
                                    }
                                    if (businessPhone.isBlank() || businessEmail.isBlank()) {
                                        errorMessage = "Please provide both business phone and email."
                                        return@OutlinedButton
                                    }
                                    if (licenseNumber.isBlank()) {
                                        errorMessage = "Please provide your trade license or certification number."
                                        return@OutlinedButton
                                    }

                                    isSubmitting = true
                                    errorMessage = null

                                    coroutineScope.launch {
                                        delay(1200)
                                        isSubmitting = false
                                        val refId = "BRG-PRO-${(10000..99999).random()}"
                                        val appInfo = ProviderApplicationInfo(
                                            id = refId,
                                            businessName = businessName.trim(),
                                            contactName = contactPersonName.trim(),
                                            phone = businessPhone.trim(),
                                            email = businessEmail.trim(),
                                            categoryId = selectedCategoryId,
                                            categoryTitle = selectedCategoryTitle,
                                            serviceAreaRadiusMiles = serviceRadiusMiles.toInt(),
                                            hourlyRate = hourlyRateInput.toDoubleOrNull() ?: 75.0,
                                            licenseNumber = licenseNumber.trim(),
                                            insurancePolicyNumber = insurancePolicyNumber.trim().ifBlank { "VERIFICATION_PENDING" },
                                            experienceYears = yearsExperience.toIntOrNull() ?: 5,
                                            description = serviceDescription.trim().ifBlank { "Licensed specialist in $selectedCategoryTitle." },
                                            submittedAt = "Just now",
                                            status = "PENDING_SIGNATURE",
                                            signLater = true,
                                            digitalSignature = ""
                                        )
                                        onSubmitApplication(appInfo)
                                        submittedSignLater = true
                                        submittedApplicationId = refId
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_and_sign_later_button"),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSubmitting
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Complete Registration & Sign Later", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderFormSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
fun ProAdvantageItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = subtitle, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color(0xFF93C5FD))
    }
}
