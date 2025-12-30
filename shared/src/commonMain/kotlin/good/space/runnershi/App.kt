package good.space.runnershi

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import good.space.runnershi.ui.home.HomeRoute
import good.space.runnershi.ui.login.LoginRoute
import good.space.runnershi.ui.navigation.Screen
import good.space.runnershi.ui.signup.SignUpRoute
import good.space.runnershi.ui.theme.RunnersHiTheme
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
                        navigateToRun = {
                            navController.navigate(Screen.RUN.name)
                        }
                    )
                }
            }
        }
    }
}
