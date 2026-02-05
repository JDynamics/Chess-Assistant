package com.chessassistant.di

import android.content.Context
import com.chessassistant.engine.StockfishEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing chess engine dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideStockfishEngine(
        @ApplicationContext context: Context
    ): StockfishEngine {
        return StockfishEngine(context)
    }
}
