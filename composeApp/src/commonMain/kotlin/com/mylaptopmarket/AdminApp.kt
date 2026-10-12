package com.mylaptopmarket

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mylaptopmarket.ui.navigation.AdminNavGraph
import com.mylaptopmarket.ui.theme.MyLaptopMarketTheme

@Composable
fun AdminApp() {
    MyLaptopMarketTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            AdminNavGraph(navController = navController, standalone = true)
        }
    }
}
