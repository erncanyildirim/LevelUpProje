package com.suer.levelup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suer.levelup.ui.screens.*
import com.suer.levelup.ui.theme.LevelUpTheme
import com.suer.levelup.ui.viewmodel.AuthViewModel
import com.suer.levelup.ui.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LevelUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ViewModel'leri burada oluşturuyoruz ki veriler ekranlar arası kaybolmasın
                    val authViewModel: AuthViewModel = viewModel()
                    val habitViewModel: HabitViewModel = viewModel()

                    // Giriş kontrolü
                    val startDestination = if (authViewModel.isUserLoggedIn()) "main" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {

                        // --- LOGIN ---
                        composable("login") {
                            LoginScreen(
                                onNavigateToSignup = { navController.navigate("signup") },
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- SIGNUP ---
                        composable("signup") {
                            SignupScreen(
                                onNavigateToLogin = { navController.popBackStack() },
                                onSignupSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- MAIN SCREEN ---
                        composable("main") {
                            MainScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                onNavigateToCreateHabit = {
                                    // Yeni alışkanlık ekleme sihirbazını başlat
                                    navController.navigate("create_habit_step1")
                                },
                                authViewModel = authViewModel,
                                habitViewModel = habitViewModel
                            )
                        }

                        // --- YENİ ALIŞKANLIK OLUŞTURMA AKIŞI (3 ADIM) ---

                        // Adım 1: İsim ve Açıklama
                        composable("create_habit_step1") {
                            HabitCreationScreen(
                                onNext = { navController.navigate("create_habit_step2") },
                                onBack = { navController.popBackStack() },
                                viewModel = habitViewModel
                            )
                        }

                        // Adım 2: Zamanlama ve Sıklık
                        composable("create_habit_step2") {
                            HabitSchedulingScreen(
                                onNext = { navController.navigate("create_habit_step3") },
                                onBack = { navController.popBackStack() },
                                viewModel = habitViewModel
                            )
                        }

                        // Adım 3: Kategori ve Kayıt
                        composable("create_habit_step3") {
                            HabitCustomizationScreen(
                                onSave = {
                                    // Kayıt başarılı, Ana Ekrana dön ve sihirbazı geçmişten sil
                                    navController.navigate("main") {
                                        popUpTo("create_habit_step1") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() },
                                viewModel = habitViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}