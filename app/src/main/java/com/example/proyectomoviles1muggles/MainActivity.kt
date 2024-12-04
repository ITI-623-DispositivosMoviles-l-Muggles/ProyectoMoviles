package com.example.proyectomoviles1muggles

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectomoviles1muggles.adapters.InvestigationAdapter
import com.example.proyectomoviles1muggles.clase.Investigation
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerAuthor: Spinner
    private lateinit var spinnerArea: Spinner
    private lateinit var spinnerCycle: Spinner
    private lateinit var spinnerOrder: Spinner
    private lateinit var investigationList: MutableList<Investigation>
    private lateinit var filteredList: MutableList<Investigation>  // Lista filtrada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización del RecyclerView y Spinners
        recyclerView = findViewById(R.id.recycler_view)
        spinnerAuthor = findViewById(R.id.spinner_author)
        spinnerArea = findViewById(R.id.spinner_area)
        spinnerCycle = findViewById(R.id.spinner_cycle)
        spinnerOrder = findViewById(R.id.spinner_order)

        // Configurar RecyclerView (con LayoutManager y Adapter)
        recyclerView.layoutManager = LinearLayoutManager(this)
        investigationList = mutableListOf()
        filteredList = mutableListOf()

        // Obtener los datos de Firebase
        val db = FirebaseFirestore.getInstance()

        // Cargar las investigaciones
        db.collection("Pruebas")
            .get()
            .addOnSuccessListener { result ->
                val Correo = mutableSetOf("Todos")
                val areas = mutableSetOf("Todos")
                val cycles = mutableSetOf("Todos")
                for (document in result) {
                    val investigation = document.toObject(Investigation::class.java)
                    investigationList.add(investigation)

                    // Agregar a los sets para obtener valores únicos
                    Correo.add(investigation.Correo)
                    areas.add(investigation.area)
                    cycles.add(investigation.ciclo)
                }

                // Llenar los Spinners con los valores únicos obtenidos
                setupSpinner(spinnerAuthor, Correo.toList())
                setupSpinner(spinnerArea, areas.toList())
                setupSpinner(spinnerCycle, cycles.toList())

                // Ordenar las investigaciones por fecha de publicación (más reciente o más antiguo)
                setupOrderSpinner()

                // Establecer el adaptador con la lista completa
                filteredList.addAll(investigationList)  // Inicialmente, mostrar todas las investigaciones
                recyclerView.adapter = InvestigationAdapter(filteredList)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al cargar datos", e)
            }

        // Filtros de los Spinners
        spinnerAuthor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) {}

            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedAuthor = spinnerAuthor.selectedItem.toString()
                applyFilters(selectedAuthor, spinnerArea.selectedItem.toString(), spinnerCycle.selectedItem.toString())
            }
        }

        spinnerArea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) {}

            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedArea = spinnerArea.selectedItem.toString()
                applyFilters(spinnerAuthor.selectedItem.toString(), selectedArea, spinnerCycle.selectedItem.toString())
            }
        }

        spinnerCycle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) {}

            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val selectedCycle = spinnerCycle.selectedItem.toString()
                applyFilters(spinnerAuthor.selectedItem.toString(), spinnerArea.selectedItem.toString(), selectedCycle)
            }
        }

        spinnerOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parentView: AdapterView<*>) {}

            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                val sortedList = when (position) {
                    0 -> filteredList.sortedByDescending { it.publicationDate }  // Más reciente
                    1 -> filteredList.sortedBy { it.publicationDate }  // Más antiguo
                    else -> filteredList
                }

                recyclerView.adapter = InvestigationAdapter(sortedList)
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, values: List<String>) {
        // Crear un ArrayAdapter para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupOrderSpinner() {
        val orderOptions = listOf("Más reciente", "Más antiguo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, orderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrder.adapter = adapter
    }

    private fun applyFilters(selectedAuthor: String, selectedArea: String, selectedCycle: String) {
        filteredList.clear()

        filteredList.addAll(
            investigationList.filter { investigation ->
                // Filtrar por autor
                val authorFilter = selectedAuthor == "Todos" || investigation.Correo == selectedAuthor
                // Filtrar por área
                val areaFilter = selectedArea == "Todos" || investigation.area == selectedArea
                // Filtrar por ciclo
                val cycleFilter = selectedCycle == "Todos" || investigation.ciclo == selectedCycle

                authorFilter && areaFilter && cycleFilter
            }
        )

        // Actualizar el RecyclerView con los resultados filtrados
        recyclerView.adapter = InvestigationAdapter(filteredList)
    }
}



