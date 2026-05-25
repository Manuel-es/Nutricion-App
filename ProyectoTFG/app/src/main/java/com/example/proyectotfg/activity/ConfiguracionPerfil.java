package com.example.proyectotfg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectotfg.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class ConfiguracionPerfil extends AppCompatActivity {

    private EditText etNombre, etEdad, etPeso, etAltura;
    private Spinner spinnerObjetivo, spinnerActividad;
    private Button btnGuardar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // NUEVA VARIABLE: Para almacenar el rol que viene desde el Registro
    private String rolUsuario = "usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_perfil);

        //Inicializar componentes de Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getIntent() != null && getIntent().hasExtra("ROL_SELECCIONADO")) {
            rolUsuario = getIntent().getStringExtra("ROL_SELECCIONADO");
        }
        Log.d("TFG_ROLES", "Rol recibido para configurarse: " + rolUsuario);

        try {
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
        } catch (IllegalStateException e) {
            Log.w("TFG_FIRESTORE", "Settings ya inicializados previamente: " + e.getMessage());
        }

        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        etPeso = findViewById(R.id.etPeso);
        etAltura = findViewById(R.id.etAltura);
        spinnerObjetivo = findViewById(R.id.spinnerObjetivo);
        spinnerActividad = findViewById(R.id.spinnerActividad);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarDatos();
            }
        });
    }

    private void guardarDatos() {
        Toast.makeText(this, "Procesando datos...", Toast.LENGTH_SHORT).show();

        String nombre = etNombre.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();
        String pesoStr = etPeso.getText().toString().trim();
        String alturaStr = etAltura.getText().toString().trim();

        // Validación de datos
        if (nombre.isEmpty() || edadStr.isEmpty() || pesoStr.isEmpty() || alturaStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerObjetivo.getSelectedItem() == null || spinnerActividad.getSelectedItem() == null) {
            Toast.makeText(this, "⚠️ Selecciona una opción en los desplegables", Toast.LENGTH_SHORT).show();
            return;
        }

        int edad = Integer.parseInt(edadStr);
        double peso = Double.parseDouble(pesoStr);
        int altura = Integer.parseInt(alturaStr);
        String objetivo = spinnerObjetivo.getSelectedItem().toString();
        String actividad = spinnerActividad.getSelectedItem().toString();

        double tmb = (10 * peso) + (6.25 * altura) - (5 * edad) + 5;

        double factorActividad = 1.2;
        if (actividad.toLowerCase().contains("ligero")) factorActividad = 1.375;
        else if (actividad.toLowerCase().contains("moderado")) factorActividad = 1.55;
        else if (actividad.toLowerCase().contains("fuerte")) factorActividad = 1.725;

        double mantenimiento = tmb * factorActividad;
        double caloriasObjetivo = mantenimiento;

        // Ajuste calórico adaptado según el objetivo nutricional
        if (objetivo.toLowerCase().contains("perder")) {
            caloriasObjetivo -= 500; // Déficit calórico
        } else if (objetivo.toLowerCase().contains("ganar") || objetivo.toLowerCase().contains("masa")) {
            caloriasObjetivo += 500; // Superávit calórico
        }

        // MACRONUTRIENTES
        int kcalTotales = (int) caloriasObjetivo;
        int gramosProteina = (int) ((kcalTotales * 0.25) / 4);
        int gramosCarbos = (int) ((kcalTotales * 0.50) / 4);
        int gramosGrasa = (int) ((kcalTotales * 0.25) / 9);

        // Control de Seguridad: Verificar el token de sesión de Firebase Auth
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Error: Sesión expirada. Inicie sesión de nuevo.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ConfiguracionPerfil.this, Login.class));
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Mapeo estructurado
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("uid", userId);
        perfil.put("nombre", nombre);
        perfil.put("email", mAuth.getCurrentUser().getEmail());
        perfil.put("edad", edad);
        perfil.put("peso", peso);
        perfil.put("altura", altura);
        perfil.put("objetivo", objetivo);
        perfil.put("actividad", actividad);
        perfil.put("caloriasDiarias", kcalTotales);

        // Persistencia de los objetivos calculados
        perfil.put("macroProteinaObj", gramosProteina);
        perfil.put("macroCarbosObj", gramosCarbos);
        perfil.put("macroGrasaObj", gramosGrasa);

        // Estructura de control diaria
        perfil.put("caloriasConsumidasHoy", 0);
        perfil.put("proteinaConsumidaHoy", 0);
        perfil.put("carbosConsumidosHoy", 0);
        perfil.put("grasaConsumidaHoy", 0);
        perfil.put("rol", rolUsuario);

        //Envío y sincronización asíncrona con Cloud Firestore
        db.collection("usuarios").document(userId)
                .set(perfil)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ConfiguracionPerfil.this, "¡Perfil guardado con éxito!", Toast.LENGTH_SHORT).show();

                    //Redireccionamiento segun el Rol
                    Intent intent;
                    if (rolUsuario != null && rolUsuario.equalsIgnoreCase("nutricionista")) {
                        intent = new Intent(ConfiguracionPerfil.this, NutricionistaActivity.class);
                    } else {
                        intent = new Intent(ConfiguracionPerfil.this, MainActivity.class);
                    }

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ConfiguracionPerfil.this, "ERROR EN LA NUBE: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("TFG_FIRESTORE", "Error crítico al escribir el documento del usuario", e);
                });
    }
}