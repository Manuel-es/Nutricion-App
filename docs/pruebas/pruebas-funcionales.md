# Pruebas funcionales Nutrición-App

## Registro e Inicio de Sesión de Usuarios

### Caso PF-001: Validar correo electrónico
* **Acción:** Registro con correo electrónico sin "@" (`manuelgmail.com`).
* **Resultado esperado:** Error en pantalla: "Formato de correo no válido". El sistema bloquea el avance.
* **Resultado obtenido:** El validador intercepta la cadena y muestra el error correctamente.
* **Estado:** OK

### Caso PF-002: Validar contraseña corta
* **Acción:** Registro introduciendo una contraseña de menos de 6 caracteres (`1234`).
* **Resultado esperado:** Firebase Authentication rechaza la petición. Mensaje en la app: "La contraseña debe tener mínimo 6 caracteres".
* **Resultado obtenido:** Error controlado, el sistema no crashea y avisa al usuario.
* **Estado:** OK

### Caso PF-003: Validar campos vacíos en el Perfil
* **Acción:** Intentar calcular la fórmula matematica dejando el campo "Peso" en blanco.
* **Resultado esperado:** La app detecta el campo vacío antes de calcular, frena el hilo y lanza un aviso en el campo de texto.
* **Resultado obtenido:** Bloqueo correcto de la operación mediante `TextUtils.isEmpty()`.
* **Estado:** OK

### Caso PF-004: Escáner de código de barras erróneo
* **Acción:** Escanear un producto con un código de barras roto, incompleto o inexistente.
* **Resultado esperado:** Retrofit gestiona la respuesta HTTP, el objeto devuelve un código de estado alternativo y la app muestra: "Producto no encontrado".
* **Resultado obtenido:** Controlado sin crasheos.
* **Estado:** OK