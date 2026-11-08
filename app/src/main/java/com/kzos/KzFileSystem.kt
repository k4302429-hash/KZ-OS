package com.kzos

import android.content.Context
import java.io.File

/** Local, app-private storage. Files remain on the device and require no cloud service. */
class KzFileSystem(context: Context) {
    private val root = File(context.filesDir, "kz-drive").apply { mkdirs() }
    data class Item(val name: String, val isDirectory: Boolean)

    fun listItems(): List<Item> = root.listFiles().orEmpty().sortedBy { it.name.lowercase() }
        .map { Item(it.name, it.isDirectory) }

    fun readText(name: String): String = safeFile(name).takeIf { it.isFile }?.readText().orEmpty()
    fun writeText(name: String, text: String) { safeFile(name).writeText(text) }
    fun createFolder(name: String): String {
        val folder = safeFile(name)
        return if (folder.exists()) "Esse nome já está em uso" else if (folder.mkdir()) "Pasta criada" else "Não foi possível criar a pasta"
    }
    fun rename(oldName: String, newName: String): Boolean {
        val from = safeFile(oldName)
        return from.exists() && !safeFile(newName).exists() && from.renameTo(safeFile(newName))
    }
    fun delete(name: String): Boolean = safeFile(name).deleteRecursively()
    fun usedBytes(): Long = root.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    private fun safeFile(name: String): File {
        val clean = name.trim().replace('/', '_').replace('\\', '_').take(100).ifBlank { "untitled.txt" }
        return File(root, clean)
    }
}
