# Incidencias Nutrición-App

## INC-001 (Relacionada con el registro de alimentos)

* **Problema:** Al escanear ciertos alimentos de marca blanca, la API de OpenFoodFacts devuelve algunos campos de macronutrientes vacíos (`null`) en el JSON. Al intentar sumar estos valores al acumulador diario del paciente, la aplicación sufría un error crítico de tipo `NullPointerException` y se cerraba sola.
* **Prioridad:** Alta
* **Estado:** Resuelto
* **Causa:** La clase intermedia no filtraba si los campos numéricos del JSON venían vacíos antes de parsearlos e intentar transformarlos en tipos primitivos dobles o enteros.
* **Solución:** Se implementó un método de saneamiento y validación dentro del modelo `AlimentoResponse`. Ahora, si la API devuelve un valor nulo para las proteínas, grasas o carbohidratos, el sistema intercepta el valor, le asigna un `0.0` por defecto y permite que el usuario continúe registrando su comida sin que la app explote.

```java
// Código Java de seguridad implementado en el modelo de datos
public double sanearMacronutriente(Double valorJson) {
    if (valorJson == null) {
        return 0.0; // Evita el NullPointerException si el campo no existe en la API
    }
    return valorJson;
}