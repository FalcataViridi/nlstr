package vocs.nlstr.utils

/**
 * Created by Moises on 29/07/2018.
 */
enum class RecognitionStatus {
    SUCCESS, FAILURE, UNAVAILABLE
}

enum class TranslationKeys(var key: String, var desc: String) {
    TRADUCIR("traducir", "Introducir texto a traducir")
    , TRADUCIDO("traducido", "texto traducido")
    , BORRAR("borrar", "accion de borrar prueba con descripcion larga")
    , ACEPTAR("ok", "accion de aceptar")
}

enum class MainListKeys(var key: String, var desc: String) {
    CREAR("crear", "acción de crear")
    , BORRAR("borrar", "accion de borrar prueba con descripcion larga")
    , SIGUIENTE("siguiente", "accion de continuar")
    , ACEPTAR("ok", "accion de aceptar")
    , SELECCIONAR("perro", "accion de seleccionar un item de la lista")
}

enum class MainListItemAttributes {
    TITULO, DESCRIPCION, STATUS, TIPO
}

enum class ShoppingListItemAttributes {
    TITULO, DESCRIPCION, STATUS
}