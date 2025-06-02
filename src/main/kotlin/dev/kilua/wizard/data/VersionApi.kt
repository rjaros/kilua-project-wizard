package dev.kilua.wizard.data

import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import dev.kilua.wizard.data.model.VersionData

interface VersionApi {

    @GET("versions.json")
    fun getVersionData(): Single<VersionData>

    companion object {
        fun create(): VersionApi {
            return Retrofit.Builder()
                    .baseUrl("https://raw.githubusercontent.com/rjaros/kilua/main/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                    .create(VersionApi::class.java)
        }
    }
}