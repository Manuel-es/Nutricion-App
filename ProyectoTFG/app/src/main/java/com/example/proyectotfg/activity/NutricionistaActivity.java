package com.example.proyectotfg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.proyectotfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NutricionistaActivity extends AppCompatActivity {

    private ListView lvPacientes;
    private FirebaseFirestore db;
    private List<DocumentSnapshot> listaDocumentosPacientes;
    private List<String> listaNombresMostrar;
    private ArrayAdapter<String> adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutricionista);

        Toolbar toolbar = findViewById(R.id.toolbarNutri);
        setSupportActionBar(toolbar);
        toolbar.setSubtitle("Cerrar Sesión");
        toolbar.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(NutricionistaActivity.this, Login.class)); // Cambia "Login.class" por tu actividad de login real si se llama distinto
            finish();
        });

        lvPacientes = findViewById(R.id.lvPacientes);
        db = FirebaseFirestore.getInstance();
        listaDocumentosPacientes = new ArrayList<>();
        listaNombresMostrar = new ArrayList<>();

        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaNombresMostrar);
        lvPacientes.setAdapter(adaptador);

        obtenerPacientesDeLaNube();

        lvPacientes.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot pacienteSeleccionado = listaDocumentosPacientes.get(position);
            mostrarFichaPaciente(pacienteSeleccionado);
        });
    }

    private void obtenerPacientesDeLaNube() {
        db.collection("usuarios")
                .whereEqualTo("rol", "usuario")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaDocumentosPacientes.clear();
                    listaNombresMostrar.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        listaDocumentosPacientes.add(doc);

                        String nombre = doc.getString("nombre");
                        String email = doc.getString("email");
                        String objetivo = doc.getString("objetivo");

                        if (nombre == null) nombre = "Usuario Anónimo";

                        listaNombresMostrar.add("👤 " + nombre + " (" + objetivo + ")\n✉️ " + email);
                    }
                    adaptador.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al sincronizar pacientes", Toast.LENGTH_SHORT).show());
    }

    private void mostrarFichaPaciente(DocumentSnapshot doc) {
        String nombre = doc.getString("nombre");
        String objetivo = doc.getString("objetivo");
        Long kcalObj = doc.getLong("caloriasDiarias");
        Long protObj = doc.getLong("macroProteinaObj");
        Long carbObj = doc.getLong("macroCarbosObj");
        Long grasObj = doc.getLong("macroGrasaObj");

        Long kcalCons = doc.getLong("caloriasConsumidasHoy");

        String fichaInformativa = "🎯 Objetivo: " + (objetivo != null ? objetivo : "No definido") + "\n\n"
                + "🔥 Gasto Objetivo: " + (kcalObj != null ? kcalObj : 0) + " kcal / día\n"
                + "📥 Consumidas Hoy: " + (kcalCons != null ? kcalCons : 0) + " kcal\n\n"
                + "📊 Reparto Macro Estructural:\n"
                + " • Proteínas: " + (protObj != null ? protObj : 0) + "g\n"
                + " • Carbohidratos: " + (carbObj != null ? carbObj : 0) + "g\n"
                + " • Grasas: " + (grasObj != null ? grasObj : 0) + "g";

        new AlertDialog.Builder(this)
                .setTitle("Ficha Clínica: " + nombre)
                .setMessage(fichaInformativa)
                .setPositiveButton("Volver", null)
                .show();
    }
}