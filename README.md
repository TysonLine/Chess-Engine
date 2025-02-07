# TysonChess (Rating of 850)
Created by Tyson Line and Rafael Robin (@Robin-in-in)

**TysonChess** is a fully functional chess engine implemented in Java. It is inspired by a high-performance C engine Sophia by @bartekspitza. It utilizes advanced techniques such as bitboards, magic bitboards, Zobrist hashing, and an alpha–beta search with move ordering and transposition tables. The engine supports the Universal Chess Interface (UCI) protocol, making it compatible with many chess GUIs.

## Table of Contents

- [Overview](#overview)
- [Board Representation](#board-representation)
- [FEN Parsing and Board Initialization](#fen-parsing-and-board-initialization)
- [Move Generation](#move-generation)
  - [Non-Sliding Pieces](#non-sliding-pieces)
  - [Sliding Pieces (Magic Bitboards)](#sliding-pieces-magic-bitboards)
  - [Special Moves](#special-moves)
- [Zobrist Hashing and Transposition Table](#zobrist-hashing-and-transposition-table)
- [Evaluation Function](#evaluation-function)
- [Search Algorithm: Alpha–Beta with Move Ordering](#search-algorithm-alpha–beta-with-move-ordering)
- [SAN (Standard Algebraic Notation) Conversion](#san-standard-algebraic-notation-conversion)
- [UCI Protocol Interface](#uci-protocol-interface)
- [Interactive UI](#interactive-ui)
- [Algorithms Summary](#algorithms-summary)
- [How to Play](#how-to-play)

## Overview

TysonChess is a chess engine written in Java. It leverages modern techniques for fast move generation and efficient search and communicates via the UCI protocol. Whether you use a graphical interface or a physical board, TysonChess can calculate moves for you.

## Board Representation

### Bitboards

- **Concept:**  
  The board is represented by 64-bit long integers (bitboards), with each bit corresponding to one square (0 = a1, …, 63 = h8).  
- **Piece Representation:**  
  There are 12 bitboards in total (one for each piece type: white pawns, white knights, …, black kings).  
- **Occupancy Masks:**  
  The engine maintains separate masks for white, black, and overall occupancy, allowing quick lookups and fast move generation.

## FEN Parsing and Board Initialization

- **FEN (Forsyth–Edwards Notation):**  
  FEN strings encode a complete chess position, including:
  - Piece placement (e.g., `rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR`)
  - Side to move (`w` or `b`)
  - Castling rights (e.g., `KQkq`)
  - En passant target square (e.g., `-` or `e3`)
  - Halfmove clock and fullmove number
- **Initialization:**  
  The engine uses the FEN string (stored as `Constants.START_FEN` for the standard starting position) to set up its internal board via the `Fen.java` class.

## Move Generation

### Non-Sliding Pieces

- **Pawns:**  
  - **Pushing:** Single and double pawn pushes are generated by shifting the pawn bitboards.
  - **Attacks:** Precomputed attack tables for pawns (east and west) are used.
  - **Promotion:** Moves are generated for each promotion possibility when a pawn reaches the last rank.
- **Knights and Kings:**  
  Precomputed move masks (stored in arrays such as `KNIGHT_MOVEMENT` and `KING_MOVEMENT`) are used for fast move generation.

### Sliding Pieces (Magic Bitboards)

- **Magic Bitboards:**  
  - For bishops and rooks (and hence queens), the engine uses magic numbers and occupancy masks to quickly look up legal moves.
  - Each square has a movement mask (computed via helper methods such as `bishopMovement` and `rookMovement`) and a magic number (stored in `Magics.java`).
  - The product of the occupancy (restricted to the mask) and the magic number, shifted by a fixed amount, yields an index into a precomputed attack table.
- **Initialization:**  
  The `initBishopRookAttackTables()` method (with helper methods `bishopMovement` and `rookMovement`) computes these tables.

### Special Moves

- **Castling:**  
  Generated by checking castling rights and ensuring that squares between the king and rook are unoccupied. Castling moves are represented by the king’s move (e.g., `e1g1` for white kingside).
- **En Passant:**  
  When a pawn moves two squares, an en passant square is set. Special handling in `pushMove` (via `makeEnPassantMove`) processes these moves.
- **Promotions:**  
  When a pawn reaches the final rank, moves are generated for promotion to queen, rook, bishop, or knight.

## Zobrist Hashing and Transposition Table

- **Zobrist Hashing:**  
  Each board position is assigned a unique 64-bit hash computed by XORing precomputed random bitboards for each piece on each square, for en passant squares, for castling rights, and for the side to move (see `Zobrist.java`).
- **Transposition Table (TT):**  
  A fixed-size TT caches evaluation results and best moves for positions (see `TT.java` and `TTEntry.java`), which is used during search to avoid redundant computations.

## Evaluation Function

- **Evaluation:**  
  The engine evaluates a position by summing the values of all pieces and adds bonuses or penalties based on piece–square tables (see `Evaluation.java`).
- **Piece-Square Tables:**  
  Separate tables for white and mirrored versions for black provide positional scoring.
- **Special Cases:**  
  Checkmate, stalemate, and insufficient material are handled to adjust the evaluation score.

## Search Algorithm: Alpha–Beta with Move Ordering

- **Alpha–Beta Search:**  
  The engine uses a depth-first search with alpha–beta pruning to efficiently explore the game tree.
- **Move Ordering:**  
  Moves are scored using MVV–LVA (Most Valuable Victim – Least Valuable Aggressor) heuristics and prioritized to improve pruning.
- **Transposition Table Integration:**  
  The search routine first checks the TT. If a result exists for the current position (and with sufficient depth), the stored evaluation is used.
- **Implementation:**  
  See `Search.java` for the recursive alpha–beta search and board copy routines.

## SAN (Standard Algebraic Notation) Conversion

- **Conversion Functions:**  
  The `San.java` class converts moves to and from coordinate notation (e.g., `e2e4`, `e7e8q` for promotions, and `e1g1` for castling).

## UCI Protocol Interface

- **UCI Commands:**  
  The engine supports commands such as `isready`, `position`, `go`, `quit`, and `uci` via the UCI protocol.
- **Communication:**  
  The `UCI.java` class listens for UCI commands, updates the internal board position accordingly, and initiates the search when prompted.

## Interactive UI

- **Command-Line Play:**  
  The `InteractiveUI.java` class (if included) allows you to play a game interactively from the console by typing moves in coordinate notation.
- **Physical Board Use:**  
  Use your physical board to follow along as the engine calculates its moves.

## Algorithms Summary

- **Bitwise Operations:**  
  Used extensively for fast board updates and move generation.
- **Magic Bitboards:**  
  Provide efficient sliding piece move generation via precomputed masks and magic numbers.
- **Zobrist Hashing:**  
  Quickly computes unique identifiers for board positions, essential for the transposition table.
- **Alpha–Beta Search:**  
  A recursive search that prunes branches that will not affect the final decision.
- **Transposition Table:**  
  Caches evaluations of positions to prevent redundant calculations.
- **Move Ordering (MVV–LVA):**  
  Prioritizes captures and critical moves to improve search efficiency.
- **Evaluation Function:**  
  Combines material values and positional bonuses for an overall score.

## How to Play

If you have a physical board, you can play interactively with the engine using the provided command-line UI:

1. Run the engine (e.g., via `InteractiveUI.java`).
2. Type your move in coordinate notation (e.g., `e2e4`).
3. The engine will calculate its move and output it (e.g., `e7e5`).
4. Update your physical board and repeat by entering your next move.
5. To exit, type `quit`.

Alternatively, load TysonChess in a UCI-compatible GUI to play against it.

## Conclusion

TysonChess is a full-featured chess engine built using advanced techniques in move generation, evaluation, and search. It supports UCI for compatibility with graphical interfaces and also provides an interactive command-line UI for direct play. Enjoy playing with TysonChess!
