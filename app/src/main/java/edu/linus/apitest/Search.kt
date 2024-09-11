package edu.linus.apitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Search : AppCompatActivity() {
    lateinit var searchResults: LinearLayout
    private fun getGeocodeByName(searchText: String, cordList: ArrayList<HashMap<String, String>>, db: FirebaseFirestore, userId: String) {
// Instantiate the cache
        val key = BuildConfig.LOCATIONIQ_KEY
        val cache = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap

        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network).apply {
            start()
        }

        val url = "https://us1.locationiq.com/v1/search?q=$searchText&format=json&key=$key"

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                if (response.length() > 0) {
                    searchResults.removeAllViews()
                    for (i in 0..<response.length()) {
                        val location = response.getJSONObject(i);
                        //Get if it has previously been defined
                        val lat = location.getDouble("lat");
                        val long = location.getDouble("lon");
                        val exists = cordList.find { cords ->
                            cords.getOrDefault("lat", "0").toDouble().equals(lat) && cords.getOrDefault("long", "0").toDouble().equals(long)
                        }
                        UtilFuncs.getWeatherData(this, lat, long, searchResults, exists == null, db, userId, false)
                        Log.i("TAG", "TAGGGG ADDING VIEW ")
                    }
                } else {
                    //We have no results

                }

            },
            { error ->
                // TODO: Handle error
                Log.e("LINERR", "makeJsonReq: ", error)
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = Firebase.firestore
        val sharedPref = getSharedPreferences(getString(R.string.storage_key), Context.MODE_PRIVATE)
        searchResults=findViewById(R.id.searchResults)
        val searchField = findViewById<EditText>(R.id.search)
        //Get all coords by this user
        findViewById<Button>(R.id.searchBtn).setOnClickListener {
            //Search
            val text = searchField.text
            val username = sharedPref.getString(getString(R.string.storage_key), null)
            db.collection("users").whereEqualTo("name", username)
                .get()
                .addOnSuccessListener { documents ->
                    var cordList = ArrayList<HashMap<String, String>>()
                    if (documents.size() > 0){
                        val first = documents.first()

                        if (first !== null) {
                            //We have a document
                            cordList = first.data["favorties"] as ArrayList<HashMap<String, String>>
                            getGeocodeByName(text.toString(), cordList, db, first.id)
                        }
                    } else {
                        val firestoreUser = hashMapOf(
                            "name" to username,
                            "favorties" to cordList

                        )
                        //We need to add it
                        db.collection("users")
                            .add(firestoreUser)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Test", "DocumentSnapshot added with ID: ${documentReference.id}")
                                getGeocodeByName(text.toString(), cordList, db, documentReference.id)
                            }
                            .addOnFailureListener { e ->
                                Log.w("Test", "Error adding document", e)
                            }
                    }


                }
        }






    }
}