package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.util.hideKeyboard
import com.mylaptopmarket.viewmodel.AddRequirementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRequirementScreen(
    viewModel: AddRequirementViewModel,
    onBackClick: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Laptops") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categories = listOf(
        "Laptops", "Desktop", "TFT/Screen", "Server", "Storage", "Accessories", "Graphics Card"
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()
    val focusManager = LocalFocusManager.current
    var formFocused by remember { mutableStateOf(false) }
    val dismissKeyboardOnScroll = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    focusManager.clearFocus(force = true)
                    hideKeyboard()
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(success) {
        if (success) onBackClick()
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Post Requirement",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    actions = {
                        if (formFocused) {
                            TextButton(onClick = {
                                focusManager.clearFocus(force = true)
                                hideKeyboard()
                            }) {
                                Text("Done", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(dismissKeyboardOnScroll)
                .onFocusEvent { formFocused = it.hasFocus }
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Use a clear title. Matching titles from other vendors share one card.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(200) },
                label = { Text("Title *") },
                placeholder = { Text("e.g. Need 50 Dell laptops") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus(force = true)
                    hideKeyboard()
                }),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { ch -> ch.isDigit() }.take(8) },
                label = { Text("Quantity (optional)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus(force = true)
                    hideKeyboard()
                }),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(2000) },
                label = { Text("Description (optional)") },
                placeholder = { Text("Specs, budget, timeline...") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            if (!error.isNullOrBlank()) {
                Text(error ?: "", color = ErrorRed, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    viewModel.post(
                        title = title,
                        category = category,
                        quantity = quantity.toIntOrNull(),
                        description = description.ifBlank { null }
                    )
                },
                enabled = !isLoading && title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SurfaceWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Post Requirement", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
