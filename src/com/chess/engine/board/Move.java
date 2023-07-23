package com.chess.engine.board;

import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Rook;
import com.chess.engine.pieces.chessPiece;

import static com.chess.engine.board.Board.*;

public abstract class Move {

    protected final Board board;
    protected final chessPiece pieceToMove;
    protected final int destinationCoordinate;
    protected final boolean isFirstMove;

    public static final Move NULL_MOVE = new NullMove();

    private Move(final Board board, final chessPiece pieceToMove, final int destinationCoordinate) {
        this.board = board;
        this.pieceToMove = pieceToMove;
        this.destinationCoordinate = destinationCoordinate;
        this.isFirstMove = pieceToMove.isFirstMove();
    }

    private Move(final Board board,
                 final int destinationCoordinate) {
        this.board = board;
        this.destinationCoordinate = destinationCoordinate;
        this.pieceToMove = null;
        this.isFirstMove = false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.destinationCoordinate;
        result = prime * result + this.pieceToMove.hashCode();
        result = prime * result + this.pieceToMove.getPiecePosition();
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if(this == other) {
            return true;
        }
        if(!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getCurrentCoordinate() == otherMove.getCurrentCoordinate() &&
               getDestinationCoordinate() == otherMove.getDestinationCoordinate() &&
               getPieceToMove().equals(otherMove.getPieceToMove());
    }

    public Board getBoard() {
        return this.board;
    }

    public int getCurrentCoordinate() {
        return this.getPieceToMove().getPiecePosition();
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public chessPiece getPieceToMove() {
        return this.pieceToMove;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public chessPiece getAttackedPiece() {
        return null;
    }

    public Board execute() {
        final Builder builder  = new Builder();
        for(final chessPiece piece : this.board.currentPlayer().getActivePieces()) {
            if(!this.pieceToMove.equals(piece)) {
                builder.setPiece(piece);
            }
        }
        for(final chessPiece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }
        //Move the moved piece!
        builder.setPiece(this.pieceToMove.movePiece(this));
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        return builder.build();
    }

    public static class MajorAttackMove extends AttackMove {

        public MajorAttackMove(final Board board,
                               final chessPiece pieceMoved,
                               final int destinationCoordinate,
                               final chessPiece pieceAttacked) {
            super(board, pieceMoved, destinationCoordinate, pieceAttacked);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof MajorAttackMove && super.equals(other);
        }

        @Override
        public String toString() {
            return pieceToMove.getPieceType() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }

    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board,
                         final chessPiece pieceToMove,
                         final int destinationCoordinate) {
            super(board, pieceToMove, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof MajorMove && super.equals(other);
        }

        @Override
        public String toString() {
            return pieceToMove.getPieceType().toString() + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class AttackMove extends Move {

        final chessPiece attackedPiece;

        public AttackMove(final Board board,
                          final chessPiece pieceToMove,
                          final int destinationCoordinate,
                          final chessPiece attackedPiece) {
            super(board, pieceToMove, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public int hashCode() {
            return this.attackedPiece.hashCode() + super.hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            if(this == other) {
                return true;
            }
            if(!(other instanceof AttackMove)) {
                return false;
            }
            final AttackMove otherAttackMove = (AttackMove) other;
            return super.equals(otherAttackMove) && getAttackedPiece().equals(otherAttackMove.getAttackedPiece());
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public chessPiece getAttackedPiece() {
            return this.attackedPiece;
        }
    }

    public static final class PawnMove extends Move {
        public PawnMove(final Board board,
                        final chessPiece pieceToMove,
                        final int destinationCoordinate) {
            super(board, pieceToMove, destinationCoordinate);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnMove && super.equals(other);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    public static class PawnAttackMove extends AttackMove {
        public PawnAttackMove(final Board board,
                              final chessPiece pieceToMove,
                              final int destinationCoordinate,
                              final chessPiece attackedPiece) {
            super(board, pieceToMove, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnAttackMove && super.equals(other);
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.pieceToMove.getPiecePosition()).substring(0, 1) + "x" +
                   BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }


    public static final class PawnEnPassantAttackMove extends MajorMove.AttackMove {
        public PawnEnPassantAttackMove(final Board board,
                                       final chessPiece pieceToMove,
                                       final int destinationCoordinate,
                                       final chessPiece attackedPiece) {
            super(board, pieceToMove, destinationCoordinate, attackedPiece);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof PawnEnPassantAttackMove && super.equals(other);
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();
            for(final chessPiece piece : this.board.currentPlayer().getActivePieces()) {
                if(!this.pieceToMove.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for(final chessPiece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                if(!piece.equals(this.getAttackedPiece())) {
                    builder.setPiece(piece);
                }
            }
            builder.setPiece(this.pieceToMove.movePiece(this));
            builder.setMoveMaker((this.board.currentPlayer().getOpponent().getAlliance()));
            return builder.build();
        }
    }

    public static class PawnPromotion extends Move {

        final Move decoratedMove;
        final Pawn promotedPawn;

        // Design Pattern - decorator
        public PawnPromotion(final Move decoratedMove) {
            super(decoratedMove.getBoard(), decoratedMove.getPieceToMove(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) decoratedMove.getPieceToMove();
        }

        @Override
        public int hashCode() {
            return decoratedMove.hashCode() + (31 * promotedPawn.hashCode());
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof  PawnPromotion && (super.equals(other));
        }

        @Override
        public Board execute() {

            final Board pawnMoveBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Builder();
            for(final chessPiece piece : pawnMoveBoard.currentPlayer().getActivePieces()) {
                if(!this.promotedPawn.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final chessPiece piece : pawnMoveBoard.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.promotedPawn.getPromotionPiece().movePiece(this));
            builder.setMoveMaker(pawnMoveBoard.currentPlayer().getAlliance());
            return builder.build();
        }

        @Override
        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public chessPiece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();

        }

        @Override
        public String toString() {
            return "";
        }
    }

    public static final class PawnJump extends Move {
        public PawnJump(final Board board,
                        final chessPiece pieceToMove,
                        final int destinationCoordinate) {
            super(board, pieceToMove, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Builder builder = new Builder();
            for (final chessPiece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.pieceToMove.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final chessPiece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            final Pawn movedPawn = (Pawn) this.pieceToMove.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public String toString() {
            return BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
        }
    }

    static abstract class CastleMove extends Move {

        protected final Rook castleRook;
        protected final int castleRookStart;
        protected final int castleRookDestination;

        public CastleMove(final Board board,
                          final chessPiece pieceToMove,
                          final int destinationCoordinate,
                          final Rook castleRook,
                          final int castleRookStart,
                          final int castleRookDestination) {
            super(board, pieceToMove, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        public Rook getCastleRook() {
            return this.castleRook;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {

            final Builder builder = new Builder();
            for (final chessPiece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.pieceToMove.equals(piece) && !this.castleRook.equals(piece)) {
                    builder.setPiece(piece);
                }
            }
            for (final chessPiece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }
            builder.setPiece(this.pieceToMove.movePiece(this));
            //todo look into the first move on normal pieces
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.castleRook.hashCode();
            result = prime * result + this.castleRookDestination;
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            if(this == other) {
                return true;
            }
            if(!(other instanceof CastleMove)) {
                return false;
            }
            final CastleMove otherCastleMove = (CastleMove)other;
            return super.equals(otherCastleMove) && this.castleRook.equals(otherCastleMove.getCastleRook());
        }
    }

    public static final class KingSideCastleMove extends CastleMove {
        public KingSideCastleMove(final Board board,
                                  final chessPiece pieceToMove,
                                  final int destinationCoordinate,
                                  final Rook castleRook,
                                  final int castleRookStart,
                                  final int castleRookDestination) {
            super(board, pieceToMove, destinationCoordinate, castleRook,castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof KingSideCastleMove && super.equals(other);
        }

        @Override
        public String toString() {
            return "0-0";
        }
    }

    public static final class QueenSideCastleMove extends CastleMove {
        public QueenSideCastleMove(final Board board,
                                   final chessPiece pieceToMove,
                                   final int destinationCoordinate,
                                   final Rook castleRook,
                                   final int castleRookStart,
                                   final int castleRookDestination) {
            super(board, pieceToMove, destinationCoordinate, castleRook,castleRookStart, castleRookDestination);
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || other instanceof QueenSideCastleMove && super.equals(other);
        }

        @Override
        public String toString() {
            return "0-0-0";
        }
    }

    public static final class NullMove extends Move {
        public NullMove() {
            super(null, -1);
        }

        @Override
        public Board execute() {
            throw new RuntimeException("Cannot execute null move!");
        }

        @Override
        public int getCurrentCoordinate() {
            return -1;
        }
    }

    public static class MoveFactory {
        private MoveFactory() {
            throw new RuntimeException("Not instantiable!");
        }
        public static Move createMove(final Board board,
                                      final int currentCoordinate,
                                      final int destinationCoordinate) {
            for (final Move move : board.getAllLegalMoves()) {
                if (move.getCurrentCoordinate() == currentCoordinate &&
                        move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return NULL_MOVE;
        }
    }
}
