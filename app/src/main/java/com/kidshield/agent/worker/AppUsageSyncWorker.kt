package com.kidshield.agent.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.kidshield.agent.data.repository.KidShieldRepository
import com.kidshield.agent.utils.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class AppUsageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: KidShieldRepository,
    private val networkUtils: NetworkUtils
) : CoroutineWorker(applicationContext = context, params = params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!networkUtils.isOnline()) {
            return@withContext Result.retry()
        }
        try {
            repository.syncAppUsage()
            repository.syncScreenTime()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AppUsageSyncWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "app_usage_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
