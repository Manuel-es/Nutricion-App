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
import java.util.Calendar;
import java.util.Locale;

public class EjerciciosActivity extends AppCompatActivity {

    private TextView tvDiaActual, tvObjetivoRutina, tvTituloRutina, tvDescripcionRutina, tvPostEntreno;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String diaDeHoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ejercicios);

        Toolbar toolbar = findViewById(R.id.toolbarEjercicios);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDiaActual = findViewById(R.id.tvDiaActual);
        tvObjetivoRutina = findViewById(R.id.tvObjetivoRutina);
        tvTituloRutina = findViewById(R.id.tvTituloRutina);
        tvDescripcionRutina = findViewById(R.id.tvDescripcionRutina);
        tvPostEntreno = findViewById(R.id.tvPostEntreno);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        calcularDiaActual();
        obtenerObjetivoUsuario();
    }

    private void calcularDiaActual() {
        Calendar calendar = Calendar.getInstance();
        String nombreDia = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale("es", "ES"));
        if (nombreDia != null && !nombreDia.isEmpty()) {
            diaDeHoy = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1);
        } else {
            diaDeHoy = "Miércoles";
        }
        tvDiaActual.setText("🏋️‍♂️ Rutina del " + diaDeHoy);
    }

    private void obtenerObjetivoUsuario() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String objetivoStr = documentSnapshot.getString("objetivo");
                        if (objetivoStr != null) {
                            tvObjetivoRutina.setText("Basado en tu meta: " + objetivoStr);

                            String queryObjetivo = "mantener";
                            if (objetivoStr.toLowerCase().contains("perder")) {
                                queryObjetivo = "perder";
                            } else if (objetivoStr.toLowerCase().contains("ganar") || objetivoStr.toLowerCase().contains("masa")) {
                                queryObjetivo = "ganar";
                            }

                            cargarRutinaYComidaDelDia(queryObjetivo);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar perfil de entrenamiento", Toast.LENGTH_SHORT).show());
    }

    private void cargarRutinaYComidaDelDia(String objetivoFiltrado) {
        if (diaDeHoy != null && diaDeHoy.length() > 0) {
            diaDeHoy = diaDeHoy.substring(0, 1).toUpperCase() + diaDeHoy.substring(1).toLowerCase();
        }

        Log.d("TFG_DEBUG", "Buscando en Firebase -> Objetivo: " + objetivoFiltrado + " | Día: " + diaDeHoy);

        db.collection("ejercicios")
                .whereEqualTo("objetivo", objetivoFiltrado)
                .whereEqualTo("dia", diaDeHoy)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("TFG_DEBUG", "Firebase devolvió una lista VACÍA para esos filtros.");
                        tvTituloRutina.setText("💤 Día de Descanso");
                        tvDescripcionRutina.setText("Hoy no hay rutinas de fuerza asignadas. Aprovecha el día de hoy para recuperar tus fibras musculares.");
                        tvPostEntreno.setText("🥗 Día de descanso activo. Prioriza comidas limpias y mantente hidratado.");
                        return;
                    }

                    com.google.firebase.firestore.DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                    String titulo = doc.getString("titulo");
                    String descripcion = doc.getString("descripcion");

                    String postEntrenoTexto = doc.getString("post_entreno (sugerencia)");

                    Log.d("TFG_DEBUG", "¡Documento encontrado con éxito!");
                    Log.d("TFG_DEBUG", "Contenido post_entreno (sugerencia): " + postEntrenoTexto);

                    if (descripcion != null) {
                        descripcion = descripcion.replace("", "");
                    }
                    if (postEntrenoTexto != null) {
                        postEntrenoTexto = postEntrenoTexto.replace("", "");
                    } else {
                        postEntrenoTexto = "Opción Recomendada:El documento existe pero el campo post_entreno (sugerencia) está vacío o mal nombrado.";
                    }

                    tvTituloRutina.setText("🔥 " + titulo);
                    tvDescripcionRutina.setText(descripcion);
                    tvPostEntreno.setText(postEntrenoTexto);
                })
                .addOnFailureListener(e -> {
                    Log.e("TFG_EJERCICIOS", "Error crítico al conectar con la colección ejercicios", e);
                    Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                });
    }
}