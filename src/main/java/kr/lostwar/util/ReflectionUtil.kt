package kr.lostwar.util

class ReflectionUtil {
    companion object{
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
    }
}
