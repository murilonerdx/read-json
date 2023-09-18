package Helper

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

class HelperFileAsString {
    private fun getResourceFileAsString(fileName: String): String {
        return BufferedReader(InputStreamReader(getResourceFileAsInputStream(fileName), StandardCharsets.UTF_8)).lines()
            .collect(Collectors.joining(System.lineSeparator())) as String
    }

    private fun getResourceFileAsInputStream(fileName: String): InputStream {
        return HelperFileAsString::class.java.classLoader.getResourceAsStream("objects/$fileName.json")!!
    }

    fun <T : Any> gson(objectType: Class<T>, fileName: String): T {
        return convert(getResourceFileAsString(fileName), objectType)
    }

    private fun <T> convert(jsonString: String, objectType: Class<T>): T {
        val obj = objectType.getDeclaredConstructor().newInstance()
        val findAll = mutableListOf<Pair<String, Any>>()

        // Define a expressão regular para capturar os pares chave-valor desejados
        val pattern = Regex("\"(.*?)\":\\s*(\\[[^\\]]*\\]|\"[^\"]*\")")

        // Encontra todas as correspondências na string JSON
        val matches = pattern.findAll(jsonString)

        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2]

            // Remove as aspas externas, se houver
            val cleanedValue = if (value.startsWith("\"") && value.endsWith("\"")) {
                value.substring(1, value.length - 1)
            } else {
                value
            }

            findAll.add(Pair(key, cleanedValue))
        }

        for(param in findAll){
            try {
                val field: Field? = findFieldRecursively(objectType, param.first)
                if (field != null) {
                    field.isAccessible = true
                    val convertedValue: Any? = when (val fieldType: Class<*> = field.type) {
                        String::class.java -> {
                            param.second.toString()
                        }

                        Int::class.javaPrimitiveType, Int::class.java -> {
                            param.second.toString().toInt()
                        }

                        Double::class.javaPrimitiveType, Double::class.java -> {
                            param.second.toString().toDouble()
                        }

                        Float::class.javaPrimitiveType, Float::class.java -> {
                            param.second.toString().toFloat()
                        }

                        Boolean::class.javaPrimitiveType, Boolean::class.java -> {
                            param.second.toString().toBoolean()
                        }

                        List::class.java -> {
                            listOf(param.second.toString().replace("[\\[\\]]", ""))
                        }

                        else -> {
                            if (fieldType == Any::class.java) {
                                convert(param.second.toString(), fieldType)
                            } else {
                                convert(param.second.toString(), fieldType)
                            }
                        }
                    }
                    field.set(obj, convertedValue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return obj
    }

    private fun findFieldRecursively(clazz: Class<*>, fieldName: String): Field? {
        return try {
            val field: Field? = clazz.getDeclaredField(fieldName)
            if (field != null) {
                field
            } else {
                val superClass: Class<*>? = clazz.superclass
                if (superClass != null && superClass != Any::class.java) {
                    findFieldRecursively(superClass, fieldName)
                } else {
                    null
                }
            }
        } catch (e: NoSuchFieldException) {
            val superClass: Class<*>? = clazz.superclass
            if (superClass != null && superClass != Any::class.java) {
                findFieldRecursively(superClass, fieldName)
            } else {
                null
            }
        }
    }
}