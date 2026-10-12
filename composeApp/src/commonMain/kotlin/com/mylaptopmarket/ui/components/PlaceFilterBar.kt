package com.mylaptopmarket.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mylaptopmarket.model.Product
import com.mylaptopmarket.ui.theme.TextPrimary

fun productState(product: Product): String {
    val saved = product.vendorState.trim()
    if (saved.isNotEmpty()) return saved
    return VendorPlace.resolve("", product.vendorCity).state
}

fun productMatchesPlace(product: Product, state: String): Boolean {
    if (state.isBlank()) return true
    return productState(product).equals(state.trim(), ignoreCase = true)
}

@Composable
fun PlaceFilterBar(
    products: List<Product>,
    selectedState: String,
    onStateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val states = products
        .map { productState(it) }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }

    if (states.isEmpty()) return

    PlaceMenu(
        label = "State",
        selected = selectedState,
        options = states,
        onSelect = onStateChange,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PlaceMenu(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    val shown = selected.ifBlank { "All" }

    Box(modifier) {
        OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp)
        ) {
            Text(
                "$label: $shown",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelect("")
                    open = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}
