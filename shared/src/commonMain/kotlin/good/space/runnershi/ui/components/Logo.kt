package good.space.runnershi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.painterResource
import runnershi.shared.generated.resources.Res
import runnershi.shared.generated.resources.logo
import runnershi.shared.generated.resources.logo_signup

@Composable
fun Logo(
    width: Dp
) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "Runners Hi Logo",
        modifier = Modifier
            .width(width)
    )
}

@Composable
fun SignUpLogo(
    width: Dp
) {
    Image(
        painter = painterResource(Res.drawable.logo_signup),
        contentDescription = "Runners Hi Logo",
        modifier = Modifier
            .width(width)
    )
}
