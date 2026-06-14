package com.farmai.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.farmai.feature.broker.ui.BrokerDetailScreen
import com.farmai.feature.broker.ui.BrokerListScreen
import com.farmai.feature.farmer.ui.FarmerDetailScreen
import com.farmai.feature.farmer.ui.FarmerListScreen
import com.farmai.feature.receipt.ui.ReceiptDetailScreen
import com.farmai.feature.receipt.ui.ReceiptEntryScreen
import com.farmai.feature.receipt.ui.ReceiptListScreen

@Composable
fun FarmAINavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToFarmers = { navController.navigate("farmers") },
                onNavigateToBrokers = { navController.navigate("brokers") },
                onNavigateToReceipts = { navController.navigate("receipts") }
            )
        }

        composable("farmers") {
            FarmerListScreen(navController = navController)
        }
        composable("farmer/add") {
            FarmerDetailScreen(navController = navController, farmerId = null)
        }
        composable("farmer/edit/{farmerId}") { backStackEntry ->
            FarmerDetailScreen(navController = navController, farmerId = backStackEntry.arguments?.getString("farmerId"))
        }

        composable("brokers") {
            BrokerListScreen(navController = navController)
        }
        composable("broker/add") {
            BrokerDetailScreen(navController = navController, brokerId = null)
        }
        composable("broker/edit/{brokerId}") { backStackEntry ->
            BrokerDetailScreen(navController = navController, brokerId = backStackEntry.arguments?.getString("brokerId"))
        }

        composable("receipts") {
            ReceiptListScreen(navController = navController)
        }
        composable("receipt/add") {
            ReceiptEntryScreen(navController = navController)
        }
        composable("receipt/{receiptId}") { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: return@composable
            ReceiptDetailScreen(navController = navController, receiptId = receiptId)
        }
    }
}