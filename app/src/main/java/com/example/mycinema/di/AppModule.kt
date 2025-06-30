// AppModule.kt - תיקון השגיאות
package com.example.mycinema.di

import android.content.Context
import com.example.mycinema.data.CinemaDao
import com.example.mycinema.data.MovieDao
import com.example.mycinema.data.MovieDatabase
import com.example.mycinema.network.MovieApiService
import com.example.mycinema.repository.CinemaRepository
import com.example.mycinema.repository.MovieRepository
import com.example.mycinema.services.LocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database provisions
    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase {
        return MovieDatabase.getDatabase(context)
    }

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    fun provideCinemaDao(database: MovieDatabase): CinemaDao {
        return database.cinemaDao()
    }

    // Network provisions
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MovieApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApiService(retrofit: Retrofit): MovieApiService {
        return retrofit.create(MovieApiService::class.java)
    }

    // Services
    @Provides
    @Singleton
    fun provideLocationService(@ApplicationContext context: Context): LocationService {
        return LocationService(context)
    }

    // Repository provisions
    @Provides
    @Singleton
    fun provideMovieRepository(
        movieDao: MovieDao,
        apiService: MovieApiService,
        @ApplicationContext context: Context
    ): MovieRepository {
        return MovieRepository(movieDao, apiService, context)
    }

    @Provides
    @Singleton
    fun provideCinemaRepository(
        cinemaDao: CinemaDao,
        locationService: LocationService,
        @ApplicationContext context: Context
    ): CinemaRepository {
        return CinemaRepository(cinemaDao, locationService, context)
    }
}