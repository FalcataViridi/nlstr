package vocs.nlstr.actions


public interface basicCommands {

    val basicCommands: List<String>
        get() = listOf("isenzo", "umbono", "ulnazi")
    fun isBasicCommand (word: String): Boolean {
        return false
    }
}
