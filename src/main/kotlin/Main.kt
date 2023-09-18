import Helper.HelperFileAsString

fun main(args: Array<String>) {
    val gson = HelperFileAsString().gson<"SUA CLASSE">(<"SUA CLASSE">, "<nome do arquivo do seu json em resources/objects/delivery>")
    println(gson);
}