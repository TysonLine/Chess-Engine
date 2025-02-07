package engine;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class EngineTests {

    // This method is run before each test to ensure a fresh board
    private Board createBoardFromFen(String fen) {
        Board board = new Board();
        Fen.setFen(board, fen);
        return board;
    }
    
    @Before
    public void setUp() {
        // Initialize common engine components once
        Bitboards.initBitboards();
        Zobrist.initZobrist();
        MoveGen.initMoveGeneration();
        Evaluation.initEvaluation();
    }
    
    // Test FEN parsing for side to move
    @Test
    public void testParseFenTurn() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1";
        Board board = createBoardFromFen(fen);
        assertEquals("Board turn should be BLACK", Constants.BLACK, board.turn);
    }
    
    // Test that piece bitboards are set (i.e. not zero) after FEN parsing
    @Test
    public void testParseFenPieceMasks() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1";
        Board board = createBoardFromFen(fen);
        // Check that at least one white pawn and one black pawn exist.
        assertNotEquals("White pawn bitboard should not be zero", 0, board.pawnW);
        assertNotEquals("Black pawn bitboard should not be zero", 0, board.pawnB);
    }
    
    // Test FEN parsing for en passant square
    @Test
    public void testParseFenEpSquare() {
        // For fen "… e3" the en passant target square should be set.
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e3 0 1";
        Board board = createBoardFromFen(fen);
        // Calculate expected square:
        // File: 'h' - 'e' = 3; Rank: '3' -> 3 - 1 = 2; square = 2*8 + 3 = 19.
        assertEquals("En passant square should be 19", 19, board.epSquare);
    }
    
    // Test FEN parsing for king squares (using a custom fen)
    @Test
    public void testParseFenKingSquares() {
        // In this FEN, assume white king is on d3 and black king is on e6.
        String fen = "8/8/4k3/8/8/3K4/8/8 b - - 0 1";
        Board board = createBoardFromFen(fen);
        // The actual square numbers depend on our numbering (0 = a1, …, 63 = h8).
        // Here we assume: d3 = 3 + 2*8 = 19, e6 = 4 + 5*8 = 44.
        assertEquals("White king square should be 19", 19, board.whiteKingSq);
        assertEquals("Black king square should be 44", 44, board.blackKingSq);
    }
    
    // Test FEN parsing for castling rights
    @Test
    public void testParseFenCastlingRights() {
        String fen = "r3k3/8/8/8/8/8/8/4K2R b Kq - 0 1";
        Board board = createBoardFromFen(fen);
        int expected = Constants.K | Constants.q;
        assertEquals("Castling rights should be K and q", expected, board.castling);
    }
    
    // Test that the hash computed during FEN parsing matches the computed hash value.
    @Test
    public void testParseFenHash() {
        Board board = createBoardFromFen(Constants.START_FEN);
        long computedHash = Zobrist.hash(board);
        assertEquals("Board hash should match computed hash", computedHash, board.hash);
    }
    
    // Test move-to-SAN conversion for a normal move.
    @Test
    public void testMoveToSanNormal() {
        // For testing, we define a move from e2 to e3.
        // (Assume we have defined appropriate constants for squares in Constants.java.)
        Move move = new Move(Constants.E2_INDEX, Constants.E3_INDEX, Constants.NO_PROMOTION, 0, 0);
        String san = San.moveToSan(move);
        assertEquals("e2e3", san);
    }
    
    // Test SAN-to-move conversion for a normal move.
    @Test
    public void testSanToMoveNormal() {
        Board board = createBoardFromFen(Constants.START_FEN);
        Move move = San.sanToMove(board, "e2e3");
        assertEquals("Move from-square should be e2", Constants.E2_INDEX, move.fromSquare);
        assertEquals("Move to-square should be e3", Constants.E3_INDEX, move.toSquare);
    }
    
    // Test a simple pushMove and verify that the board hash changes.
    @Test
    public void testPushMoveHashNoCapture() {
        Board board = createBoardFromFen(Constants.START_FEN);
        long oldHash = board.hash;
        Move move = San.sanToMove(board, "e2e3");
        // Push move (assuming pushMove properly updates the board state and hash)
        MoveGen.pushMove(board, move);
        long newHash = board.hash;
        assertNotEquals("Hash should change after a non-capture move", oldHash, newHash);
    }
    
    // Test evaluation function (starting position should be near 0)
    @Test
    public void testEvaluation() {
        Board board = createBoardFromFen(Constants.START_FEN);
        List<Move> moves = MoveGen.legalMoves(board);
        int res = Evaluation.result(board, moves);
        int eval = Evaluation.evaluate(board, res);
        assertTrue("Evaluation of starting position should be near 0", Math.abs(eval) < 100);
    }
    
    // Additional tests for castling, promotion, and en passant can be added similarly.
}
