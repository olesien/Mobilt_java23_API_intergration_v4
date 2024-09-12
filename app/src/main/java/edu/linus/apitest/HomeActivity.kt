package edu.linus.apitest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeActivity : AppCompatActivity() {
    // declare a global variable of FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var cityText: TextView
    private lateinit var tempText: TextView
    private lateinit var humidityText: TextView
    private lateinit var weatherText: TextView
    private lateinit var sharedPref: SharedPreferences

    private fun getWeatherHomeData(key: String, lat: Double, long: Double) {
    // Instantiate the cache
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        val url = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=$lat&lon=$long&appid=$key"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                //textView.text = "Response: %s".format(response.get("joke").toString())
                Log.i("TAG", response.toString())

                //Render
                val weather = response.getJSONArray("weather")
                val main = response.getJSONObject("main")
                val sys = response.getJSONObject("sys")
                if (weather.length() > 0) {
                    //Get first
                    val townWeather = weather.getJSONObject(0);
                    if (townWeather != null) {
                        cityText.text =
                            getString(R.string.city, response.get("name"), sys.get("country"))
                        tempText.text =
                            getString(R.string.temp, main.getString("temp"))
                        humidityText.text =
                            getString(R.string.humidity, main.getString("humidity"))
                        weatherText.text =
                            getString(R.string.weather, townWeather.getString("description"))
                    }


                }
            },
            { error ->
                // TODO: Handle error
                Log.e("LINERR", "makeJsonReq: ", error)
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun getData(location: Location?) {
        val key = BuildConfig.WEATHER_KEY
        //val geoCoder = Geocoder(this, Locale.getDefault())
        if (location != null) {
            // use your location object
            // get latitude , longitude and other info from this
            Log.i("TAG", "LINUS -> $location")
            getWeatherHomeData(key, location.latitude, location.longitude)
        } else {
            Log.i("TAG", "LINUS -> Location is null")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPref = getSharedPreferences(getString(R.string.storage_key), Context.MODE_PRIVATE)
        cityText = findViewById(R.id.city)
        tempText = findViewById(R.id.temp)
        humidityText = findViewById(R.id.humidity)
        weatherText = findViewById(R.id.weather)

        //Set username
        findViewById<TextView>(R.id.loggedInAs).text =
            "Logged in as: ${sharedPref.getString(getString(R.string.storage_key), null)}"

        // in onCreate() initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Try again!
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location->
                            getData(location)
                        }
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied
                    Toast.makeText(this, "You have previously said no to access for this app on fine location, please check settings and re-enable to continue.", Toast.LENGTH_LONG).show()
                }
            }.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        }

        //After above is done, set listener.
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                getData(location)
            }
    }
    //Auto logout
    override fun onStart() {
        super.onStart()

        val userName =sharedPref.getString(getString(R.string.storage_key), null);
        if (userName == null) {
            val intent = Intent(this, MainActivity::class.java)
            this.startActivity(intent)
        }
    }
}