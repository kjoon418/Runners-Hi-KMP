package good.space.runnershi.di

import good.space.runnershi.network.ApiClient
import good.space.runnershi.repository.AuthRepository
import good.space.runnershi.repository.AuthRepositoryImpl
import good.space.runnershi.repository.QuestRepository
import good.space.runnershi.repository.QuestRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            httpClient = get(named("PublicClient")),
            authenticatedHttpClient = get(named("AuthClient")),
            baseUrl = get(named("BaseUrl"))
        )
    }

    single<QuestRepository> {
        QuestRepositoryImpl(
            apiClient = get<ApiClient>()
        )
    }
}
