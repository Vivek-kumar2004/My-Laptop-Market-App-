package com.mylaptopmarket.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mylaptopmarket.model.Media
import com.mylaptopmarket.model.MediaType
import com.mylaptopmarket.ui.theme.*
import com.mylaptopmarket.ui.tour.tourTarget
import com.mylaptopmarket.util.ExcelPicker
import com.mylaptopmarket.util.ImagePicker
import com.mylaptopmarket.util.hideKeyboard
import com.mylaptopmarket.viewmodel.AddProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel,
    onBackClick: () -> Unit,
    productId: String? = null
) {
    val isEdit = !productId.isNullOrBlank()
    var productName by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Laptops") }
    var youtubeVideo by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Active") }
    var statusExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImages by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
    var sheetName by remember { mutableStateOf<String?>(null) }
    var sheetBytes by remember { mutableStateOf<ByteArray?>(null) }
    var removeSheet by remember { mutableStateOf(false) }
    var existingImages by remember { mutableStateOf<List<Media>>(emptyList()) }
    var formReady by remember { mutableStateOf(!isEdit) }

    val categories = listOf("Laptops", "Desktop", "TFT/Screen", "Server", "Storage", "Accessories", "Graphics Card")
    val statusOptions = listOf("Active", "Deactive")

    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.success.collectAsState()
    val error by viewModel.error.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var formFocused by remember { mutableStateOf(false) }
    val userTouching = remember { mutableStateOf(false) }
    val dismissKeyboardOnScroll = remember(focusManager) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (userTouching.value && source == NestedScrollSource.UserInput && available.y != 0f) {
                    dismissProductKeyboard(focusManager)
                }
                return Offset.Zero
            }
        }
    }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (!productId.isNullOrBlank()) {
            viewModel.loadForEdit(productId)
        }
    }

    LaunchedEffect(editingProduct) {
        val product = editingProduct ?: return@LaunchedEffect
        productName = product.name
        stock = product.stockCount.toString()
        category = product.category?.takeIf { it in categories } ?: (product.category ?: "Laptops")
        youtubeVideo = product.videoUrl.orEmpty()
        description = product.description
        price = product.price?.takeIf { it.isNotBlank() && it != "0" }.orEmpty()
        statusText = if (product.isActive) "Active" else "Deactive"
        existingImages = product.media.filter { media ->
            media.type == MediaType.IMAGE &&
                media.url.isNotBlank() &&
                !media.url.contains("no-image", ignoreCase = true) &&
                !media.url.contains("no-product", ignoreCase = true)
        }
        selectedImages = emptyList()
        // Keep existing category even if not in list
        if (!product.category.isNullOrBlank() && product.category !in categories) {
            category = product.category
        }
        formReady = true
    }

    LaunchedEffect(success) {
        if (success) {
            onBackClick()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            scope.launch { snackbarHostState.showSnackbar(error!!) }
        }
    }

    val categoryChoices = remember(category) {
        if (category !in categories) categories + category else categories
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
                            if (isEdit) "Edit Product" else "Add Product",
                            modifier = Modifier.tourTarget("page-sell"),
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
                            TextButton(onClick = { dismissProductKeyboard(focusManager) }) {
                                Text("Done", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                        if (isEdit && formReady) {
                            IconButton(
                                onClick = { confirmDelete = true },
                                enabled = !isLoading
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete product", tint = ErrorRed)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isEdit && !formReady && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(dismissKeyboardOnScroll)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            userTouching.value = event.changes.any { it.pressed }
                        }
                    }
                }
                .onFocusEvent { formFocused = it.hasFocus }
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp, 18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(PrimaryBlue)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEdit) "Edit Product Information" else "Product Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name *") },
                        placeholder = { Text("Enter Product Name") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dismissProductKeyboard(focusManager) }),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stock,
                        onValueChange = { newValue -> stock = newValue.filter { it.isDigit() } },
                        label = { Text("Stock Quantity *") },
                        placeholder = { Text("Enter Stock Quantity") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { dismissProductKeyboard(focusManager) }),
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
                            categoryChoices.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        category = selectionOption
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = youtubeVideo,
                        onValueChange = { youtubeVideo = it },
                        label = { Text("YouTube Video Link (Optional)") },
                        placeholder = { Text("https://youtube.com/...") },
                        leadingIcon = {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = TextMuted)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { dismissProductKeyboard(focusManager) }),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Enter Product Description") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )

                    Text("Product Images", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)

                    if (isEdit) {
                        if (existingImages.isEmpty() && selectedImages.isEmpty()) {
                            Text(
                                "No images yet. Add photos below.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                "Tap X to remove. Add new photos below, then tap Update Product.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            existingImages.forEach { media ->
                                Box {
                                    AsyncImage(
                                        model = media.url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { existingImages = existingImages.filterNot { it.url == media.url } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(ErrorRed)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove image",
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            selectedImages.forEachIndexed { index, bytes ->
                                Box {
                                    AsyncImage(
                                        model = bytes,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImages = selectedImages.filterIndexed { i, _ -> i != index } },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(ErrorRed)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove new image",
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ImagePicker(
                        onImagesSelected = { bytes ->
                            selectedImages = selectedImages + bytes
                        }
                    ) { onClick ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = SurfaceHover,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = onClick,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryLight,
                                        contentColor = PrimaryBlue
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (isEdit) "Add Images" else "Choose Files",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    if (selectedImages.isEmpty()) {
                                        if (isEdit) "Keep current images" else "No file chosen"
                                    } else {
                                        "${selectedImages.size} file(s) selected"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedImages.isEmpty()) TextMuted else PrimaryBlue,
                                    fontWeight = if (selectedImages.isEmpty()) FontWeight.Normal else FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        "You can select multiple images",
                        fontSize = 11.sp,
                        color = ErrorRed,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    Text("Private stock sheet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Excel stays inside the app. Buyers can read the rows, but the file cannot be downloaded or shared.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    ExcelPicker(
                        onFileSelected = { name, bytes ->
                            sheetName = name
                            sheetBytes = bytes
                            removeSheet = false
                        }
                    ) { onClick ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = SurfaceHover,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = onClick,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryLight,
                                        contentColor = PrimaryBlue
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Choose Excel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text(
                                    sheetName ?: if (isEdit && editingProduct?.hasStockSheet == true && !removeSheet) {
                                        "Sheet already attached"
                                    } else {
                                        "No Excel chosen"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (sheetName == null) TextMuted else PrimaryBlue,
                                    fontWeight = if (sheetName == null) FontWeight.Normal else FontWeight.Bold,
                                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    if (isEdit && editingProduct?.hasStockSheet == true && sheetBytes == null) {
                        TextButton(onClick = { removeSheet = !removeSheet }) {
                            Text(if (removeSheet) "Keep current sheet" else "Remove current sheet")
                        }
                    }

                    OutlinedTextField(
                        value = price,
                        onValueChange = { newValue -> price = newValue.filter { it.isDigit() || it == '.' } },
                        label = { Text("Price (₹) *") },
                        placeholder = { Text("Enter Product Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = statusText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status *") },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statusOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        statusText = opt
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val p = price.toDoubleOrNull() ?: 0.0
                            val s = stock.toIntOrNull() ?: 0
                            val statusVal = if (statusText == "Active") 1 else 0
                            if (isEdit && !productId.isNullOrBlank()) {
                                viewModel.updateProduct(
                                    productId = productId,
                                    productName = productName,
                                    category = category,
                                    description = description,
                                    price = p,
                                    stock = s,
                                    youtubeVideo = if (youtubeVideo.isNotBlank()) youtubeVideo else null,
                                    status = statusVal,
                                    images = selectedImages,
                                    keepImageNames = existingImages.map { imageFileName(it.url) }.filter { it.isNotBlank() },
                                    stockSheetName = sheetName,
                                    stockSheetBytes = sheetBytes,
                                    removeStockSheet = removeSheet && sheetBytes == null
                                )
                            } else {
                                viewModel.addProduct(
                                    productName = productName,
                                    category = category,
                                    description = description,
                                    price = p,
                                    stock = s,
                                    youtubeVideo = if (youtubeVideo.isNotBlank()) youtubeVideo else null,
                                    status = statusVal,
                                    images = selectedImages,
                                    stockSheetName = sheetName,
                                    stockSheetBytes = sheetBytes
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isLoading && productName.isNotBlank() && price.isNotBlank() && stock.isNotBlank() &&
                            (!isEdit || formReady)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = SurfaceWhite)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isEdit) "Update Product" else "Save Product",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    if (isEdit && formReady) {
                        OutlinedButton(
                            onClick = { confirmDelete = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Product", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete && isEdit && !productId.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) confirmDelete = false },
            title = { Text("Delete this product?") },
            text = { Text("This will remove the listing permanently. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteProduct(productId)
                    },
                    enabled = !isLoading
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, enabled = !isLoading) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun dismissProductKeyboard(focusManager: FocusManager) {
    focusManager.clearFocus(force = true)
    hideKeyboard()
}

private fun imageFileName(url: String): String {
    return url.substringAfterLast('/').substringBefore('?').substringBefore('#')
}
