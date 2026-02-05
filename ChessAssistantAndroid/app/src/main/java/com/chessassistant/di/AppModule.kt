package com.chessassistant.di

import android.content.Context
import com.chessassistant.data.local.SettingsDataStore
import com.chessassistant.data.repository.ChessRepository
import com.chessassistant.data.repository.GameStorageRepository
import com.chessassistant.data.repository.SettingsRepository
import com.chessassistant.engine.StockfishEngine
import com.chessassistant.network.VisionAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing application-level dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): SettingsRepository {
        return SettingsRepository(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideGameStorageRepository(
        @ApplicationContext context: Context
    ): GameStorageRepository {
        return GameStorageRepository(context)
    }

    @Provides
    @Singleton
    fun provideChessRepository(
        stockfishEngine: StockfishEngine,
        visionAnalyzer: VisionAnalyzer
    ): ChessRepository {
        return ChessRepository(stockfishEngine, visionAnalyzer)
    }
}
