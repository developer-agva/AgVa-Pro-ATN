package com.agvahealthcare.ventilator_ext.logs.trends

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.agvahealthcare.ventilator_ext.model.DataStoreModel


private const val STARTING_KEY = 0

class PagingSourceDataBase : PagingSource<Int, DataStoreModel>() {


    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DataStoreModel> {
        // Start paging with the STARTING_KEY if this is the first load
        val start = params.key ?: STARTING_KEY

        TODO("Not yet implemented")
    }
    override fun getRefreshKey(state: PagingState<Int, DataStoreModel>): Int? {
        TODO("Not yet implemented")
    }


}