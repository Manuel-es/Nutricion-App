package com.example.proyectotfg.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.proyectotfg.R;
import com.example.proyectotfg.api.OpenFoodFactsApi;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvSaludo, tvCaloriasRestantes, tvPasos;
    private TextView tvProteinas, tvCarbos, tvGrasas;
    private ProgressBar pbCalorias;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditText etBarcode;
    private Button btnSearch;
    private TextView tvResult;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent = false;
    private static final int PERMISSION_REQUEST_ACTIVITY = 100;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private OpenFoodFactsApi api;
    private int caloriasObjetivo = 2000;
    private int proteinasObj = 0, carbosObj = 0, grasaObj = 0;
    private int caloriasConsumidas = 0;
    private int proteinasConsumidas = 0, carbosConsumidos = 0, grasaConsumidas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializar Firebase y Hardware
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        tvSaludo = findViewById(R.id.tvSaludo);
        tvCaloriasRestantes = findViewById(R.id.tvCaloriasRestantes);
        tvPasos = findViewById(R.id.tvPasos);
        pbCalorias = findViewById(R.id.pbCalorias);
        etBarcode = findViewById(R.id.etBarcode);
        btnSearch = findViewById(R.id.btnSearch);
        tvResult = findViewById(R.id.tvResult);
        tvProteinas = findViewById(R.id.tvProteinas);
        tvCarbos = findViewById(R.id.tvCarbos);
        tvGrasas = findViewById(R.id.tvGrasas);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                0, 0);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_reset) {
                reiniciarProgresoDiario();
            } else if (id == R.id.nav_recetas) {
                Intent intentRecetas = new Intent(MainActivity.this, RecetasActivity.class);
                startActivity(intentRecetas);
            } else if (id == R.id.nav_ejercicios) {
                Intent intentEjercicios = new Intent(MainActivity.this, EjerciciosActivity.class);
                startActivity(intentEjercicios);
            } else if (id == R.id.nav_logout) {
                cerrarSesionUsuario();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        verificarPermisosSensor();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(OpenFoodFactsApi.class);

        cargarDatosUsuario();

        btnSearch.setOnClickListener(v -> buscarProducto());
    }

    private void reiniciarProgresoDiario() {
        caloriasConsumidas = 0;
        proteinasConsumidas = 0;
        carbosConsumidos = 0;
        grasaConsumidas = 0;

        actualizarGraficosDashboard();
        tvResult.setText("🔄 Progreso diario restablecido");

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> resetUpdates = new HashMap<>();
        resetUpdates.put("caloriasConsumidasHoy", 0);
        resetUpdates.put("proteinaConsumidaHoy", 0);
        resetUpdates.put("carbosConsumidosHoy", 0);
        resetUpdates.put("grasaConsumidaHoy", 0);

        db.collection("usuarios").document(userId)
                .update(resetUpdates)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "¡Progreso borrado en la nube!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("FIRESTORE_ERROR", "No se pudo resetear el día", e));
    }

    private void cerrarSesionUsuario() {
        mAuth.signOut();
        Toast.makeText(this, "🚪 Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void verificarPermisosSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST_ACTIVITY);
            } else {
                inicializarSensorPasos();
            }
        } else {
            inicializarSensorPasos();
        }
    }

    private void inicializarSensorPasos() {
        if (sensorManager != null && sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
            tvPasos.setText("👣 Pasos hoy: 0");
        } else {
            // TRUCO TEMPORAL PARA EL EMULADOR: Forzamos a que intente escuchar de todas formas
            stepSensor = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) : null;
            isSensorPresent = true;
            tvPasos.setText("👣 Esperando señal del podómetro...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            inicializarSensorPasos();
        }
    }

    private void cargarDatosUsuario() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null) tvSaludo.setText("Hola, " + nombre + "!");

                        Long totalCalorias = documentSnapshot.getLong("caloriasDiarias");
                        Long pObj = documentSnapshot.getLong("macroProteinaObj");
                        Long cObj = documentSnapshot.getLong("macroCarbosObj");
                        Long gObj = documentSnapshot.getLong("macroGrasaObj");

                        if (totalCalorias != null) caloriasObjetivo = totalCalorias.intValue();
                        if (pObj != null) proteinasObj = pObj.intValue();
                        if (cObj != null) carbosObj = cObj.intValue();
                        if (gObj != null) grasaObj = gObj.intValue();

                        Long cCons = documentSnapshot.getLong("caloriasConsumidasHoy");
                        Long pCons = documentSnapshot.getLong("proteinaConsumidaHoy");
                        Long ccCons = documentSnapshot.getLong("carbosConsumidosHoy");
                        Long gCons = documentSnapshot.getLong("grasaConsumidaHoy");

                        caloriasConsumidas = (cCons != null) ? cCons.intValue() : 0;
                        proteinasConsumidas = (pCons != null) ? pCons.intValue() : 0;
                        carbosConsumidos = (ccCons != null) ? ccCons.intValue() : 0;
                        grasaConsumidas = (gCons != null) ? gCons.intValue() : 0;

                        actualizarGraficosDashboard();
                    }
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE_ERROR", "Error al leer datos", e));
    }

    private void actualizarGraficosDashboard() {
        int restantes = caloriasObjetivo - caloriasConsumidas;
        if (restantes < 0) restantes = 0;

        tvCaloriasRestantes.setText("Restan: " + restantes + " / " + caloriasObjetivo + " kcal");
        pbCalorias.setMax(caloriasObjetivo);
        pbCalorias.setProgress(caloriasConsumidas);

        tvProteinas.setText("Prot:\n" + proteinasConsumidas + "g / " + proteinasObj + "g");
        tvCarbos.setText("Carb:\n" + carbosConsumidos + "g / " + carbosObj + "g");
        tvGrasas.setText("Gras:\n" + grasaConsumidas + "g / " + grasaObj + "g");
    }

    private void buscarProducto() {
        String barcode = etBarcode.getText().toString().trim();
        if (barcode.isEmpty()) return;

        tvResult.setText("Buscando nutrientes...");

        api.getProductInfo(barcode).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject root = response.body();

                    if (root.has("status") && root.get("status").getAsInt() == 1) {
                        JsonObject product = root.getAsJsonObject("product");
                        String nombreProducto = product.has("product_name") ? product.get("product_name").getAsString() : "Alimento";

                        int energiaKcal = 0;
                        int proteinaGramos = 0;
                        int carbosGramos = 0;
                        int grasaGramos = 0;

                        if (product.has("nutriments")) {
                            JsonObject nutriments = product.getAsJsonObject("nutriments");

                            if (nutriments.has("energy-kcal_100g")) energiaKcal = nutriments.get("energy-kcal_100g").getAsInt();
                            else if (nutriments.has("energy-kcal")) energiaKcal = nutriments.get("energy-kcal").getAsInt();

                            if (nutriments.has("proteins_100g")) proteinaGramos = (int) nutriments.get("proteins_100g").getAsDouble();
                            else if (nutriments.has("proteins")) proteinaGramos = (int) nutriments.get("proteins").getAsDouble();

                            if (nutriments.has("carbohydrates_100g")) carbosGramos = (int) nutriments.get("carbohydrates_100g").getAsDouble();
                            else if (nutriments.has("carbohydrates")) carbosGramos = (int) nutriments.get("carbohydrates").getAsDouble();

                            if (nutriments.has("fat_100g")) grasaGramos = (int) nutriments.get("fat_100g").getAsDouble();
                            else if (nutriments.has("fat")) grasaGramos = (int) nutriments.get("fat").getAsDouble();
                        }

                        tvResult.setText("Añadido: " + nombreProducto + "\n🔥 " + energiaKcal + " kcal | 🍗 " + proteinaGramos + "g P | 🍞 " + carbosGramos + "g C | 🥑 " + grasaGramos + "g G");
                        etBarcode.setText("");

                        registrarNutrientesConsumidos(energiaKcal, proteinaGramos, carbosGramos, grasaGramos);

                    } else {
                        tvResult.setText("❌ Alimento no encontrado");
                    }
                } else {
                    tvResult.setText("❌ Error en el servidor");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                tvResult.setText("❌ Error de conexión");
            }
        });
    }

    private void registrarNutrientesConsumidos(int kcal, int p, int c, int g) {
        caloriasConsumidas += kcal;
        proteinasConsumidas += p;
        carbosConsumidos += c;
        grasaConsumidas += g;

        actualizarGraficosDashboard();

        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("caloriasConsumidasHoy", caloriasConsumidas);
        updates.put("proteinaConsumidaHoy", proteinasConsumidas);
        updates.put("carbosConsumidosHoy", carbosConsumidos);
        updates.put("grasaConsumidaHoy", grasaConsumidas);

        db.collection("usuarios").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Macros del día sincronizados"))
                .addOnFailureListener(e -> Log.e("FIRESTORE_ERROR", "Fallo al actualizar macros", e));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            tvPasos.setText("👣 Pasos hoy: " + (int) event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        // Al forzar isSensorPresent a true, el emulador SÍ se suscribirá al evento de hardware
        if (isSensorPresent && sensorManager != null && stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent && sensorManager != null && stepSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }
}