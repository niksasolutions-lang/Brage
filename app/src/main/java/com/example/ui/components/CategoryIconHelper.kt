package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "CleaningServices" -> Icons.Default.CleaningServices
        "Build" -> Icons.Default.Build
        "Spa" -> Icons.Default.Spa
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "LocalShipping" -> Icons.Default.LocalShipping
        "Devices" -> Icons.Default.Devices
        "Pets" -> Icons.Default.Pets
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "School" -> Icons.Default.School
        "Celebration" -> Icons.Default.Celebration
        else -> Icons.Default.MiscellaneousServices
    }
}
