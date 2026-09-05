package com.example.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.local.PreferencesManager
import com.example.ui.theme.CharcoalSlate900
import com.example.ui.theme.CharcoalSlate950
import com.example.ui.theme.CodeStreakTheme
import com.example.ui.theme.DevCyan
import com.example.ui.theme.StreakAmber
import com.example.ui.theme.StreakAmberLight
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.StreakFlameLight
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel

class AuthActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PreferencesManager(this)
        if (prefs.isLoggedIn) {
            if (prefs.isAdmin) {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
                return
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            }
        }

        setContent {
            CodeStreakTheme(themeMode = prefs.themeMode) {
                val uiState by authViewModel.uiState.collectAsState()

                LaunchedEffect(uiState) {
                    if (uiState is AuthUiState.Success) {
                        val user = (uiState as AuthUiState.Success).user
                        if (user.isAdmin) {
                            Toast.makeText(this@AuthActivity, "Welcome Administrator!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AuthActivity, AdminActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@AuthActivity, "Welcome to CodeStreak!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AuthActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                AuthScreenContent(
                    uiState = uiState,
                    onLogin = { email, pass -> authViewModel.login(email, pass) },
                    onRegister = { name, email, pass -> authViewModel.register(name, email, pass) },
                    onGuestLogin = { authViewModel.loginAsGuest() },
                    onResetError = { authViewModel.resetState() }
                )
            }
        }
    }
}

@Composable
fun AuthScreenContent(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onGuestLogin: () -> Unit,
    onResetError: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val errorMessage = (uiState as? AuthUiState.Error)?.message
    val isLoading = uiState is AuthUiState.Loading

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CharcoalSlate950
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CharcoalSlate950)
        ) {
            // Glassmorphism Ambient Glowing Background Orbs
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .offset(x = (-40).dp, y = 40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                StreakFlame.copy(alpha = 0.35f),
                                StreakAmber.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevCyan.copy(alpha = 0.25f),
                                StreakFlame.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // High-Energy Flame Header Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(StreakFlame, StreakAmber)
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "</>",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CodeStreak",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Ignite your developer habit daily.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StreakAmberLight
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Glassmorphism Main Card Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xE61E293B), // Translucent Slate Glass Top
                                    Color(0xB30F172A)  // Translucent Slate Glass Bottom
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.08f),
                                    StreakFlame.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Glass Tab Selector
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = StreakAmber,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = StreakFlame,
                                    height = 3.dp
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    onResetError()
                                },
                                text = {
                                    Text(
                                        text = "Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (selectedTab == 0) Color.White else Color(0xFF94A3B8)
                                    )
                                },
                                modifier = Modifier.testTag("tab_login")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = {
                                    selectedTab = 1
                                    onResetError()
                                },
                                text = {
                                    Text(
                                        text = "Create Account",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (selectedTab == 1) Color.White else Color(0xFF94A3B8)
                                    )
                                },
                                modifier = Modifier.testTag("tab_register")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, Color(0x66EF4444), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        if (selectedTab == 1) {
                            // Register Username
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Developer Handle", color = Color(0xFF94A3B8)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = StreakAmber
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_username_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StreakFlame,
                                    unfocusedBorderColor = Color(0x40FFFFFF),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x22000000)
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = StreakAmber
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StreakFlame,
                                unfocusedBorderColor = Color(0x40FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x33000000),
                                unfocusedContainerColor = Color(0x22000000)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = StreakAmber
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StreakFlame,
                                unfocusedBorderColor = Color(0x40FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x33000000),
                                unfocusedContainerColor = Color(0x22000000)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary Action Button (Flame Ember Gradient)
                        Button(
                            onClick = {
                                if (selectedTab == 0) {
                                    onLogin(email, password)
                                } else {
                                    onRegister(username, email, password)
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(StreakFlame, StreakAmber)
                                    )
                                )
                                .testTag("submit_auth_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "Sign In" else "Start Coding Streak",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Glassmorphism Quick Demo Button
                        OutlinedButton(
                            onClick = onGuestLogin,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("guest_login_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DevCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quick Demo / Developer Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DevCyan
                            )
                        }
                    }
                }
            }
        }
    }
}
