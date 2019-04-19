package nsmain2

import kotlin.random.Random

inline class PlayerMark(val value: String)

class Board(private val cells: List<PlayerMark?>) {

    companion object {
        val playerX = PlayerMark("x")
        val playerO = PlayerMark("o")

        val allCellPositions = listOf(
            0 to 0, 1 to 0, 2 to 0,
            0 to 1, 1 to 1, 2 to 1,
            0 to 2, 1 to 2, 2 to 2
        )

        val winPositionOptions = listOf(
            listOf(0 to 0, 0 to 1, 0 to 2),
            listOf(0 to 1, 1 to 1, 2 to 1),
            listOf(0 to 2, 1 to 2, 2 to 2),

            listOf(0 to 0, 1 to 0, 2 to 0),
            listOf(1 to 0, 1 to 1, 1 to 2),
            listOf(2 to 0, 2 to 1, 2 to 2),

            listOf(0 to 0, 1 to 1, 2 to 2),
            listOf(0 to 2, 1 to 1, 2 to 0)
        )

        fun createInitialBoard(): Board = Board(cells = List(9) { null })
    }

    fun getOpenCells(): List<Pair<Int, Int>> =
        allCellPositions.filter { (x, y) -> !isCellOccupied(x, y) }

    private fun getCellContents(x: Int, y: Int): PlayerMark? {
        require(x in 0..2 && y in 0..2)

        return cells[x + y * 3]
    }

    fun isCellOccupied(x: Int, y: Int): Boolean = getCellContents(x, y) != null

    private fun isCellOccupiedWith(x: Int, y: Int, mark: PlayerMark): Boolean {
        require(mark == playerX || mark == playerO)

        return getCellContents(x, y) == mark
    }

    fun markCellWith(x: Int, y: Int, mark: PlayerMark): Board {
        require(mark == playerX || mark == playerO)
        require(x in 0..2 && y in 0..2)
        require(!isCellOccupied(x, y))

        return Board(cells = cells.copy(x + y * 3, mark))
    }

    private fun checkSingleWinOption(opt: List<Pair<Int, Int>>, mark: PlayerMark) =
        opt.all { (x, y) -> isCellOccupiedWith(x, y, mark) }

    private fun checkSingleWinner(mark: PlayerMark) =
        winPositionOptions.any { checkSingleWinOption(it, mark) }

    fun checkForWinner(): PlayerMark? = when {
        checkSingleWinner(playerX) -> playerX
        checkSingleWinner(playerO) -> playerO
        else -> null
    }

    override fun toString(): String = cells
        .map { it?.value?.toUpperCase() ?: "_" }
        .chunked(3)
        .joinToString(separator = "\n")
}

private fun <E> List<E>.copy(index: Int, newValue: E): List<E> =
//    this.toMutableList().apply { set(index, value) }
    this.mapIndexed { current, original -> if (current == index) newValue else original }

class Game(
    private val board: Board = Board.createInitialBoard(),
    private val winner: PlayerMark? = null
) {

    fun hasWinner() = winner != null

    fun getWinner(): PlayerMark {
        require(hasWinner())
        return winner as PlayerMark
    }

    fun makeAutoMove(mark: PlayerMark, rnd: Int): Game {
        require(!hasWinner())

        val nboard = if (!board.isCellOccupied(1, 1)) {
            board.markCellWith(1, 1, mark)
        } else {
            val opts = board.getOpenCells()
            val (x, y) = opts.random(Random(rnd))
            board.markCellWith(x, y, mark)
        }
        return Game(board = nboard, winner = nboard.checkForWinner())
    }

    fun makeExplicitMove(x: Int, y: Int, mark: PlayerMark): Game {
        require(!board.isCellOccupied(x, y))

        val nboard = board.markCellWith(x, y, mark)
        return Game(board = nboard, winner = nboard.checkForWinner())
    }

    override fun toString() = board.toString()
}

fun main() {
    var game = Game()

    game = game.makeAutoMove(Board.playerX, 0)
    game = game.makeAutoMove(Board.playerO, 1)
    game = game.makeAutoMove(Board.playerX, 2)
    game = game.makeAutoMove(Board.playerO, 3)

    game = game.makeExplicitMove(2, 0, Board.playerX)

    println(game)

    if (game.hasWinner()) {
        println("\nPlayer ${game.getWinner().value} won!")
    }
}