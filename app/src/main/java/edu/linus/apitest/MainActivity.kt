package edu.linus.apitest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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


class MainActivity : AppCompatActivity() {
    private lateinit var sharedPref: SharedPreferences

    private fun login () {
        val myIntent = Intent(
            this,
            Home::class.java
        )
        startActivity(myIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tv = findViewById<TextView>(R.id.tv)
        val field = findViewById<EditText>(R.id.name)
        val btn = findViewById<Button>(R.id.login)

        sharedPref = getSharedPreferences(getString(R.string.storage_key), Context.MODE_PRIVATE)

        btn.setOnClickListener {
            //Login
            with(sharedPref.edit()) {
                putString(getString(R.string.storage_key), field.text.toString())
                apply()
                login()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val userName =sharedPref.getString(getString(R.string.storage_key), null);
        if (userName != null) {
            //Login
            login()
        }
    }
}