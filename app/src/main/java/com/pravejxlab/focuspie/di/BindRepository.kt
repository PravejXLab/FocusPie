package com.pravejxlab.focuspie.di

import com.pravejxlab.focuspie.data.HostNearbyConnectionRepositoryImpl
import com.pravejxlab.focuspie.data.StudentNearbyConnectionRepositoryImpl
import com.pravejxlab.focuspie.domain.HostNearbyConnectionRepository
import com.pravejxlab.focuspie.domain.StudentNearbyConnectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindRepository {

    @Binds
    @Singleton
    abstract fun bindHostNearbyConnectionRepository(
        impl: HostNearbyConnectionRepositoryImpl
    ): HostNearbyConnectionRepository

    @Binds
    @Singleton
    abstract fun bindStudentConnectionRepository(
        impl: StudentNearbyConnectionRepositoryImpl
    ): StudentNearbyConnectionRepository
}