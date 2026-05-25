package com.example.proyectotfg.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.proyectotfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RecetasActivity extends AppCompatActivity {

    private TextView tvTituloObjetivo, tvMenuDesayuno, tvMenuAlmuerzo, tvMenuCena;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recetas);

        Toolbar toolbar = findViewById(R.id.toolbarRecetas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvTituloObjetivo = findViewById(R.id.tvTituloObjetivo);
        tvMenuDesayuno = findViewById(R.id.tvMenuDesayuno);
        tvMenuAlmuerzo = findViewById(R.id.tvMenuAlmuerzo);
        tvMenuCena = findViewById(R.id.tvMenuCena);

        obtenerObjetivoUsuario();
    }

    private void obtenerObjetivoUsuario() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String objetivoStr = documentSnapshot.getString("objetivo");
                        if (objetivoStr != null) {
                            tvTituloObjetivo.setText("Plan ideal para: " + objetivoStr);

                            String queryObjetivo = "mantener";
                            if (objetivoStr.toLowerCase().contains("perder")) queryObjetivo = "perder";
                            else if (objetivoStr.toLowerCase().contains("ganar") || objetivoStr.toLowerCase().contains("masa")) queryObjetivo = "ganar";

                            cargarRecetasFiltradas(queryObjetivo);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar tu perfil", Toast.LENGTH_SHORT).show());
    }

    private void cargarRecetasFiltradas(String objetivoFiltrado) {
        db.collection("recetas")
                .whereEqualTo("objetivo", objetivoFiltrado)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w("TFG_RECETAS", "No se encontraron recetas con el tag: " + objetivoFiltrado);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String tipo = doc.getString("tipo");
                        String titulo = doc.getString("titulo");
                        String detalles = doc.getString("detalles");

                        String textoFormateado = "🥣 " + titulo + "\n\n📝 Preparación e Ingredientes:\n" + detalles;

                        if (tipo != null) {
                            if (tipo.equalsIgnoreCase("Desayuno")) {
                                tvMenuDesayuno.setText(textoFormateado);
                            } else if (tipo.equalsIgnoreCase("Almuerzo")) {
                                tvMenuAlmuerzo.setText(textoFormateado);
                            } else if (tipo.equalsIgnoreCase("Cena")) {
                                tvMenuCena.setText(textoFormateado);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TFG_RECETAS", "Error en la consulta compuesta", e));
    }
}