package edu.linus.apitest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Favorties : AppCompatActivity() {
    lateinit var favResults: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorties)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = Firebase.firestore
        val sharedPref = getSharedPreferences(getString(R.string.storage_key), Context.MODE_PRIVATE)
        favResults=findViewById(R.id.favResults)
        val username = sharedPref.getString(getString(R.string.storage_key), null)

        //Get the user and its favorites
        db.collection("users").whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0){
                    val first = documents.first()

                    if (first !== null) {
                        //We have a document
                        favResults.removeAllViews()
                        val cordList = first.data["favorties"] as ArrayList<HashMap<String, String>>
                        cordList.forEach { cords ->
                            val lat = cords.getOrDefault("lat", "0").toDouble()
                            val long = cords.getOrDefault("long", "0").toDouble()
                            UtilFuncs.getWeatherData(
                                this,
                                lat,
                                long,
                                favResults,
                                false,
                                db,
                                first.id,
                                true
                            )

                        }
                    }
                }
        }
    }
}