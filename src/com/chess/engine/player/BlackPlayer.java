package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Move;
import com.chess.engine.board.Square;
import com.chess.engine.board.Board;
import com.chess.engine.pieces.Rook;
import com.chess.engine.pieces.chessPiece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.chess.engine.board.Move.*;

public class BlackPlayer extends Player {
    public BlackPlayer(final Board board, final Collection<Move> whiteStandardLegalMoves, final Collection<Move> blackStandardLegalMoves) {
        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<chessPiece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals,
                                                    final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        if(this.playerKing.isFirstMove() && !this.isInCheck()) {
            //blacks king side castle
            if (!this.board.getSquare(5).isSquareActive() && !this.board.getSquare(6).isSquareActive()) {
                final Square rookSquare = this.board.getSquare(7);
                if (rookSquare.isSquareActive() && rookSquare.getPiece().isFirstMove()) {
                    if (Player.calculateAttacksOnSquare(5, opponentLegals).isEmpty() &&
                            Player.calculateAttacksOnSquare(6, opponentLegals).isEmpty() &&
                            rookSquare.getPiece().getPieceType() == chessPiece.PieceType.ROOK) {
                        kingCastles.add(new KingSideCastleMove(this.board,
                                this.playerKing,
                                6,
                                (Rook)rookSquare.getPiece(),
                                rookSquare.getSquareCoordinate(),
                                5));
                    }
                }
            }
            //blacks queen side castle
            if (!this.board.getSquare(1).isSquareActive() &&
                    !this.board.getSquare(2).isSquareActive() &&
                    !this.board.getSquare(3).isSquareActive()) {
                final Square rookSquare = this.board.getSquare(0);
                if (rookSquare.isSquareActive() && rookSquare.getPiece().isFirstMove() &&
                    (Player.calculateAttacksOnSquare(2, opponentLegals).isEmpty() &&
                     Player.calculateAttacksOnSquare(3, opponentLegals).isEmpty() &&
                     rookSquare.getPiece().getPieceType() == chessPiece.PieceType.ROOK)) {
                    kingCastles.add(new QueenSideCastleMove(this.board,
                                this.playerKing,
                                2,
                                (Rook)rookSquare.getPiece(),
                                rookSquare.getSquareCoordinate(),
                                3));
                }
            }
        }
        return Collections.unmodifiableList(kingCastles);    }
}
