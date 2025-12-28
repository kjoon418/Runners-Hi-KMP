package good.space.runnershi

import android.app.Application
import good.space.runnershi.database.AppDatabase
import good.space.runnershi.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RunnersHiApplication : Application() {
    
    // 애플리케이션 생명주기와 함께 관리되는 코루틴 스코프
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()

        // Koin 초기화는 빠르게 완료되므로 메인 스레드에서 수행
        startKoin {
            androidLogger()
            androidContext(this@RunnersHiApplication)
            modules(appModule)
        }
        
        // 무거운 초기화 작업은 백그라운드 스레드에서 비동기로 수행
        // 데이터베이스 초기화를 미리 수행하여 첫 사용 시 지연 방지
        applicationScope.launch {
            try {
                AppDatabase.initializeDatabase(this@RunnersHiApplication)
            } catch (e: Exception) {
                // 초기화 실패 시 로그만 남기고 계속 진행
                // 실제 사용 시점에 다시 초기화 시도됨
                android.util.Log.e("RunnersHiApplication", "Database pre-initialization failed", e)
            }
        }
    }
}