package kr.lostwar.util

import org.apache.commons.lang.Validate
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class Config(val file: File) : YamlConfiguration() {

    constructor(path: String) : this(File(path))

    init {
        try {
            createFile(file)
            load(file)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun createFile(path: File): Boolean {
        if(!path.exists()){
            path.parentFile.mkdirs()
            path.createNewFile()
            return false
        }
        return true
    }

    fun reload() {
        try {
            load(file.path)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save() {
        try {
            Validate.notNull(file, "File cannot be null")
            save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}