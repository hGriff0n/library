package libr

// 'relation' produces a class that we initialize with val
    // this class is able to handle parsing val into it's expected format
    // then in the returned function, we pass the book's value on to the class to get the result
// I probably need to turn this into an interface not a class (to handle the possibilities)
class Predicate(val field: String) {
    var fn = fun (s: String): Boolean { return s != null }

    fun call(bk: Book): Boolean {
        val (title, auth, pgs, read) = bk

        return when (field) {
            "author" -> fn(auth)
            "title" -> fn(title)
            "pages" -> fn(pgs.toString())
            "read" -> fn(read.toString())
            else -> false
        }
    }

    fun setCompareObject(nfn: (String) -> Boolean): Predicate {
        fn = nfn
        return this
    }
}

fun getRelation(field: String, relation: String, obj: String): Predicate {
    val rel = when (relation.toLowerCase()) {
        // This doesn't quite work (doesn't show enough)
        "startswith" -> fun (s: String): Boolean { return s.startsWith(obj) }
        "==" -> fun (s: String): Boolean { return obj.equals(s) }
        // this doesn't work
        ">" -> fun (s: String): Boolean { return s.toInt() > obj.toInt() }
        else -> fun (s: String): Boolean { return s != null }
    }

    return Predicate(field).setCompareObject(rel)
}