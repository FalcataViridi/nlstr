package vocs.nlstr.utils

/**
 * Created by Moises on 29/07/2018.
 */
enum class RecognitionStatus {
    SUCCESS, FAILURE, UNAVAILABLE
}

enum class TranslationKeys(key: String) {
    TRADUCIR("traducir")
    , TRADUCIDO("traducido")
    , BORRAR("borrar")
    , ACEPTAR("aceptar")

}