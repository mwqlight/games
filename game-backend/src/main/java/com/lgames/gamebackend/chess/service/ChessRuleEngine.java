package com.lgames.gamebackend.chess.service;

import com.lgames.gamebackend.chess.model.ChessMove;
import com.lgames.gamebackend.chess.model.ChessPiece;
import com.lgames.gamebackend.chess.model.ChessPieceType;
import com.lgames.gamebackend.chess.model.ChessPlayer;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChessRuleEngine {
    public List<ChessPiece> initializeBoard() {
        List<ChessPiece> board = new ArrayList<>();

        // Place pawns
        for (int i = 0; i < 8; i++) {
            board.add(new ChessPiece(ChessPieceType.PAWN, ChessPlayer.WHITE, 1, i));
            board.add(new ChessPiece(ChessPieceType.PAWN, ChessPlayer.BLACK, 6, i));
        }

        // Place rooks
        board.add(new ChessPiece(ChessPieceType.ROOK, ChessPlayer.WHITE, 0, 0));
        board.add(new ChessPiece(ChessPieceType.ROOK, ChessPlayer.WHITE, 0, 7));
        board.add(new ChessPiece(ChessPieceType.ROOK, ChessPlayer.BLACK, 7, 0));
        board.add(new ChessPiece(ChessPieceType.ROOK, ChessPlayer.BLACK, 7, 7));

        // Place knights
        board.add(new ChessPiece(ChessPieceType.KNIGHT, ChessPlayer.WHITE, 0, 1));
        board.add(new ChessPiece(ChessPieceType.KNIGHT, ChessPlayer.WHITE, 0, 6));
        board.add(new ChessPiece(ChessPieceType.KNIGHT, ChessPlayer.BLACK, 7, 1));
        board.add(new ChessPiece(ChessPieceType.KNIGHT, ChessPlayer.BLACK, 7, 6));

        // Place bishops
        board.add(new ChessPiece(ChessPieceType.BISHOP, ChessPlayer.WHITE, 0, 2));
        board.add(new ChessPiece(ChessPieceType.BISHOP, ChessPlayer.WHITE, 0, 5));
        board.add(new ChessPiece(ChessPieceType.BISHOP, ChessPlayer.BLACK, 7, 2));
        board.add(new ChessPiece(ChessPieceType.BISHOP, ChessPlayer.BLACK, 7, 5));

        // Place queens
        board.add(new ChessPiece(ChessPieceType.QUEEN, ChessPlayer.WHITE, 0, 3));
        board.add(new ChessPiece(ChessPieceType.QUEEN, ChessPlayer.BLACK, 7, 3));

        // Place kings
        board.add(new ChessPiece(ChessPieceType.KING, ChessPlayer.WHITE, 0, 4));
        board.add(new ChessPiece(ChessPieceType.KING, ChessPlayer.BLACK, 7, 4));

        return board;
    }

    public boolean isValidMove(List<ChessPiece> board, ChessMove move) {
        ChessPiece piece = getPieceAtPosition(board, move.getFromRow(), move.getFromCol());
        if (piece == null || piece.getPlayer() != move.getPlayer()) {
            return false;
        }

        if (isSamePlayerPieceAtPosition(board, move.getToRow(), move.getToCol(), move.getPlayer())) {
            return false;
        }

        switch (piece.getType()) {
            case PAWN:
                return isValidPawnMove(board, move);
            case ROOK:
                return isValidRookMove(board, move);
            case KNIGHT:
                return isValidKnightMove(board, move);
            case BISHOP:
                return isValidBishopMove(board, move);
            case QUEEN:
                return isValidQueenMove(board, move);
            case KING:
                return isValidKingMove(board, move);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(List<ChessPiece> board, ChessMove move) {
        ChessPlayer player = move.getPlayer();
        int direction = player == ChessPlayer.WHITE ? 1 : -1;
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        // Move forward one square
        if (toCol == fromCol && toRow == fromRow + direction && getPieceAtPosition(board, toRow, toCol) == null) {
            return true;
        }

        // Move forward two squares from starting position
        if (toCol == fromCol && toRow == fromRow + 2 * direction && getPieceAtPosition(board, toRow, toCol) == null) {
            if (player == ChessPlayer.WHITE && fromRow == 1) {
                return getPieceAtPosition(board, fromRow + direction, fromCol) == null;
            } else if (player == ChessPlayer.BLACK && fromRow == 6) {
                return getPieceAtPosition(board, fromRow + direction, fromCol) == null;
            }
        }

        // Capture diagonally
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            ChessPiece capturedPiece = getPieceAtPosition(board, toRow, toCol);
            return capturedPiece != null && capturedPiece.getPlayer() != player;
        }

        return false;
    }

    private boolean isValidRookMove(List<ChessPiece> board, ChessMove move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        int rowStep = fromRow == toRow ? 0 : (toRow > fromRow ? 1 : -1);
        int colStep = fromCol == toCol ? 0 : (toCol > fromCol ? 1 : -1);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (getPieceAtPosition(board, currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    private boolean isValidKnightMove(List<ChessPiece> board, ChessMove move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(List<ChessPiece> board, ChessMove move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
            return false;
        }

        int rowStep = toRow > fromRow ? 1 : -1;
        int colStep = toCol > fromCol ? 1 : -1;

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow && currentCol != toCol) {
            if (getPieceAtPosition(board, currentRow, currentCol) != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    private boolean isValidQueenMove(List<ChessPiece> board, ChessMove move) {
        return isValidRookMove(board, move) || isValidBishopMove(board, move);
    }

    private boolean isValidKingMove(List<ChessPiece> board, ChessMove move) {
        int fromRow = move.getFromRow();
        int fromCol = move.getFromCol();
        int toRow = move.getToRow();
        int toCol = move.getToCol();

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        return rowDiff <= 1 && colDiff <= 1;
    }

    public void makeMove(List<ChessPiece> board, ChessMove move) {
        ChessPiece piece = getPieceAtPosition(board, move.getFromRow(), move.getFromCol());
        if (piece == null) {
            return;
        }

        ChessPiece capturedPiece = getPieceAtPosition(board, move.getToRow(), move.getToCol());
        if (capturedPiece != null) {
            board.remove(capturedPiece);
        }

        piece.setRow(move.getToRow());
        piece.setCol(move.getToCol());
    }

    public boolean isCheck(List<ChessPiece> board, ChessPlayer player) {
        ChessPiece king = findKing(board, player);
        if (king == null) {
            return false;
        }

        ChessPlayer opponent = player == ChessPlayer.WHITE ? ChessPlayer.BLACK : ChessPlayer.WHITE;
        for (ChessPiece piece : board) {
            if (piece.getPlayer() == opponent) {
                ChessMove move = new ChessMove(piece.getRow(), piece.getCol(), king.getRow(), king.getCol(), opponent);
                if (isValidMove(board, move)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isCheckmate(List<ChessPiece> board, ChessPlayer player) {
        if (!isCheck(board, player)) {
            return false;
        }

        return !hasValidMoves(board, player);
    }

    public boolean isStalemate(List<ChessPiece> board, ChessPlayer player) {
        if (isCheck(board, player)) {
            return false;
        }

        return !hasValidMoves(board, player);
    }

    private boolean hasValidMoves(List<ChessPiece> board, ChessPlayer player) {
        for (ChessPiece piece : board) {
            if (piece.getPlayer() == player) {
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        ChessMove move = new ChessMove(piece.getRow(), piece.getCol(), toRow, toCol, player);
                        if (isValidMove(board, move)) {
                            // Make the move and check if it leaves the king in check
                            ChessPiece capturedPiece = getPieceAtPosition(board, toRow, toCol);
                            piece.setRow(toRow);
                            piece.setCol(toCol);
                            if (capturedPiece != null) {
                                board.remove(capturedPiece);
                            }

                            boolean isCheck = isCheck(board, player);

                            // Undo the move
                            piece.setRow(move.getFromRow());
                            piece.setCol(move.getFromCol());
                            if (capturedPiece != null) {
                                board.add(capturedPiece);
                            }

                            if (!isCheck) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private ChessPiece getPieceAtPosition(List<ChessPiece> board, int row, int col) {
        for (ChessPiece piece : board) {
            if (piece.getRow() == row && piece.getCol() == col) {
                return piece;
            }
        }
        return null;
    }

    private boolean isSamePlayerPieceAtPosition(List<ChessPiece> board, int row, int col, ChessPlayer player) {
        ChessPiece piece = getPieceAtPosition(board, row, col);
        return piece != null && piece.getPlayer() == player;
    }

    private ChessPiece findKing(List<ChessPiece> board, ChessPlayer player) {
        for (ChessPiece piece : board) {
            if (piece.getType() == ChessPieceType.KING && piece.getPlayer() == player) {
                return piece;
            }
        }
        return null;
    }
}