package com.example.proyectotfg.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectotfg.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registro extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText correousu, contrausu, confirmcontra;
    private CheckBox cbEsNutricionista; // NUEVA VISTA: Selector de rol
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro);

        correousu = findViewById(R.id.correo);
        contrausu = findViewById(R.id.contraseña);
        confirmcontra = findViewById(R.id.confirma);
        cbEsNutricionista = findViewById(R.id.cbEsNutricionista); // Vinculamos la casilla de verificación
        btnGuardar = findViewById(R.id.Guardar);

        mAuth = FirebaseAuth.getInstance();

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correousu.getText().toString().trim();
                String contra = contrausu.getText().toString().trim();
                String confirm = confirmcontra.getText().toString().trim();

                if (correo.isEmpty() || contra.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(Registro.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!contra.equals(confirm)) {
                    Toast.makeText(Registro.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (contra.length() < 6) {
                    Toast.makeText(Registro.this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(correo, contra)
                        .addOnCompleteListener(Registro.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registro.this, "¡Cuenta creada! Vamos a configurar tu perfil", Toast.LENGTH_SHORT).show();

                                    String rolAsignado = "usuario";
                                    if (cbEsNutricionista.isChecked()) {
                                        rolAsignado = "nutricionista";
                                    }

                                    Intent intent = new Intent(Registro.this, ConfiguracionPerfil.class);
                                    intent.putExtra("ROL_SELECCIONADO", rolAsignado);
                                    startActivity(intent);

                                    finish();
                                } else {
                                    Toast.makeText(Registro.this, "Error al registrarse: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}