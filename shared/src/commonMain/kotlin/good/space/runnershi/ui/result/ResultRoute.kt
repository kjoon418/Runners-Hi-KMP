package good.space.runnershi.ui.result

import androidx.compose.runtime.Composable
import good.space.runnershi.model.dto.user.UpdatedUserResponse
import good.space.runnershi.ui.running.RunningResultToShow

@Composable
fun ResultRoute(
    userInfo: UpdatedUserResponse?,
    runResult: RunningResultToShow,
    onCloseClick: () -> Unit
) {
    ResultScreen(
        userInfo = userInfo,
        runResult = runResult,
        onCloseClick = onCloseClick
    )
}
