package com.fleetpulse.notifications.di

import com.fleetpulse.notifications.config.DatabaseConfig
import com.fleetpulse.notifications.repository.NotificationRepository
import com.fleetpulse.notifications.service.NotificationService
import org.koin.dsl.module

fun appModule(mongoUri: String) = module {
    single { DatabaseConfig(mongoUri) }
    single { NotificationRepository(get<DatabaseConfig>().notifications) }
    single { NotificationService(get()) }
}
