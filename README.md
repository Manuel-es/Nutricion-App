# 🥑 Nutrición-App

**Nutrición-App** es una aplicación móvil nativa para Android diseñada para la gestión integral de la salud dietética y el control del sedentarismo. Permite a los usuarios monitorizar sus calorías mediante un escáner inteligente de código de barras y registrar su actividad física de forma pasiva a través del podómetro por hardware del móvil, conectando todos estos datos en tiempo real con un panel de control exclusivo para nutricionistas.

## 🎯 Propósito del Proyecto

En la actualidad, llevar un control de la dieta y el ejercicio es un jaleo porque obliga a usar mil apps a la vez. Además, los nutricionistas suelen ir "a ciegas" entre consulta y consulta, dependiendo de que el cliente les mande capturas por WhatsApp o un Excel apuntado a ojo. **Nutrición-App** unifica todo en una sola pantalla y rompe esa barrera: el paciente escanea lo que come, anda con el móvil en el bolsillo y el médico ve todo el progreso en su pantalla al segundo y en tiempo real.

## Idiomas Soportados

Para ofrecer una experiencia accesible y global, la aplicación cuenta con un sistema nativo de internacionalización (i18n) que se adapta automáticamente a toda la interfaz gráfica, menús y alertas según el idioma que el usuario tenga configurado en su dispositivo Android. Actualmente está disponible en:
* 🇪🇸 **Español** (Idioma nativo del desarrollo)
* 🇬🇧 **Inglés** (English)

## Roles de Usuario

La plataforma implementa una arquitectura de control de acceso basada en roles (RBAC) con 2 niveles de acceso totalmente diferenciados:

* 🧑‍⚕️ **Nutricionista (Profesional):** Perfil orientado a los especialistas de la salud.
  * **Dashboard Dinámico:** Acceso exclusivo a un panel avanzado con la lista estructurada de todos sus pacientes asignados.
  * **Auditoría Clínica:** Permite revisar los macros y calorías que consume cada paciente en tiempo real y hacer un seguimiento milimétrico sin esperar a la cita presencial.
  * **Bloqueo Perimetral:** La interfaz restringe el acceso a las funciones de usuario común para mantener un entorno 100% profesional.
* 🧑 **Paciente (Cliente):** Usuario estándar de la comunidad.
  * **Contador Todo en Uno:** Visualización en una sola pantalla del balance calórico diario y el conteo de pasos.
  * **Asistente Harris-Benedict:** Al configurar el perfil (edad, sexo, altura, peso), la app calcula automáticamente su gasto metabólico basal.
  * **Registro Automatizado:** Acceso al escáner de códigos de barras y al buscador para subir alimentos al instante.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje y entorno:** Java (JDK 17/21), Android Studio.
* **Base de datos en tiempo real:** Cloud Firestore (Base de datos NoSQL orientada a documentos).
* **Autenticación y Seguridad:** Firebase Authentication (Cifrado de credenciales TLS) y Firebase Security Rules.
* **Consumo de APIs de terceros:** Retrofit 2 (para el consumo asíncrono de servicios REST) y Gson para el parseo de JSONs.
* **Base de Datos Alimenticia:** API Pública de *OpenFoodFacts*.
* **Control de Hardware:** Android SensorManager (`Sensor.TYPE_STEP_COUNTER` para el podómetro por hardware).
* **Accesibilidad:** Motor de asistencia por voz (Audio UX) integrado en la configuración del perfil.
* **Control de versiones:** Git y GitHub.

## Arquitectura del Sistema

El proyecto se ha desarrollado siguiendo el principio de **Separación de Responsabilidades (SoC)**, garantizando que la aplicación responda de forma fluida a 60 FPS sin tirones visuales:
* **Capa de Modelos (POJO):** Clases como `AlimentoResponse` encargadas de mapear la estructura de los datos que vienen de internet.
* **Capa de Servicios Asíncronos:** Gestión de conexiones externas mediante callbacks y listeners en segundo plano (`addSnapshotListener`) para evitar congelar la pantalla.
* **Capa de Interfaz de Usuario (UI):** Controladores de la vista (`MainActivity`, `NutricionistaActivity`, `ConfiguracionPerfil`) que reaccionan estrictamente al ciclo de vida de Android (`onCreate`, `onResume`) y manejan los elementos gráficos (`TextView`, `ProgressBar`).

## 📂 Estructura del Repositorio

El repositorio está organizado de forma limpia y accesible para el tribunal:
```text
Proyecto_Subida
├── apk/                  # Archivo ejecutable .APK listo para instalar en el móvil
├── docs/                 # Memoria oficial (31 págs), Diagramas DAFO y capturas de pantalla
    ├── diagramas              
    └── memoria
    └── pruebas
└── ProyectoTFG/          # Código fuente completo de Android Studio (Carpeta raíz del proyecto)
    ├── app/              # Código Java, pantallas XML y recursos del sistema
    └── build.gradle      # Scripts de configuración y dependencias de Gradle
└── screenshots/
    ├── menu             
    └── Rol nutricionista
    └── usuario
