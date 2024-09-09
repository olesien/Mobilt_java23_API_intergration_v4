package edu.linus.apitest

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class Home : AppCompatActivity() {
    private fun makeReq() {
        /* Volley example */
        val url = "https://italian-jokes.vercel.app/api/jokes";

        val rq: RequestQueue = Volley.newRequestQueue( /* context = */ this)

        var request = StringRequest( Request.Method.GET , url,
            { res -> Log.d("Linus",res.toString())  } ,
            { err ->  Log.e("Linus",err.toString()) }
        )

        rq.add(request)
    }

    private fun makeJsonReq(textView: TextView) {
// Instantiate the cache
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        val url = "https://italian-jokes.vercel.app/api/jokes"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                textView.text = "Response: %s".format(response.get("joke").toString())
            },
            { error ->
                // TODO: Handle error
                Log.e("LINERR", "makeJsonReq: ", error)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val key = BuildConfig.WEATHER_KEY
        Log.i("LINUS", "APIKEY: $key")
    }
}