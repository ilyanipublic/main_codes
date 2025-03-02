package com.airprint.smart.printer.utils
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DataStoreApi {

    private val Context.dataStore by preferencesDataStore(name = "application")

    private val gson = Gson()
    private val HISTORY_LIST_KEY = stringPreferencesKey("history_list_key")

    data class HistoryModel(
        val scanText: String? = null,
        val scanDate: String? = null
    )

    private suspend fun <T> setPreference(
        context: Context,
        key: Preferences.Key<T>,
        value: T
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    private suspend fun setListPreference(
        context: Context,
        key: Preferences.Key<String>,
        list: List<HistoryModel>
    ) {
        val json = gson.toJson(list)
        setPreference(context, key, json)
    }

    fun getHistoryList(context: Context): Flow<List<HistoryModel>> =
        context.dataStore.data.map { preferences ->
            val json = preferences[HISTORY_LIST_KEY]
            if (json.isNullOrEmpty()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<HistoryModel>>() {}.type)
        }

    suspend fun addHistory(context: Context, newHistory: HistoryModel) {
        val currentList = getHistoryList(context).first()
        val updatedList = currentList + newHistory
        setListPreference(context, HISTORY_LIST_KEY, updatedList)
    }

    suspend fun deleteHistoryItem(context: Context, historyItem: HistoryModel) {
        val currentList = getHistoryList(context).first()
        val updatedList = currentList.toMutableList()
        val itemToRemove = updatedList.find {
            it.scanText == historyItem.scanText && it.scanDate == historyItem.scanDate
        }
        if (itemToRemove != null) {
            updatedList.remove(itemToRemove)
            setListPreference(context, HISTORY_LIST_KEY, updatedList)  // Save the updated list back
        }
    }


    suspend fun updateHistory(context: Context, updatedHistory: HistoryModel) {
        val currentList = getHistoryList(context).first()
        val updatedList = currentList.map {
            if (it.scanText == updatedHistory.scanText) updatedHistory else it
        }
        setListPreference(context, HISTORY_LIST_KEY, updatedList)
    }

    fun getHistoryByText(context: Context, scanText: String): Flow<HistoryModel?> {
        return getHistoryList(context).map { list ->
            list.find { it.scanText == scanText }
        }
    }

    suspend fun clearHistory(context: Context) {
        setListPreference(context, HISTORY_LIST_KEY, emptyList())
    }
}






