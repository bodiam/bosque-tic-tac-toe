package nsmain

import kotlin.random.Random

typealias PlayerMark = String

class Board(private var cells: List<PlayerMark?>) {

    companion object {
        const val playerX: PlayerMark = "x" as PlayerMark
        const val playerO: PlayerMark = "o" as PlayerMark

        var allCellPositions : List<Pair<Int, Int>> = listOf(
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

        //Board is a list of marks, indexed by x,y coords from upper left 0 based
//    var cells = emptyList<PlayerMark?>()

        fun createInitialBoard(): Board {
            return Board(cells = List(9) { null })
        }
    }

    override fun toString(): String {
        return cells.chunked(3).joinToString (separator = "\n")
    }

    fun getOpenCells(): List<Pair<Int, Int>> {
        return Board.allCellPositions.filter { pos ->
            !this.isCellOccupied(pos.first, pos.second)
        }
    }

    fun getCellContents(x: Int, y: Int): PlayerMark? {
        require( 0 <= x && x < 3 && 0 <= y && y < 3)

        return this.cells[x + y * 3]
    }

    fun isCellOccupied(x: Int, y: Int): Boolean {
        return this.getCellContents(x, y) != null
    }

    fun isCellOccupiedWith(x: Int, y: Int, mark: PlayerMark): Boolean {
        require(mark == Board.playerX || mark == Board.playerO)

        return this.getCellContents(x, y) == mark
    }

    fun markCellWith(x: Int, y: Int, mark: PlayerMark): Board {
        require(mark == Board.playerX || mark == Board.playerO)
        require(0 <= x && x < 3 && 0 <= y && y < 3)
        require(!this.isCellOccupied(x, y))

//      return Board(cells = this.cells.withCopy { set(x + y * 3, mark) })
        return Board(cells = this.cells.asCopy(x + y * 3, mark))
    }

    private fun checkSingleWinOption(opt: List<Pair<Int, Int>>, mark: PlayerMark): Boolean {
        return opt.all { entry -> this.isCellOccupiedWith(entry.first, entry.second, mark) }
    }

    private fun checkSingleWinner(mark: PlayerMark): Boolean {
        return Board.winPositionOptions.any { opt -> checkSingleWinOption(opt, mark) }
    }

    fun checkForWinner(): PlayerMark? {
        if (this.checkSingleWinner(Board.playerX)) {
            return Board.playerX
        } else if (this.checkSingleWinner(Board.playerO)) {
            return Board.playerO
        } else {
            return null
        }
    }
}

private fun <E> List<E>.withCopy(action: MutableList<E>.() -> Unit): List<E> {
    return this.toMutableList().apply { action() }
}

private fun <E> List<E>.asCopy(index: Int, value: E): List<E> {
    return this.toMutableList().apply { set(index, value) }
}

class Game(
     var board: Board = Board.createInitialBoard(),
    private var winner: PlayerMark? = null
) {

    fun hasWinner(): Boolean {
        return winner != null
    }

    fun getWinner(): PlayerMark {
        require(hasWinner())
        return this.winner as PlayerMark
    }

    fun makeAutoMove(mark: PlayerMark, rnd: Int): Game {
        require(!hasWinner())

        var nboard: Board
        if (!this.board.isCellOccupied(1, 1)) {
            nboard = this.board.markCellWith(1, 1, mark)
        } else {
            val opts = this.board.getOpenCells()
            val tup = opts.random(Random(rnd))
            nboard = this.board.markCellWith(tup.first, tup.second, mark)
        }
        return Game(board = nboard, winner = nboard.checkForWinner())
    }

    fun makeExplicitMove(x: Int, y: Int, mark: PlayerMark): Game {
        require(!this.board.isCellOccupied(x, y))

        var nboard = this.board.markCellWith(x, y, mark)
        return Game(board = nboard, winner = nboard.checkForWinner())
    }
}

fun main() {
    var game = Game()

    game = game.makeAutoMove(Board.playerX, 0)
    game = game.makeAutoMove(Board.playerO, 1)
    game = game.makeAutoMove(Board.playerX, 2)

    game = game.makeExplicitMove(2, 0, Board.playerO)
    game = game.makeExplicitMove(2, 1, Board.playerX)
    game = game.makeExplicitMove(0, 0, Board.playerO)

    println(game.board)

    if(game.hasWinner()) {
        println(game.getWinner())
    }
}