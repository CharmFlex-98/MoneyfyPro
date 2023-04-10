package com.example.moneyfypro.di

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.moneyfypro.data.Expense
import com.example.moneyfypro.data.ExpensesDatabase
import com.example.moneyfypro.data.ExpensesRepository
import com.example.moneyfypro.data.ExpensesRepositoryImp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    /**
     * The context is provided by creating an Hilt specific annotated subclass of Application
     * This method provide singleton instance of ExpensesDatabase
     * The ExpensesDatabase has a getDao() method which return DAO object.
     */
    @Provides
    @Singleton
    fun provideExpensesDatabase(appContext: Application): ExpensesDatabase {
        return Room.databaseBuilder(
            appContext,
            ExpensesDatabase::class.java,
            ExpensesDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    /**
     * We can pass in the database to get the DAO object.
     * DAO generated automatically by ROOM.
     * The DAO object need to be passed into the repository for accessing data source.
     */
    @Provides
    @Singleton
    fun provideExpensesRepository(database: ExpensesDatabase): ExpensesRepository {
        return ExpensesRepositoryImp(database.getDao())
    }
}