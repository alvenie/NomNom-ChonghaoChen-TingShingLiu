package com.example.nomnom

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nomnom.pages.FriendRequestsPage
import com.example.nomnom.pages.FriendsPage
import com.example.nomnom.pages.HomePage
import com.example.nomnom.pages.LoginPage
import com.example.nomnom.pages.SignupPage
import com.example.nomnom.pages.ProfilePage
import com.example.nomnom.pages.RoulettePage
import com.example.nomnom.pages.SearchPage
import com.example.nomnom.pages.ChatPage

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier, navController, authViewModel)
        }
        composable("signup"){
            SignupPage(modifier, navController, authViewModel)
        }
        composable("home"){
            HomePage(navController, authViewModel, homeViewModel)
        }
        composable("profile"){
            ProfilePage(navController, authViewModel)
        }
        composable("friends"){
            FriendsPage(navController, authViewModel)
        }
        composable("friendRequests"){
            FriendRequestsPage(navController, authViewModel)
        }
        composable("search"){
            SearchPage(navController, homeViewModel)
        }
        composable("roulette"){
            RoulettePage(navController, homeViewModel)
        composable(
            "chat/{friendEmail}",
            arguments = listOf(navArgument("friendEmail") { type = NavType.StringType })
        ) { backStackEntry ->
            val friendEmail = backStackEntry.arguments?.getString("friendEmail") ?: ""
            ChatPage(navController, friendEmail)
        }
    })
}

