package com.example.ipscannerk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ipscannerk.interactor.OnOUIDataLoaded
import com.example.ipscannerk.model.local.VendorInfo
import com.example.ipscannerk.viewmodel.SplashViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException
import java.io.InputStream


class SplashScreenActivity : AppCompatActivity(), OnOUIDataLoaded {
    private lateinit var viewModel: SplashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        //do update the SQLite OUI Database
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        viewModel.isDatabaseEmpty()?.observe(this, Observer {
            if (it == null) {
                Thread(Runnable {
                    val vendorInfoList: MutableList<VendorInfo> = ArrayList()
                    //read the json files
                    val obj: JsonObject =
                        JsonParser.parseString(loadJSONFromAsset()).asJsonObject
                    val entrySet: Set<Map.Entry<String?, JsonElement>> =
                        obj.entrySet()
                    for ((key, value) in entrySet) {
                        vendorInfoList.add(
                            VendorInfo(
                                key!!,
                                value.asJsonObject.get("vendor").getAsString()
                            )
                        )
                    }
                    viewModel.loadOuiData(vendorInfoList, this@SplashScreenActivity)
                }).start()
                Toast.makeText(this, "DB is Empty, Load the Data", Toast.LENGTH_SHORT).show()
                Log.e("this", "DB is Empty, Load the Data")
            } else {
                Toast.makeText(this, "DB already available", Toast.LENGTH_SHORT).show()
                Log.e("this", "DB already available")
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            }
        })

//        viewModel.loadOuiData(this@SplashScreenActivity)
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = assets.open("oui.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    override fun onOuiDataLoadedListener() {
        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
    }
}
