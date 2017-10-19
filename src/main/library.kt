package libr

import org.mapdb.*
import org.mapdb.serializer.*


fun main(args: Array<String>) {
    // setup the database
    var db = DBMaker
        .fileDB("ktbooks.db")
        // .transactionEnable()
        .make()

    // Create the backing storage
        // TODO: Get this typed better
    var library = db.hashMap("name_of_map")
        .keySerializer(Serializer.LONG)
        .valueSerializer(Serializer.STRING)
        .createOrOpen();

    // Get the id counter
    var id = db.atomicVar("maxId", Serializer.LONG).createOrOpen();
    id.compareAndSet(null, 0)

    // Run command line resolution
    val qargs = 1 until args.size
    when (args.getOrNull(0)) {
        "show" -> showCommand(library, args.slice(qargs))
        "add" -> addCommand(library, getAndInc(id), args.slice(qargs))
        "del" -> deleteCommand(library, args.slice(qargs))
        "set" -> updateCommand(library, args.slice(qargs))
        else -> displayHelp(args.slice(qargs))
    }

    db.close();
}


fun showCommand(lib: HTreeMap<Long, String>, args: List<String>) {
    val disp = parseDisplay(args)
    val predicate = parsePred(args)
    
    val toDisplay = lib.map { it.value.toBook() }
                       .filter { predicate(it) }
    
    // TODO: Calculate display info before running display function
    toDisplay.forEach {
        disp(it)
    }
}

fun addCommand(lib: HTreeMap<Long, String>, id: Long, args: List<String>) {
    val inset = Book("Book", "Book", 3, 3)
    lib.put(id, inset.toString())
}

fun deleteCommand(lib: HTreeMap<Long, String>, args: List<String>) {
    if (args.getOrNull(0) != "where") return displayHelp(args)

    println("Delete $args")
}

fun updateCommand(lib: HTreeMap<Long, String>, args: List<String>) {
    println("Update $args")
}

fun displayHelp(args: List<String>) {
    println("Help $args!")
}


// SQLish parsing functions
fun parseDisplay(args: List<String>): (Book) -> Unit {
    return fun(bk: Book) {
        val (title, author, pages, read) = bk
        println("$title | $author | $pages | $read")
    }
}

fun parsePred(args: List<String>): (Book) -> Boolean {
    return fun(bk: Book): Boolean { return true }
}


// Helper functions
data class Book(var title: String, var author: String, var pages: Int, var read: Int = 0) {
    override fun toString(): String {
        return "$title;;$author;;$pages;;$read"
    }
}

fun String.toBook(): Book {
    var parts = split(";;")
    return Book(parts[0], parts[1], parts[2].toInt(), parts[3].toInt())
}

fun getAndInc(v: Atomic.Var<Long>): Long {
    return v.getAndSet(v.get() + 1)
}