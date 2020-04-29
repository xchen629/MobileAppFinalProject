package com.example.finalProject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomTaskService {
    // get(".") indicates that your url is the same as the base url
    //http://www.boredapi.com/api/activity?type=recreational
    @GET(".")
    fun getTask() : Call<Task>

    @GET(".")
    fun getTaskOfType(@Query("type") type: String) : Call<Task>
}