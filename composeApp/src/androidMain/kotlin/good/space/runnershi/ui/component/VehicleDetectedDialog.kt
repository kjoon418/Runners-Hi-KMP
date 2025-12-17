package good.space.runnershi.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun VehicleDetectedDialog(
    onDismiss: () -> Unit,
    onResumeRun: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // 바깥 터치 시 닫기 (일시정지 상태 유지)
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFFFA000) // Amber/Orange 색상
            )
        },
        title = {
            Text(
                text = "차량 이동 감지 🚗",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("이동 속도가 너무 빨라 러닝 기록을 일시정지했습니다.\n\n정확한 기록을 위해 차량이나 자전거 이동 구간은 제외됩니다.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onResumeRun()
                    onDismiss()
                }
            ) {
                Text("다시 달리기 (Resume)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("확인 (계속 정지)")
            }
        }
    )
}

