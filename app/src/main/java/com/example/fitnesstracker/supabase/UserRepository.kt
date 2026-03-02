package com.example.fitnesstracker.supabase

import android.util.Log
import com.example.fitnesstracker.ui.screens.profile.User
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

// UserRepository interface that is used when the app wants to interact with the database
interface UserRepository {
    suspend fun getUsers(): List<User>?
    suspend fun getUser(email: String): User
}

// Implementation for UserRepository.
// All the user related database interactions will happen here
class UserRepositoryImpl @Inject constructor(
    // Initialized in SupabaseClient.kt
    private val postgrest: Postgrest,
) : UserRepository {

    // Gets a list of all the users
    override suspend fun getUsers(): List<User>? {
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("User")
                .select().decodeList<User>()
            result
        }
    }

    // Get a single user by email that will be passed to the functions as an argument
    override suspend fun getUser(email: String): User {
        try {
            return withContext(Dispatchers.IO) {
                val result = postgrest.from("User")
                    .select(columns = Columns.ALL) {
                        // Filter block to only get the user with given email
                        // In this case the given email is the logged in user's email
                        filter {
                            eq("email", email)
                        }
                    }
                Log.d("UserRepo", "getUser test: $result \n decoding: ${result.decodeSingle<User>()}")
                result.decodeSingle()
            }
        } catch (e: Exception) {
            Log.d("UserRepo", "getUser() error: $e")
        }
        // Returns a user with default values from the user data class if there were errors
        return User()
    }
}

// Create a new Module and scope it to the whole application using the SingletonComponent
@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {

    // Binds the UserRepository so that it can be injected into a constructor
    // Takes the implementation of the repository as a parameter
    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}