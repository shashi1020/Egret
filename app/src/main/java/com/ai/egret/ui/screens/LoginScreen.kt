package com.ai.egret.ui.screens


import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ai.egret.R
import com.ai.egret.viewmodels.LoginUiState
import com.ai.egret.viewmodels.LoginViewModel

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    // --- UI State ---
    var showMobileLogin by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showCreatePasswordDialog by remember { mutableStateOf(false) }

    // --- Phone Auth UI State ---
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val countryCodes = listOf("+91")
    var selectedCountryCode by remember { mutableStateOf("+91") }

    // --- Handle Login Results ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                navController.navigate("dashboard") {
                    popUpTo("login") { inclusive = true }
                }
                viewModel.resetState()
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // --- Google Sign-In Setup ---
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                viewModel.signInWithCredential(credential)
            } catch (e: ApiException) {
                Toast.makeText(context, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Phone Auth Callbacks ---
    val auth = FirebaseAuth.getInstance() // Needed specifically for PhoneAuthProvider options
    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(context, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = verId
                codeSent = true
                isTimerRunning = true
                timerSeconds = 60
            }
        }
    }

    // --- Timer Logic ---
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
            isTimerRunning = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFDFBF7)) {
        // Loading Overlay
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.farmer_icon2), // Ensure this exists
                    contentDescription = "Farmer Icon",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("LOGIN", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5D2A))
                Spacer(modifier = Modifier.height(32.dp))

                if (!showMobileLogin) {
                    EmailPasswordGoogleLogin(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = it },
                        onEmailLogin = { viewModel.loginWithEmail(email, password) },
                        onGoogleLogin = {
                            googleSignInClient.signOut()
                            googleAuthLauncher.launch(googleSignInClient.signInIntent)
                        },
                        onSwitchToMobile = {
                            codeSent = false
                            otpCode = ""
                            showMobileLogin = true
                        },
                        onCreatePasswordClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter email first", Toast.LENGTH_SHORT).show()
                            } else {
                                showCreatePasswordDialog = true
                            }
                        }
                    )
                } else {
                    MobileLogin(
                        selectedCountryCode = selectedCountryCode,
                        onCountryCodeClick = { expanded = true },
                        expanded = expanded,
                        onDismiss = { expanded = false },
                        countryCodes = countryCodes,
                        onCountryCodeSelect = { selectedCountryCode = it; expanded = false },
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        codeSent = codeSent,
                        otpCode = otpCode,
                        onOtpCodeChange = { otpCode = it },
                        isTimerRunning = isTimerRunning,
                        timerSeconds = timerSeconds,
                        onResend = { /* Resend Logic similar to Send Logic */ },
                        onPrimaryButtonClick = {
                            if (activity != null) {
                                if (!codeSent) {
                                    val fullPhone = selectedCountryCode + phoneNumber.trim()
                                    if (fullPhone.length < 10) {
                                        Toast.makeText(context, "Invalid Phone", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val options = PhoneAuthOptions.newBuilder(auth)
                                            .setPhoneNumber(fullPhone)
                                            .setTimeout(60L, TimeUnit.SECONDS)
                                            .setActivity(activity)
                                            .setCallbacks(callbacks)
                                            .build()
                                        PhoneAuthProvider.verifyPhoneNumber(options)
                                    }
                                } else {
                                    if (verificationId != null) {
                                        val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                                        viewModel.signInWithCredential(credential)
                                    }
                                }
                            }
                        },
                        onSwitchToEmail = { showMobileLogin = false }
                    )
                }
            }

            // Loading Indicator
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Dialog for Password Creation
    if (showCreatePasswordDialog) {
        CreatePasswordDialog(
            email = email,
            onDismiss = { showCreatePasswordDialog = false },
            onCreate = { newPass -> viewModel.createAccount(email, newPass) }
        )
    }
}

// --- SUB-COMPONENTS (Cleaned up, copy/paste friendly) ---

@Composable
private fun EmailPasswordGoogleLogin(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onEmailLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onSwitchToMobile: () -> Unit,
    onCreatePasswordClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = email, onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )
        TextButton(onClick = onCreatePasswordClick, modifier = Modifier.align(Alignment.End)) {
            Text("Create account / password", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onEmailLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF4B5D2A)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Divider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.weight(1f)); Text(" OR ", color = Color.Gray); Divider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onGoogleLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            // Note: Replace with standard Google Icon if R.drawable.ic_google_logo missing
            Icon(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
            Text("Sign in with Google", modifier = Modifier.padding(start = 12.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Login with mobile", color = Color(0xFF4B5D2A), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onSwitchToMobile() })
    }
}

@Composable
private fun MobileLogin(
    selectedCountryCode: String, onCountryCodeClick: () -> Unit, expanded: Boolean, onDismiss: () -> Unit,
    countryCodes: List<String>, onCountryCodeSelect: (String) -> Unit,
    phoneNumber: String, onPhoneNumberChange: (String) -> Unit,
    codeSent: Boolean, otpCode: String, onOtpCodeChange: (String) -> Unit,
    isTimerRunning: Boolean, timerSeconds: Int, onResend: () -> Unit,
    onPrimaryButtonClick: () -> Unit, onSwitchToEmail: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(100.dp)) {
                OutlinedTextField(
                    value = selectedCountryCode, onValueChange = {}, readOnly = true,
                    modifier = Modifier.clickable { onCountryCodeClick() },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
                    countryCodes.forEach { code -> DropdownMenuItem(text = { Text(code) }, onClick = { onCountryCodeSelect(code) }) }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = phoneNumber, onValueChange = onPhoneNumberChange,
                label = { Text("Phone Number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f)
            )
        }
        if (codeSent) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = otpCode, onValueChange = onOtpCodeChange,
                label = { Text("Enter OTP") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isTimerRunning) Text("Resend OTP in $timerSeconds s", color = Color.Gray)
            else Text("Resend", color = Color.Blue, modifier = Modifier.clickable { onResend() })
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onPrimaryButtonClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(Color(0xFF4B5D2A)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(if (!codeSent) "Send OTP" else "Verify OTP", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("Login with Email/Google", color = Color(0xFF4B5D2A), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onSwitchToEmail() })
    }
}

@Composable
private fun CreatePasswordDialog(email: String, onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var pwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create password for $email") },
        text = {
            Column {
                OutlinedTextField(value = pwd, onValueChange = { pwd = it; error = false }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(value = confirm, onValueChange = { confirm = it; error = false }, label = { Text("Confirm") }, visualTransformation = PasswordVisualTransformation())
                if (error) Text("Mismatch or short password", color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (pwd.length >= 6 && pwd == confirm) { onCreate(pwd); onDismiss() } else error = true }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}