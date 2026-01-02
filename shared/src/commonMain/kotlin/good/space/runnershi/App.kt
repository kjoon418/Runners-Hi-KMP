package good.space.runnershi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.ui.home.HomeRoute
import good.space.runnershi.ui.login.LoginRoute
import good.space.runnershi.ui.navigation.Screen
import good.space.runnershi.ui.result.ResultRoute
import good.space.runnershi.ui.running.RunningResultToShow
import good.space.runnershi.ui.running.RunningRoute
import good.space.runnershi.ui.signup.SignUpRoute
import good.space.runnershi.ui.theme.RunnersHiTheme
import kotlinx.serialization.json.Json
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        RunnersHiTheme {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = Screen.Login.name
            ) {
                // 로그인 화면
                composable(route = Screen.Login.name) {
                    LoginRoute(
                        navigateToHome = {
                            navController.navigate(Screen.Home.name) {
                                popUpTo(Screen.Login.name) { inclusive = true }
                            }
                        },
                        navigateToSignUp = {
                            navController.navigate(Screen.SignUp.name)
                        }
                    )
                }

                // 회원가입 화면
                composable(route = Screen.SignUp.name) {
                    SignUpRoute(
                        navigateBack = {
                          navController.popBackStack() // 뒤로 가기
                        },
                        navigateToHome = {
                            navController.navigate(Screen.Home.name)
                        }
                    )
                }

                // 홈 화면
                composable(route = Screen.Home.name) {
                    HomeRoute(
                        navigateToRunning = {
                            navController.navigate(Screen.RUNNING.name)
                        }
                    )
                }

                // 러닝 화면
                composable(route = Screen.RUNNING.name) {
                    RunningRoute(
                        navigateToResult = { userInfo, runResult ->
                            // RESULT 화면으로 이동하기 전에 RUNNING의 savedStateHandle에 데이터 저장
                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                val json = Json { ignoreUnknownKeys = true }

                                userInfo?.let {
                                    set("userInfo", json.encodeToString(UpdatedUserResponse.serializer(), it))
                                }

                                set("runResult", json.encodeToString(RunningResultToShow.serializer(), runResult))
                            }
                            
                            // RESULT로 이동 (RUNNING은 백스택에 유지하여 데이터 전달)
                            navController.navigate(Screen.RESULT.name)
                        }
                    )
                }

                // 결과 화면
                composable(route = Screen.RESULT.name) {
                    // RUNNING이 백스택에 있으므로 previousBackStackEntry로 데이터 접근 가능
                    val previousBackStack = navController.previousBackStackEntry
                    val json = Json { ignoreUnknownKeys = true }

                    val userInfoJson = previousBackStack?.savedStateHandle?.get<String>("userInfo")
                    val userInfo = userInfoJson?.let {
                        try {
                            json.decodeFromString(UpdatedUserResponse.serializer(), it)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    val runResultJson = previousBackStack?.savedStateHandle?.get<String>("runResult")
                    val runResult = runResultJson?.let {
                        try {
                            json.decodeFromString(RunningResultToShow.serializer(), it)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    // 데이터가 정상적으로 넘어왔을 때만 화면 표시
                    if (runResult != null) {
                        ResultRoute(
                            userInfo = userInfo,
                            runResult = runResult,
                            onCloseClick = {
                                // 홈 화면까지 모든 화면을 제거하고 홈으로 이동
                                if (!navController.popBackStack(Screen.Home.name, inclusive = false)) {
                                    // Home이 백스택에 없으면 navigate로 이동
                                    navController.navigate(Screen.Home.name) {
                                        popUpTo(Screen.Home.name) { inclusive = false }
                                    }
                                }
                            }
                        )
                    } else {
                        // 데이터가 없으면 홈으로 튕겨내기
                        LaunchedEffect(Unit) {
                            if (!navController.popBackStack(Screen.Home.name, inclusive = false)) {
                                navController.navigate(Screen.Home.name) {
                                    popUpTo(Screen.Home.name) { inclusive = false }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
