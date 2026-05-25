package com.example.proyectotfg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectotfg.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText loginCorreo, loginContra;
    private Button btnLogin;
    private Button btnIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginCorreo = findViewById(R.id.LoginCorreo);
        loginContra = findViewById(R.id.LoginContra);
        btnLogin = findViewById(R.id.btnLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registro.class);
                startActivity(intent);
            }
        });

        // Lógica de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = loginCorreo.getText().toString().trim();
                String contra = loginContra.getText().toString().trim();

                if (correo.isEmpty() || contra.isEmpty()) {
                    Toast.makeText(Login.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Autenticación con Firebase Auth
                mAuth.signInWithEmailAndPassword(correo, contra)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    verificarPerfilUsuario();
                                } else {
                                    Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void verificarPerfilUsuario() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String rol = documentSnapshot.getString("rol");

                        if (rol != null && rol.equalsIgnoreCase("nutricionista")) {
                            Toast.makeText(Login.this, "Acceso concedido: Panel Nutricionista", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, NutricionistaActivity.class));
                        } else {
                            Toast.makeText(Login.this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, MainActivity.class));
                        }

                    } else {
                        Toast.makeText(Login.this, "Por favor, completa tu perfil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, ConfiguracionPerfil.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(Login.this, MainActivity.class));
                    finish();
                });
    }
}