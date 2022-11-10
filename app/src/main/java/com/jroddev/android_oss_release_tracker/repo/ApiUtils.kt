package com.jroddev.android_oss_release_tracker.repo

import arrow.core.Either
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ApiUtils {

    suspend fun get(url: String, requestQueue: RequestQueue) = suspendCoroutine<Either<String, VolleyError>> { cont ->
        println("get: $url")
        requestQueue.add(StringRequest(
            Request.Method.GET, url,
            { response -> cont.resume(Either.Left(response)) },
            { error -> cont.resume(Either.Right(error)) }
        ))
    }

    suspend fun getJsonArray(url: String, requestQueue: RequestQueue) = suspendCoroutine<Either<JSONArray, VolleyError>> { cont ->
        println("getJsonArray: $url")
        requestQueue.add(JsonArrayRequest(
            Request.Method.GET, url, null,
            { response -> cont.resume(Either.Left(response)) },
            { error -> cont.resume(Either.Right(error)) }
        ))
    }

    suspend fun getJsonObject(url: String, requestQueue: RequestQueue) = suspendCoroutine<Either<JSONObject, VolleyError>> { cont ->
        println("getJsonArray: $url")
        requestQueue.add(JsonObjectRequest(
            Request.Method.GET, url, null,
            { response -> cont.resume(Either.Left(response)) },
            { error -> cont.resume(Either.Right(error)) }
        ))
    }

}