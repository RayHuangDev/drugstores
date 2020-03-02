package com.jarvislin.drugstores.page.map

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.maps.model.LatLng
import com.jarvislin.domain.entity.DrugstoreInfo
import com.jarvislin.domain.entity.Progress
import com.jarvislin.domain.interactor.DrugstoreUseCase
import com.jarvislin.drugstores.base.App
import com.jarvislin.drugstores.extension.bind
import com.jarvislin.drugstores.base.BaseViewModel
import com.jarvislin.drugstores.data.db.DrugstoreDao
import com.jarvislin.drugstores.data.remote.HttpException
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.inject
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MapViewModel : BaseViewModel() {
    private val useCase: DrugstoreUseCase by inject()
    val dataPrepared = MutableLiveData<Boolean>(false)
    val drugstoreInfo = MutableLiveData<List<DrugstoreInfo>>(emptyList())
    val downloadProgress = MutableLiveData<Progress>()
    val autoUpdate = MutableLiveData<Boolean>()
    val searchedResult = MutableLiveData<List<DrugstoreInfo>>()
    val statusBarHeight = MutableLiveData<Int>()
    val ad = MutableLiveData<UnifiedNativeAd>()

    fun fetchOpenData() {
        useCase.fetchData()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ downloadProgress.value = it }, {
                when (it) {
                    is IOException -> toastText.postValue("無法連線，請檢查網路狀況")
                    is HttpException -> toastText.postValue("連線錯誤，請稍後再試")
                    else -> toastText.postValue("發生異常，請聯絡作者")
                }
                dataPrepared.value = false
                Timber.e(it)
            })
            .bind(this)
    }

    fun fetchNearDrugstoreInfo(latitude: Double, longitude: Double) {
        if (dataPrepared.value != true) {
            // from on camera idled
            return
        }
        useCase.findNearDrugstoreInfo(latitude, longitude)
            .subscribe({ drugstoreInfo.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveLastLocation(location: Location?) {
        location?.let { useCase.saveLastLocation(it.latitude, it.longitude) }
    }

    fun getLastLocation(): LatLng {
        return useCase.getLastLocation().run { LatLng(first, second) }
    }

    fun handleLatestOpenData(file: File) {
        useCase.handleLatestData(file)
            .subscribe({ dataPrepared.postValue(true) }, { Timber.e(it) })
            .bind(this)
    }

    fun countDown() {
        Flowable.interval(1, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.computation())
            .take(120)
            .doOnComplete {
                Timber.i("count down complete")
                autoUpdate.postValue(true)
            }
            .subscribe({ }, { Timber.e(it) })
            .bind(this)
    }

    fun searchAddress(keyword: String) {
        useCase.searchAddress(keyword)
            .delay(600L, TimeUnit.MILLISECONDS) // for showing progress bar
            .subscribe({ searchedResult.postValue(it) }, { Timber.e(it) })
            .bind(this)
    }

    fun saveStatusBarHeight(height: Int) {
        statusBarHeight.postValue(height)
    }

    fun requestAd(adId: String, location: Location?) {
        val videoOptions = VideoOptions.Builder()
            .setClickToExpandRequested(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        val adBuilder = AdRequest.Builder()
            .addTestDevice("94AAY0LJFG")

        location?.let { adBuilder.setLocation(it) }
        AdLoader.Builder(App.instance(), adId)
            .forUnifiedNativeAd {
                ad.value?.destroy()
                ad.postValue(it)
            }
            .withNativeAdOptions(adOptions)
            .build()
            .loadAd(adBuilder.build())
    }
}