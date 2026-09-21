package com.project.starter.core.data.di


import com.project.starter.core.data.local.EncryptedSessionManager
import com.project.starter.core.data.local.PlatformPathProvider
import com.project.starter.core.data.network.ExampleApiService
import com.project.starter.core.data.network.createHttpClient
import com.project.starter.core.data.repository.ExampleRepositoryImpl
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.domain.repository.SessionManager
import org.koin.dsl.module
import com.project.starter.core.data.local.createDataStore

val dataModule = module {
    includes(platformModule())
    
    single { createHttpClient() }
    single { ExampleApiService(get()) }
    
    single { 
        val pathProvider = get<PlatformPathProvider>()
        createDataStore { pathProvider.getDataStorePath("app_prefs") } 
    }
    single<SessionManager> { EncryptedSessionManager(get()) }
    
    single<ExampleRepository> { ExampleRepositoryImpl() }
}
