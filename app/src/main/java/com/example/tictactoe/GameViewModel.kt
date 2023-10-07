package com.example.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class GameViewModel : ViewModel() {
    var state by mutableStateOf(GameState())
    var winCell = 0

    val boardItems: MutableMap<Int, BoardCellValue> = mutableMapOf(
        1 to BoardCellValue.NONE,
        2 to BoardCellValue.NONE,
        3 to BoardCellValue.NONE,
        4 to BoardCellValue.NONE,
        5 to BoardCellValue.NONE,
        6 to BoardCellValue.NONE,
        7 to BoardCellValue.NONE,
        8 to BoardCellValue.NONE,
        9 to BoardCellValue.NONE,
    )

    fun onAction(action: UserAction) {
        when (action) {
            is UserAction.BoardTapped -> {
                addValueToBoard(action.cellNo)
            }

            UserAction.PlayAgainButtonClicked -> {
                gameReset()

            }
        }
    }
    private var beginTurn = state.beginTurn
    private fun gameReset() {
        boardItems.forEach { (i, _) ->
            boardItems[i] = BoardCellValue.NONE
        }
        beginTurn = if (beginTurn == BoardCellValue.CIRCLE){
            BoardCellValue.CROSS
        }
        else{
            BoardCellValue.CIRCLE
        }
        changeBeginTurn()
    }
    private fun changeBeginTurn() {
        val nextTurn = if (beginTurn == BoardCellValue.CIRCLE) BoardCellValue.CROSS else BoardCellValue.CIRCLE
        val nextHintText = if (nextTurn == BoardCellValue.CIRCLE) "Player 'O' turn" else "Computer turn"

        state = state.copy(
            hintText = nextHintText,
            currentTurn = nextTurn,
            victoryType = VictoryType.NONE,
            hasWon = false
        )

        if (nextTurn == BoardCellValue.CROSS) {
            computerMove()
        }
    }


    private fun addValueToBoard(cellNo: Int) {

        if (boardItems[cellNo] != BoardCellValue.NONE) {
            return
        }
        if (state.currentTurn == BoardCellValue.CIRCLE) {
            boardItems[cellNo] = BoardCellValue.CIRCLE
            state = if (checkForVictory(BoardCellValue.CIRCLE)) {
                state.copy(
                    hintText = "Player 'O' Won",
                    playerCircleCount = state.playerCircleCount + 1,
                    currentTurn = BoardCellValue.NONE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "Player 'X' turn",
                    currentTurn = BoardCellValue.CROSS
                )
            }
        } else if (state.currentTurn == BoardCellValue.CROSS) {
            boardItems[cellNo] = BoardCellValue.CROSS
            state = if (checkForVictory(BoardCellValue.CROSS)) {
                state.copy(
                    hintText = "Computer(X) Won",
                    playerCrossCount = state.playerCrossCount + 1,
                    currentTurn = BoardCellValue.NONE,
                    hasWon = true
                )
            } else if (hasBoardFull()) {
                state.copy(
                    hintText = "Game Draw",
                    drawCount = state.drawCount + 1
                )
            } else {
                state.copy(
                    hintText = "Player 'O' turn",
                    currentTurn = BoardCellValue.CIRCLE
                )
            }
        }
        if(!hasBoardFull() && state.currentTurn == BoardCellValue.CROSS){
            computerMove()
        }
    }
    private fun canWin(boardValue: BoardCellValue):Boolean{
        when {
            ((boardItems[2] == boardValue && boardItems[3] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[7] == boardValue)) && boardItems[1] == BoardCellValue.NONE -> {
                winCell = 1
                return true
            }
            ((boardItems[1] == boardValue && boardItems[3] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[8] == boardValue)) && boardItems[2] == BoardCellValue.NONE -> {
                winCell = 2
                return true
            }
            ((boardItems[1] == boardValue && boardItems[2] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[7] == boardValue)||
                    (boardItems[6] == boardValue && boardItems[9] == boardValue)) && boardItems[3] == BoardCellValue.NONE -> {
                winCell = 3
                return true
            }
            ((boardItems[1] == boardValue && boardItems[7] == boardValue)||
                    (boardItems[5] == boardValue && boardItems[6] == boardValue)) && boardItems[4] == BoardCellValue.NONE -> {
                winCell = 4
                return true
            }
            ((boardItems[2] == boardValue && boardItems[8] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[6] == boardValue)||
                    (boardItems[1] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[7] == boardValue)) && boardItems[5] == BoardCellValue.NONE -> {
                winCell = 5
                return true
            }
            ((boardItems[3] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[4] == boardValue && boardItems[5] == boardValue)) && boardItems[6] == BoardCellValue.NONE -> {
                winCell = 6
                return true
            }
            ((boardItems[1] == boardValue && boardItems[4] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[5] == boardValue)||
                    (boardItems[8] == boardValue && boardItems[9] == boardValue)) && boardItems[7] == BoardCellValue.NONE -> {
                winCell = 7
                return true
            }
            ((boardItems[7] == boardValue && boardItems[9] == boardValue)||
                    (boardItems[2] == boardValue && boardItems[5] == boardValue)) && boardItems[8] == BoardCellValue.NONE -> {
                winCell = 8
                return true
            }
            ((boardItems[1] == boardValue && boardItems[5] == boardValue)||
                    (boardItems[3] == boardValue && boardItems[6] == boardValue)||
                    (boardItems[7] == boardValue && boardItems[8] == boardValue)) && boardItems[9] == BoardCellValue.NONE -> {
                winCell = 9
                return true
            }
            else -> return false
        }
    }
    private fun canBlock():Boolean{
        if(canWin(BoardCellValue.CIRCLE)){
            return true
        }
        return false
    }
    private fun middleFree():Boolean{
        return boardItems[5] == BoardCellValue.NONE
    }
    private fun computerMove() {
        // Delay before the computer's move (Player X)
        GlobalScope.launch {
            delay(1500) // Delay for 1 second (you can adjust the delay duration as needed)

            if (canWin(BoardCellValue.CROSS)) {
                addValueToBoard(winCell)
            } else if (canBlock()) {
                addValueToBoard(winCell)
            } else if (middleFree()) {
                addValueToBoard(5)
            } else {
                var randomCell = 0
                while (true) {
                    randomCell = Random.nextInt(1, 10)
                    if (boardItems[randomCell] == BoardCellValue.NONE) {
                        addValueToBoard(randomCell)
                        break
                    }
                }
            }
        }
    }

    private fun checkForVictory(boardValue: BoardCellValue): Boolean {
        when {
            boardItems[1] == boardValue && boardItems[2] == boardValue && boardItems[3] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL1)
                return true
            }

            boardItems[4] == boardValue && boardItems[5] == boardValue && boardItems[6] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL2)
                return true
            }

            boardItems[7] == boardValue && boardItems[8] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.HORIZONTAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[4] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL1)
                return true
            }

            boardItems[2] == boardValue && boardItems[5] == boardValue && boardItems[8] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL2)
                return true
            }

            boardItems[3] == boardValue && boardItems[6] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.VERTICAL3)
                return true
            }

            boardItems[1] == boardValue && boardItems[5] == boardValue && boardItems[9] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL1)
                return true
            }

            boardItems[3] == boardValue && boardItems[5] == boardValue && boardItems[7] == boardValue -> {
                state = state.copy(victoryType = VictoryType.DIAGONAL2)
                return true
            }

            else -> return false
        }
    }

    public fun hasBoardFull(): Boolean {
        return !boardItems.containsValue(BoardCellValue.NONE)
    }
}