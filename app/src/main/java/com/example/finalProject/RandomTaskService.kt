package com.example.finalProject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RandomTaskService {

    // Requesting a random User info based on specified  nationality, https://randomuser.me/api/?nat=us
    // Adding the nationality parameter to your request
    // get(".") indicates that your url is the same as the base url
    @GET(".")
    fun getUserInfo(@Query("nat") nationality: String) : Call<Task>

    // Requesting Multiple Users, https://randomuser.me/api/?results=5000
    @GET(".")
    fun getMultipleUserInfo(@Query("results") amount: Int) : Call<Task>


    // Requesting Multiple Users, https://randomuser.me/api/?results=5000&nat=us
    //http://www.boredapi.com/api/activity?type=recreational
    @GET(".")
    fun getTaskType(@Query("type") type: String): Call<Task>

}