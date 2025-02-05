package com.agvahealthcare.ventilator_ext.logs.trends

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.agvahealthcare.ventilator_ext.model.DataStoreModel

class DataBasePagingSource : PagingSource<Int, DataStoreModel>() {
    override fun getRefreshKey(state: PagingState<Int, DataStoreModel>): Int? {
        TODO("Not yet implemented")
    }

    //The load() function will be called by the Paging library to asynchronously
    // fetch more data to be displayed as the user scrolls around
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataStoreModel> {
        TODO("Not yet implemented")
    }

}