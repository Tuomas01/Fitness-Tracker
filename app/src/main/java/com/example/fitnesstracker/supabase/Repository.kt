package com.example.fitnesstracker.supabase

import com.example.fitnesstracker.ui.screens.profile.User
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*interface UserRepository {
    suspend fun getUsers(): List<User>?
    suspend fun getUser(id: String): User
}

class UserRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : UserRepository {

    override suspend fun getUsers(): List<User>? {
        return withContext(Dispatchers.IO) {
            val result = postgrest.from("User")
                .select().decodeList<User>()
            result
        }
    }

    override suspend fun getUser(id: String): User {
        return withContext(Dispatchers.IO) {
            postgrest.from("User").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<User>()
        }
    }
}*/