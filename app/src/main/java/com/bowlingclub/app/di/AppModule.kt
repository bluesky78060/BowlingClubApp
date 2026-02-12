package com.bowlingclub.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Repository bindings and other app-level dependencies will be added here as needed
}
