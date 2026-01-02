package good.space.runnershi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import good.space.runnershi.model.type.item.AvatarItem
import good.space.runnershi.ui.theme.RunnersHiTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun ItemCard(
    item: AvatarItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(RunnersHiTheme.custom.questDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val targetImage = item.titleResource

        if (targetImage != null) {
            Image(
                painter = painterResource(targetImage),
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
