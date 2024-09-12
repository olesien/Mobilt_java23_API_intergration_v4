package edu.linus.apitest

import android.content.Context
import android.content.res.Resources.Theme
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class UtilFuncs {
    companion object {
        fun getWeatherData(context: Context, lat: Double, long: Double, results: LinearLayout, isAdd: Boolean, db: FirebaseFirestore, userId: String, fromFav: Boolean) {
            val key = BuildConfig.WEATHER_KEY
            val cache = DiskBasedCache(context.cacheDir, 1024 * 1024) // 1MB cap

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
                            generateList(context, results, response.getString("name"),sys.getString("country"), main.getString("humidity"),main.getString("temp"), townWeather.getString("description"), isAdd, db, userId, lat, long, fromFav )
                            //Originally I had intended to make each one its own fragment, thinking of it as a component
                            //But after looking online I decided this was not a good idea.
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
        fun generateList(context: Context, results: LinearLayout, city: String, country: String, humidity: String, temp: String, description: String, isAdd: Boolean, db: FirebaseFirestore, userId: String, lat: Double, long: Double, fromFav: Boolean) {
            //Create a card view
            val card = CardView(context)
            card.apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8) // Margin for CardView
                }
                radius = 4f
                elevation = 4f
            }

            //Make the vertical linearlayout (which holds content for cardview)
            val cardContentLayout = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL

                setPadding(16, 16, 16, 16)
            }

            //The two constraint layouts
            val constraintLayout1 = ConstraintLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(10, 10, 10, 10)
            }

            //-> content inside of constraint layout
            val cityText = TextView(context).apply {
                id = View.generateViewId() //Auto generate the ID
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                text = context.getString(R.string.city, city, country)
            }

            val humidityText = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = context.getString(R.string.humidity, humidity)
            }

            val btn = MaterialButton(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16) // Margin for CardView

                }
                text = if (isAdd) "Add" else "Remove"
            }

            var isAddBtn = isAdd; //Duplicate so we can change later

            btn.setOnClickListener {

                //Get latest cords
                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val updatedCordsList = document.data?.get("favorties") as ArrayList<HashMap<String, String>>
                        Log.i("TAG", if (isAddBtn) "Adding to favorites" else "Removing from favorites")
                        btn.text = if (isAddBtn) "Remove" else "Add"
                        //Modify firebase
                        if (isAddBtn) {
                            val newCords = HashMap<String, String>();
                            newCords["lat"] = lat.toString()
                            newCords["long"] = long.toString()
                            updatedCordsList.add(newCords)
                        } else {
                            //Delete
                            updatedCordsList.remove(updatedCordsList.find{cords -> lat.toString() == cords["lat"] && long.toString() == cords["long"]})
                        }

                        db.collection("users").document(userId).update("favorties", updatedCordsList).apply {
                            addOnSuccessListener {
                                Log.i("TAG", "Successfully edited user")
                            }
                        }
                        isAddBtn = !isAddBtn; //Reverse

                        if (fromFav) {
                            //Here we want to straight up delete the row if we delete
                            (card.parent as LinearLayout).removeView(card)
                        }
                    }
                }

            }


            constraintLayout1.addView(cityText)
            constraintLayout1.addView(humidityText)
            constraintLayout1.addView(btn)
            ConstraintSet().apply {
                clone(constraintLayout1)

                // City TextView Constraints
                connect(cityText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
                connect(cityText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(cityText.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                connect(cityText.id, ConstraintSet.END, cityText.id, ConstraintSet.START, 16)

                // Humidity TextView Constraints
                connect(humidityText.id, ConstraintSet.START, cityText.id, ConstraintSet.END, 16)
                connect(humidityText.id, ConstraintSet.END, btn.id, ConstraintSet.START, 16)
                connect(humidityText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(humidityText.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                // Button Constraints
                connect(btn.id, ConstraintSet.START, humidityText.id, ConstraintSet.END, 16)
                connect(btn.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
                connect(btn.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(btn.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                // Distribute space based on what is needed
                setHorizontalBias(cityText.id, 0.7f)  // city
                setHorizontalBias(humidityText.id, 0.5f)  // humidity
                setHorizontalBias(btn.id, 0.3f)  // button

                applyTo(constraintLayout1)
            }

            val constraintLayout2 = ConstraintLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(10, 10, 10, 10)
            }

            //-> content inside of constraint layout
            val tempText = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                text = context.getString(R.string.temp, temp)
            }

            val weatherText = TextView(context).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = context.getString(R.string.weather, description)
            }

            constraintLayout2.addView(tempText)
            constraintLayout2.addView(weatherText)
            ConstraintSet().apply {
                clone(constraintLayout2)

                // Left
                connect(tempText.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16)
                connect(tempText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(tempText.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

                // Right
                connect(weatherText.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
                connect(weatherText.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                connect(weatherText.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)


                applyTo(constraintLayout2)
            }



            // Add the two constraints to the linearlayout
            cardContentLayout.addView(constraintLayout1)
            cardContentLayout.addView(constraintLayout2)

            // Add the vertical layout to card
            card.addView(cardContentLayout)

            // Add the card to results
            results.addView(card)
        }
    }
}