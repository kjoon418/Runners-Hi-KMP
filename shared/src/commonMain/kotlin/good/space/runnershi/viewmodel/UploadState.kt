package good.space.runnershi.viewmodel

/**
 * 서버 업로드 상태를 나타내는 enum
 */
enum class UploadState {
    IDLE,       // 대기 (초기 상태)
    UPLOADING,  // 업로드 중
    SUCCESS,    // 업로드 성공
    FAILURE     // 업로드 실패
}

