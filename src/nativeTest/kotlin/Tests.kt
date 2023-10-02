import kotlin.test.Test
import kotlin.test.assertEquals

class SnakeTest {

    private val snake = Snake(
        cells = listOf(Cell(2,0), Cell(1,0), Cell(0,0)),
        direction = Direction.Right
    )


    @Test
    fun `snake moves right`(){

        assertEquals(
            actual = snake.move(),
            expected = Snake(
                cells = listOf(Cell(3,0), Cell(2,0),Cell(1,0)),
                direction = Direction.Right
            )
        )
    }

    @Test
    fun `snake changes direction`(){

        assertEquals(
            actual = snake.turn(Direction.Down).move(),
            expected = Snake(
                cells = listOf(Cell(2,1), Cell(2,0),Cell(1,0)),
                direction = Direction.Down
            )
        )

        assertEquals(
            actual = snake.turn(Direction.Left).move(),
            expected = Snake(
                cells = listOf(Cell(3,0), Cell(2,0),Cell(1,0)),
                direction = Direction.Right
            )
        )
    }
}