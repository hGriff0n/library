package libr

// Same procedure as for 'Predicate' but focused on displaying the results with correct formatting
class Displayer {
    var title_width = 6
    var author_width = 7
    var page_width = 4
    var read_width = 4

    fun printHeader() {
        println(" %1$-${title_width}s | %2$-${author_width}s | %3$-5s | %4$-4s".format("title", "author", "pages", "read"))
        println("=".repeat(title_width+author_width+9+11))
    }

    fun printObject(bk: Book) {
        val (title, author, pages, read) = bk
        println(" %1$-${title_width}s | %2$-${author_width}s | %3$-5s | %4$-4s".format(title, author, pages, read))
    }

    fun determineWidths(entries: List<Book>) {
        entries.forEach {
            if (it.title.length > title_width)
                title_width = it.title.length
            if (it.author.length > author_width)
                author_width = it.author.length
        }
    }
}