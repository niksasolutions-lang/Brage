package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BrageAppContent
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOut
import com.example.ui.theme.BrageTheme
import com.example.ui.viewmodel.BrageViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val credentialManager = CredentialManager.create(this)

        setContent {
            val brageViewModel: BrageViewModel = viewModel()
            val darkMode by brageViewModel.darkModeEnabled.collectAsStateWithLifecycle()
            val dynamicColors by brageViewModel.dynamicColorsEnabled.collectAsStateWithLifecycle()

            BrageTheme(darkTheme = darkMode, dynamicColor = dynamicColors) {
                var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
                var isGuestMode by remember { mutableStateOf(false) }
                val coroutineScope = rememberCoroutineScope()

                DisposableEffect(Unit) {
                    val authListener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        if (auth.currentUser != null) {
                            isGuestMode = false
                        }
                    }
                    Firebase.auth.addAuthStateListener(authListener)

                    // Silent auto sign-in attempt
                    if (currentUser == null) {
                        attemptAutoSignIn(
                            context = this@MainActivity,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                                isGuestMode = false
                            },
                            onUnauthenticated = {
                                // Keep on AuthScreen or guest
                            },
                            scope = coroutineScope
                        )
                    }

                    onDispose {
                        Firebase.auth.removeAuthStateListener(authListener)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (currentUser == null && !isGuestMode) {
                        AuthScreen(
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                                isGuestMode = false
                            },
                            onContinueAsGuest = {
                                isGuestMode = true
                            }
                        )
                    } else {
                        val brageViewModel: BrageViewModel = viewModel()
                        BrageAppContent(
                            viewModel = brageViewModel,
                            isGuest = currentUser == null,
                            onRequestSignIn = {
                                isGuestMode = false
                            },
                            onSignOut = {
                                signOut(
                                    context = this@MainActivity,
                                    credentialManager = credentialManager,
                                    onSignOutComplete = {
                                        currentUser = null
                                        isGuestMode = false
                                    },
                                    scope = coroutineScope
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
