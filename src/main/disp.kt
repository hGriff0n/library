package libr

// Same procedure as for 'Predicate' but focused on displaying the results with correct formatting
class Displayer {
    fun printHeader() {
        println("title | author | pages | read")
    }

    fun printObject(bk: Book) {
        val (title, author, pages, read) = bk
        println("$title | $author | $pages | $read")
    }
}