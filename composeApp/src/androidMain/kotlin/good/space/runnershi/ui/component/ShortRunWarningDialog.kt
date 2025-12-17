package good.space.runnershi.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ShortRunWarningDialog(
    onDismiss: () -> Unit,      // 더 달리기
    onConfirmDiscard: () -> Unit // 기록 삭제하고 종료
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.Gray
            )
        },
        title = {
            Text(
                text = "기록 저장 불가",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "러닝이 너무 짧아서 저장되지 않았어요.\n1분 이상 달렸으면서 100m 이상 달렸을 경우에만 기록이 저장돼요.",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDiscard
            ) {
                Text("삭제하고 종료", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("더 달리기 (취소)", fontWeight = FontWeight.Bold)
            }
        }
    )
}

