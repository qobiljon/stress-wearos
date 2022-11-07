package io.github.qobiljon.stressapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.room.Room
import io.github.qobiljon.etagent.R
import io.github.qobiljon.stressapp.core.data.AccData
import io.github.qobiljon.stressapp.core.data.AppDatabase
import io.github.qobiljon.stressapp.core.data.BVPData
import io.github.qobiljon.stressapp.core.data.OffBodyData
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object Storage {
    private const val KEY_PREFS_NAME = "shared_prefs"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_DATE_OF_BIRTH = "date_of_birth"

    private lateinit var db: AppDatabase

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(KEY_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun init(context: Context) {
        db = Room.databaseBuilder(context, AppDatabase::class.java, context.getString(R.string.room_db_name)).allowMainThreadQueries().build()
    }

    fun syncToCloud(context: Context) {
        if (!isAuthenticated(context)) return

        runBlocking {
            val accDataDao = db.accDataDao()
            launch {
                val allAccData = accDataDao.getAll()
                val success = Api.submitAccData(
                    context,
                    fullName = getFullName(context),
                    dateOfBirth = getDateOfBirth(context),
                    accData = allAccData,
                )
                if (success) allAccData.forEach { accDataDao.delete(it) }
            }

            val bvpDataDao = db.bvpDataDao()
            launch {
                val allBVPData = bvpDataDao.getAll()
                val success = Api.submitBVPData(
                    context,
                    fullName = getFullName(context),
                    dateOfBirth = getDateOfBirth(context),
                    bvpData = allBVPData,
                )
                if (success) allBVPData.forEach { bvpDataDao.delete(it) }
            }

            val offBodyDao = db.offBodyDataDao()
            launch {
                val allOffBodyData = offBodyDao.getAll()
                val success = Api.submitOffBodyData(
                    context,
                    fullName = getFullName(context),
                    dateOfBirth = getDateOfBirth(context),
                    offBodyData = allOffBodyData,
                )
                if (success) allOffBodyData.forEach { offBodyDao.delete(it) }
            }
        }
    }

    fun isAuthenticated(context: Context): Boolean {
        return getSharedPreferences(context).getString(KEY_FULL_NAME, null) != null && getSharedPreferences(context).getString(KEY_DATE_OF_BIRTH, null) != null
    }

    fun setFullName(context: Context, fullName: String) {
        getSharedPreferences(context).edit {
            putString(KEY_FULL_NAME, fullName)
        }
    }

    fun setDateOfBirth(context: Context, dateOfBirth: String) {
        getSharedPreferences(context).edit {
            putString(KEY_DATE_OF_BIRTH, dateOfBirth)
        }
    }

    private fun getFullName(context: Context): String {
        return getSharedPreferences(context).getString(KEY_FULL_NAME, null)!!
    }

    private fun getDateOfBirth(context: Context): String {
        return getSharedPreferences(context).getString(KEY_DATE_OF_BIRTH, null)!!
    }

    fun saveAccData(accData: AccData) {
        db.accDataDao().insertAll(accData)
    }

    fun saveBVPData(bvpData: BVPData) {
        db.bvpDataDao().insertAll(bvpData)
    }

    fun saveOffBodyData(offBodyData: OffBodyData) {
        db.offBodyDataDao().insertAll(offBodyData)
    }
}