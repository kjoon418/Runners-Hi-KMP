package good.space.runnershi.ui.character

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun LayeredCharacter(
    appearance: CharacterAppearance,
    modifier: Modifier = Modifier,
    frameDurationMillis: Int = 100,
    isPlaying: Boolean = true
) {
    val frameIndex = if (isPlaying) {
        val transition = rememberInfiniteTransition(label = "CharacterAnimation")
        val animatedValue by transition.animateValue(
            initialValue = 0,
            targetValue = 4,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = frameDurationMillis * 4,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "FrameIndex"
        )
        animatedValue
    } else {
        0 // 애니메이션을 사용하지 않을 땐 항상 0번 프레임
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // --- 1층: 기본 바디 (가장 아래) ---
        CharacterLayer(
            resource = appearance.base.getFrame(frameIndex),
            contentDescription = "Body"
        )

        // --- 2층: 신발 ---
        appearance.shoes?.let {
            CharacterLayer(resource = it.getFrame(frameIndex), contentDescription = "Shoes")
        }

        // --- 3층: 하의 ---
        appearance.bottom?.let {
            CharacterLayer(resource = it.getFrame(frameIndex), contentDescription = "Bottom")
        }

        // --- 4층: 상의 ---
        appearance.top?.let {
            CharacterLayer(resource = it.getFrame(frameIndex), contentDescription = "Top")
        }

        // --- 5층: 머리 (헤어스타일) ---
        appearance.hair?.let {
            CharacterLayer(resource = it.getFrame(frameIndex), contentDescription = "Hair")
        }

        // --- 6층: 머리 장식 ---
        appearance.head?.let {
            CharacterLayer(resource = it.getFrame(frameIndex), contentDescription = "Head Item")
        }
    }
}

@Composable
private fun CharacterLayer(
    resource: DrawableResource,
    contentDescription: String
) {
    Image(
        painter = painterResource(resource),
        contentDescription = contentDescription,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

private fun List<DrawableResource>.getFrame(index: Int): DrawableResource {
    if (isEmpty()) {
        throw IllegalStateException("리소스 리스트는 비어 있을 수 없습니다.")
    }
    return this[index % this.size]
}
