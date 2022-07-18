package kr.lostwar.util.nms

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object ReflectionUtil {
    @JvmStatic
    fun Any.getPrivateField(fieldName: String): Any?{
        return try{
            val field = this.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(this)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
    @JvmStatic
    fun getPrivateStaticField(fieldName: String, clazz: Class<*>): Any?{
        return try{
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(null)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    private val fieldMap = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Field>>()

    fun unlockField(clazz: Class<*>, fieldName: String) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            val classFieldMap = fieldMap.computeIfAbsent(clazz) { ConcurrentHashMap() }
            classFieldMap[fieldName] = field
            fieldMap[clazz] = classFieldMap
        }catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getField(clazz: Class<*>, fieldName: String): Field {
        if(!fieldMap.containsKey(clazz) || !fieldMap[clazz]!!.containsKey(fieldName)) {
            unlockField(clazz, fieldName)
        }

        return fieldMap[clazz]!![fieldName]!!
    }

    private val methodMap = ConcurrentHashMap<Class<*>, ConcurrentHashMap<String, Method>>()
    fun unlockMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>) {
        try {
            val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
            val classMethodMap = methodMap.computeIfAbsent(clazz) { ConcurrentHashMap() }
            classMethodMap[methodName] = method
            methodMap[clazz] = classMethodMap
        }catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    fun getMethod(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        if(!methodMap.containsKey(clazz) || !methodMap[clazz]!!.containsKey(methodName)) {
            unlockMethod(clazz, methodName, *parameterTypes)
        }

        return methodMap[clazz]!![methodName]!!
    }

    fun getClass(name: String) {
        val clazz = Class.forName("")
    }
}