package libr

import org.mapdb.*
import org.mapdb.serializer.*
import kotlin.comparisons.compareBy


// todo: improve predicate filtering to allow for internal comparisons
    // ie. show _ where read == pages
// todo: add more filter options
// todo: add in the capability to selectively show columns
// todo: add in some basic aggretgate functions (such as count)

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
    // val qargs = 1 until args.size
    val qargs = args.slice(1 until args.size).toMutableList()
    when (args.getOrNull(0)) {
        "show" -> showCommand(library, qargs)
        "add" -> addCommand(library, getAndInc(id), qargs)
        "del" -> deleteCommand(library, qargs)
        "set" -> updateCommand(library, qargs)
        else -> displayHelp(qargs)
    }

    db.close();
}


fun showCommand(lib: HTreeMap<Long, String>, args: MutableList<String>) {
    val disp = parseDisplay(args)
    val predicate = parsePred(args)
    
    val toDisplay = lib.map { it.value.toBook() }
                       .filter { predicate(it) }
                       .sortedWith(compareBy({ it.component3() }))
                       .reversed()           // sortedWith puts this in the wrong order

    disp.determineWidths(toDisplay)
    
    disp.printHeader()

    var pages = 0
    var read = 0
    toDisplay.forEach {
        disp.printObject(it)
        pages += it.component3()
        read += it.component4()
    }

    println("\nTOTAL: $pages | READ: $read")
}

fun addCommand(lib: HTreeMap<Long, String>, id: Long, args: MutableList<String>) {
    if (args.size < 4) return displayHelp("add", args)

    try {
        val inset = Book(args[0], args[1], args[2].toInt(), args[3].toInt())
        lib.put(id, inset.toString())
    } catch (e: NumberFormatException) {
        displayHelp("add", args)
    }
}

fun deleteCommand(lib: HTreeMap<Long, String>, args: MutableList<String>) {
    if (args.getOrNull(0) != "where") return displayHelp("del", args)

    val predicate = parsePred(args.slice(1 until args.size).toMutableList())
    for (key in lib.filterValues { predicate(it.toBook()) }.map { it.key }) {
       lib.remove(key)
    }
}

fun updateCommand(lib: HTreeMap<Long, String>, args: MutableList<String>) {
    if (args.size < 4) return displayHelp("set", args)

    val predicate = parsePred(args.slice(2 until args.size).toMutableList())
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
}

fun displayHelp(args: MutableList<String>) {
    println("Help $args!")
}

fun displayHelp(cmd: String, args: MutableList<String>) {
    println("Help:$cmd $args!")
}


// SQLish parsing functions
fun parseDisplay(args: MutableList<String>): Displayer {
    if (args.size == 0) return Displayer()

    return when (args.removeAt(0)) {
        "_" -> Displayer()
        "where" -> Displayer()
        else -> Displayer()
    }
}

fun parsePred(args: MutableList<String>): (Book) -> Boolean {
    if (args.size < 4) return ::theTruth
    if (args[0] != "where") return ::theTruth

    val relation = getRelation(args[1], args[2], args[3])
    return fun(bk: Book): Boolean {
        return relation.call(bk)
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