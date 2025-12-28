package good.space.runnershi.di

import good.space.runnershi.repository.AuthRepository
import good.space.runnershi.repository.AuthRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    // AuthRepository 인터페이스를 요청하면 AuthRepositoryImpl을 줍니다.
    single<AuthRepository> {
        AuthRepositoryImpl(
            // named를 사용하여 정확한 클라이언트를 주입
            httpClient = get(named("PublicClient")),
            authenticatedHttpClient = get(named("AuthClient")),
            // BaseURL은 이미 HttpClient에 defaultRequest로 설정했다면
            // Repository 생성자에서 빼는 것도 좋은 리팩토링입니다.
            // 일단은 주입하는 방식으로 보여드립니다.
            baseUrl = get(named("BaseUrl"))
        )
    }
}
