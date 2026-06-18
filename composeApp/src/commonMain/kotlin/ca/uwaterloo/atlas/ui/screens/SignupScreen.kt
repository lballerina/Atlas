package ca.uwaterloo.atlas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.uwaterloo.atlas.platform.PlatformImage
import ca.uwaterloo.atlas.viewmodel.SignupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    vm: SignupViewModel,
    onSwitchClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF7F7FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color(0xFFF7F7FB))
                .padding(bottom = paddingValues.calculateBottomPadding())
                .navigationBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF4C5BD4),
                                Color(0xFF8D6E95),
                                Color(0xFFC79AA1)
                            )
                        )
                    )
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 0.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 30.dp, vertical = 30.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ATLAS",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Create\nAccount",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                        }
                    PlatformImage(
                        uri = "https://ulcxyvywffoxuafjjszo.supabase.co/storage/v1/object/public/photos/bigLogoTransparent.png",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp, end = 20.dp, start = 10.dp, bottom = 20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Signup Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-50).dp)
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = Color(0xFF4C5BD4).copy(alpha = 0.5f),
                            spotColor = Color(0xFF4C5BD4).copy(alpha = 0.5f)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Start your world adventure",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                        
                        Spacer(Modifier.height(30.dp))

                        SignupForm(vm)

                        Spacer(Modifier.height(32.dp))

                        SignupButton(vm)

                        Spacer(Modifier.height(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Already have an account?",
                                color = Color(0xFF6B7280),
                                fontSize = 14.sp
                            )
                            TextButton(onClick = {
                                vm.reset()
                                onSwitchClick()
                            },
                                modifier = Modifier.testTag("switchLogin")) { // for testing clickability
                                Text(
                                    text = "Sign In",
                                    color = Color(0xFF4C5BD4),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignupForm(vm: SignupViewModel) {
    val state by vm.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("First Name")
                SignupTextField(
                    value = state.firstName,
                    onValueChange = { vm.onFirstNameChange(it) },
                    placeholder = "Jane",
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4C5BD4)) }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel("Last Name")
                SignupTextField(
                    value = state.lastName,
                    onValueChange = { vm.onLastNameChange(it) },
                    placeholder = "Doe",
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF4C5BD4)) }
                )
            }
        }

        Column {
            FieldLabel("Email Address")
            SignupTextField(
                value = state.email,
                onValueChange = { vm.onEmailChange(it) },
                placeholder = "name@example.com",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF4C5BD4)) }
            )
        }

        Column {
            FieldLabel("Password")
            SignupTextField(
                value = state.password,
                onValueChange = { vm.onPasswordChange(it) },
                placeholder = "••••••••",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4C5BD4)) },
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF374151),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
        leadingIcon = leadingIcon,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth().testTag("signupInput"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4C5BD4),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color(0xFFF9FAFB),
            unfocusedContainerColor = Color(0xFFF9FAFB),
            cursorColor = Color(0xFF4C5BD4)
        ),
        singleLine = true
    )
}

@Composable
fun SignupButton(vm: SignupViewModel) {
    val state by vm.uiState.collectAsState()
    val loading = state.isLoading
    val error = state.signupError
    val success = state.signupSuccess
    val errorMessage = state.errorMessage

    val msg = when {
        error -> errorMessage
        success -> "Boarding complete..."
        else -> ""
    }
    val colour = if (success) Color(0xFF10B981) else Color(0xFFEF4444)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { if (!loading) vm.trySignup() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    clip = false,
                    ambientColor = Color(0xFF4C5BD4).copy(alpha = 0.4f),
                    spotColor = Color(0xFF4C5BD4).copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4C5BD4),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF4C5BD4).copy(alpha = 0.7f)
            )
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    "Create My Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (msg.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Surface(
                color = colour.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    fontSize = 13.sp,
                    color = colour,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                )
            }
        }
    }
}
