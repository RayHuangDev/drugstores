package com.jarvislin.drugstores.data

import android.content.Context
import com.jarvislin.domain.entity.Proclamation
import com.jarvislin.drugstores.data.Preferences.Companion.PREFERENCE_NAME
import com.jarvislin.drugstores.extension.toList
import java.util.*

class LocalData(private val context: Context) {
    companion object {
        private const val KEY_LAST_LOCATION = "key_last_location"
        private const val KEY_LAST_REPORT_TIMESTAMP = "key_last_report_timestamp"
        private const val KEY_PROCLAMATIONS = "key_proclamations"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_RATING_COUNT = "key_rating_count"
    }

    val proclamations: List<Proclamation>
        get() = proclamationsText.toList()

    var lastLocation: String by preferences(KEY_LAST_LOCATION, "25.0393868,121.5087163")
    var lastReportTimestamp: Long by preferences(KEY_LAST_REPORT_TIMESTAMP, 0L)
    var proclamationsText: String by preferences(KEY_PROCLAMATIONS, "[]")
    var isFirstLaunch: Boolean by preferences(KEY_FIRST_LAUNCH, true)
    var ratingCount: Int by preferences(KEY_RATING_COUNT, 0)


    private fun <T : Any> preferences(name: String, defaultValue: T) =
        DelegatesExt.preferences(name, defaultValue, context)

    fun clear() {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }
}