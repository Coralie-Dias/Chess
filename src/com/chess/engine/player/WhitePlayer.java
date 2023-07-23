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

public class WhitePlayer extends Player {
    public WhitePlayer(final Board board,
                       final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Collection<chessPiece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.blackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals,
                                                    final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();
        if(this.playerKing.isFirstMove() && !this.isInCheck()) {
            //whites king side castle
            if (!this.board.getSquare(61).isSquareActive() &&
                !this.board.getSquare(62).isSquareActive()) {
                final Square rookSquare = this.board.getSquare(63);
                if (rookSquare.isSquareActive() && rookSquare.getPiece().isFirstMove()) {
                    if (Player.calculateAttacksOnSquare(61, opponentLegals).isEmpty() &&
                        Player.calculateAttacksOnSquare(62, opponentLegals).isEmpty() &&
                        rookSquare.getPiece().getPieceType() == chessPiece.PieceType.ROOK) {
                        kingCastles.add(new KingSideCastleMove(this.board,
                                                               this.playerKing,
                                              62,
                                                               (Rook)rookSquare.getPiece(),
                                                               rookSquare.getSquareCoordinate(),
                                              61));
                    }
                }
            }
            //whites queen side castle
            if (!this.board.getSquare(59).isSquareActive() &&
                    !this.board.getSquare(58).isSquareActive() &&
                    !this.board.getSquare(57).isSquareActive()) {
                final Square rookSquare = this.board.getSquare(56);
                if (rookSquare.isSquareActive() && rookSquare.getPiece().isFirstMove() &&
                        (Player.calculateAttacksOnSquare(58, opponentLegals).isEmpty() &&
                         Player.calculateAttacksOnSquare(59, opponentLegals).isEmpty() &&
                         rookSquare.getPiece().getPieceType() == chessPiece.PieceType.ROOK)) {
                    kingCastles.add(new QueenSideCastleMove(this.board,
                                                            this.playerKing,
                                                            58,
                                                            (Rook)rookSquare.getPiece(),
                                                            rookSquare.getSquareCoordinate(),
                                                            59));
                }
            }
        }
        return Collections.unmodifiableList(kingCastles);
    }
}
