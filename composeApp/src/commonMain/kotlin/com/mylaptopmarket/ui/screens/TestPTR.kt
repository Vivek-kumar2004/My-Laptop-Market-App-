package com.mylaptopmarket.ui.screens
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun TestPTR() {
    PullToRefreshBox(isRefreshing = false, onRefresh = {}) {
    }
}