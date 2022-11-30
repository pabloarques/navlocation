package com.example.navlocation.ui.API;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ValenBisiApi {
    @GET("valenbisi")
    Call<CitibikesResult> status();

}
