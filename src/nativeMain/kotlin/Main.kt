import kotlinx.cinterop.*
import ncurses.*
import platform.posix.timespec

/*
  // allocate C memory
    val t = nativeHeap.alloc<timespec>()
    nativeHeap.free(t.rawPtr)

    // regional memory allocation
    val arena = Arena()
    //  arena.defer { .. } -> run this code when arena is cleared
    arena.alloc<timespec>()
    arena.alloc<timespec>()
    arena.clear()


    memScoped {
    defer{}
        alloc<timespec>()
        alloc<timespec>()
        alloc<timespec>()
        alloc<timespec>()

        // memory is deallocated at the end of the lambda
    }
 */

fun main(args: Array<String>) = memScoped {
    initscr()
    defer { endwin() }
    noecho()
    curs_set(0)
    halfdelay(2)

    val width = 20
    val height = 10

    //Reference counting in kotlin

    var snake = Snake(
        cells = listOf(Cell(2, 0), Cell(1, 0), Cell(0, 0)),
        direction = Direction.Right
    )

    var game = Game(width, height, snake)

    //allocation manually since this is C
    val window: CPointer<WINDOW> = newwin(game.height + 2, game.width + 2, 0, 0)!!
    defer { delwin(window) }


    var input = 0

    while (!input.toChar().equals('q')) {
        window.draw(game)

        input = wgetch(window)

        val direction = when (input.toChar()) {
            'i' -> Direction.Up
            'j' -> Direction.Left
            'k' -> Direction.Down
            'l' -> Direction.Right
            else -> null

        }
        game = game.update(direction)

    }


}

private fun CPointer<WINDOW>.draw(game: Game) {
    wclear(this)

    box(this, 0, 0)
    game.snake.tail.forEach {
        mvwprintw(this, it.y + 1, it.x + 1, "o")

    }
    game.snake.head.let {
        mvwprintw(this, it.y + 1, it.x + 1, "Q")

    }

    if (game.isOver) {
        mvwprintw(this, 0, 6, "Game Over")
        mvwprintw(this, 1, 3, "Your Score ${game.score}")
    }

    wrefresh(this)
}

data class Game(
    val width: Int,
    val height: Int,
    val snake: Snake
) {

    val score = snake.cells.size
    val isOver = snake.tail.contains(snake.head) ||
            snake.cells.any {
                it.x < 0 || it.x >= width || it.y < 0 || it.y >= height
            }

    fun update(direction: Direction?): Game {
        if (isOver) return this
        return copy(
            snake = snake.turn(direction).move()
        )

    }
}

// ?? how is this a functional interface
data class Snake(val cells: List<Cell>, val direction: Direction) {

    val head = cells.first()
    val tail = cells.subList(1, cells.size)

    fun move(): Snake {
        val newHead = cells.first().move(direction)
        val newTail = cells.dropLast(1)

        return copy(
            cells = listOf(newHead) + newTail
        )
    }

    fun turn(newDirection: Direction?): Snake {
        if (newDirection == null || newDirection.isOpposite(direction)) return this
        return copy(direction = newDirection)
    }
}

data class Cell(val x: Int, val y: Int) {
    fun move(direction: Direction) =
        Cell(x + direction.dx, y + direction.dy)

}

enum class Direction(val dx: Int, val dy: Int) {
    Up(0, -1), Down(0, 1), Left(-1, 0), Right(1, 0);

    fun isOpposite(that: Direction) =
        dx + that.dx == 0 && dy + that.dy == 0
}