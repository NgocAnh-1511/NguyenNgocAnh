package com.example.myapplication
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val findBeerButton = findViewById<Button>(R.id.find_beer)
        val beerColorSpinner = findViewById<Spinner>(R.id.beer_color)
        val brandsTextView = findViewById<TextView>(R.id.brands)

        findBeerButton.setOnClickListener {
            val color = beerColorSpinner.selectedItem.toString()

            val beerList = getBeers(color)

            val beers = beerList.joinToString(separator = "\n")

            brandsTextView.text = beers
        }
    }

    fun getBeers(color: String): List<String> {
        return when (color) {
            "Light" -> listOf("Jail Pale Ale", "Lager Lite")
            "Amber" -> listOf("Jack Amber", "Red Moose")
            "Brown" -> listOf("Brown Bear Beer", "Bock Brownie")
            else -> listOf("Gout Stout", "Dark Daniel")
        }
    }
}
