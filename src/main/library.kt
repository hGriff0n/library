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
    var pages = 0
    var read = 0
    toDisplay.forEach {
        disp(it)
        read += it.component4()
        pages += it.component3()
    }

    println("TOTAL = read:$read pages:$pages")
}

fun addCommand(lib: HTreeMap<Long, String>, id: Long, args: List<String>) {
    if (args.size < 4) return displayHelp("add", args)

    // TODO: Handle unexpected values more gracefully (read can default to 0)
    val inset = Book(args[0], args[1], args[2].toInt(), args[3].toInt())
    lib.put(id, inset.toString())
}

fun deleteCommand(lib: HTreeMap<Long, String>, args: List<String>) {
    if (args.getOrNull(0) != "where") return displayHelp("del", args)

    val predicate = parsePred(args.slice(1 until args.size))

    for (key in lib.filterValues { predicate(it.toBook()) }.map { it.key }) {
       lib.remove(key)
    }

    println("Delete $args")
}

fun updateCommand(lib: HTreeMap<Long, String>, args: List<String>) {
    if (args.size < 4) return displayHelp("set", args)

    val predicate = parsePred(args.slice(2 until args.size))
    val field = args[0]

    for ((k, v) in lib) {
        val bk = v.toBook()

        if (predicate(bk)) {
            val nbk = when (field) {
                "author" -> bk.copy(author = args[1])
                "title" -> bk.copy(title = args[1])
                "pages" -> bk.copy(pages = args[1].toInt())
                "read" -> bk.copy(read = args[1].toInt())
                else -> return displayHelp("set", args)
            }

            lib.put(k, nbk.toString())
        }
    }

    println("Update $args")
}

fun displayHelp(args: List<String>) {
    println("Help $args!")
}

fun displayHelp(cmd: String, args: List<String>) {
    println("Help:$cmd $args!")
}


// SQLish parsing functions
fun parseDisplay(args: List<String>): (Book) -> Unit {
    return fun(bk: Book) {
        val (title, author, pages, read) = bk
        println("$title | $author | $pages | $read")
    }
}

fun parsePred(args: List<String>): (Book) -> Boolean {
    if (args.size < 4) return ::theTruth
    if (args[0] != "where") return ::theTruth
    
    // todo: parse out the data to extract
    val field = args[1]
    // val relation = args[2]
    // val val = args[3]

    return fun(bk: Book): Boolean {
        return when (field) {
            "author" -> true
            "title" -> true
            "pages" -> true
            "read" -> true
            else -> false
        }
    }
}

fun theTruth(bk: Book): Boolean { return true }


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