package com.jarvislin.drugstores.module

import com.jarvislin.drugstores.page.detail.DetailViewModel
import com.jarvislin.drugstores.page.map.MapViewModel
import com.jarvislin.drugstores.page.news.NewsViewModel
import com.jarvislin.drugstores.page.proclamation.ProclamationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MapViewModel() }
    viewModel { DetailViewModel() }
    viewModel { ProclamationViewModel() }
    viewModel { NewsViewModel() }
}