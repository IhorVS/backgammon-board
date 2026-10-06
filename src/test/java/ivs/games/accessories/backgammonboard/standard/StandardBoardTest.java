package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BackgammonBoard;
import ivs.games.accessories.backgammonboard.BackgammonBoardView;
import ivs.games.accessories.backgammonboard.BoardCell;
import ivs.games.accessories.backgammonboard.BoardState;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import ivs.games.accessories.backgammonboard.CheckerType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests mutable access to a standard backgammon board, checker transfers,
 * structural validation, board copying, and restoration from state.
 */
@SuppressWarnings("DataFlowIssue")
class StandardBoardTest {

    private final BackgammonBoard<TestChecker> board = new StandardBoard<>();

    /**
     * Verifies that all 24 playing cells and four special cells
     * are distinct objects.
     */
    @Test
    void allCellsAreDistinct() {
        List<BoardCell<TestChecker>> cells = new ArrayList<>();

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            cells.add(board.getCell(index));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            cells.add(board.getBar(color));
            cells.add(board.getBorneOff(color));
        }

        assertEquals(28, cells.size());

        for (int first = 0; first < cells.size(); first++) {
            for (int second = first + 1; second < cells.size(); second++) {
                assertNotSame(cells.get(first), cells.get(second));
            }
        }
    }

    /**
     * Verifies that playing cells preserve a stack containing
     * checkers of different colors and types.
     */
    @Test
    void playingCellsAcceptMixedColorsAndTypes() {
        board.getCell(0).setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.WHITE_EMIR));

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.WHITE_EMIR), board.getCell(0).getState());
    }

    /**
     * Verifies that bars and borne-off cells reject the opposite color
     * through both push and state replacement, preserving their contents.
     */
    @Test
    void specialCellsEnforceTheirAssignedColor() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            TestChecker own = man(color);
            TestChecker opponent =
                    man(StandardCheckerColor.getOpposite(color));

            for (BoardCell<TestChecker> cell :
                    List.of(board.getBar(color), board.getBorneOff(color))) {

                cell.push(own);

                assertThrows(IllegalArgumentException.class,
                        () -> cell.push(opponent));
                assertThrows(IllegalArgumentException.class,
                        () -> cell.setState(List.of(own, opponent)));

                assertEquals(List.of(own), cell.getState());
            }
        }
    }

    /**
     * Verifies that a transfer moves only the top checker and permits
     * placement on opposing checkers without evaluating game rules.
     * Moving the checker back restores the original stacks.
     */
    @Test
    void movesOnlyTopCheckerBetweenPlayingCellsWithoutApplyingGameRules() {
        board.getCell(0).setState(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR));
        board.getCell(23).setState(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_MAN));

        board.move(0, 23);

        assertEquals(List.of(TestChecker.BLACK_MAN), board.getCell(0).getState());
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR), board.getCell(23).getState());

        board.move(23, 0);

        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR), board.getCell(0).getState());
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_MAN), board.getCell(23).getState());
    }

    /**
     * Verifies that out-of-range source and destination indices
     * are rejected without changing any cell.
     */
    @Test
    void rejectsInvalidMoveIndicesWithoutChangingBoard() {
        board.getCell(0).push(TestChecker.WHITE_MAN);
        board.getCell(23).push(TestChecker.BLACK_MAN);

        for (int invalid : new int[]{-1, 24}) {
            assertUnchangedAfterFailure(IllegalArgumentException.class,
                    () -> board.move(invalid, 23));
            assertUnchangedAfterFailure(IllegalArgumentException.class,
                    () -> board.move(0, invalid));
        }
    }

    /**
     * Verifies that moving a checker to its source cell is rejected
     * without changing the board.
     */
    @Test
    void rejectsMoveToSameCellWithoutChangingBoard() {
        board.getCell(0).push(TestChecker.WHITE_MAN);

        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.move(0, 0));
    }

    /**
     * Verifies that an empty playing cell cannot provide a checker
     * and that the destination remains unchanged.
     */
    @Test
    void rejectsMoveFromEmptyPlayingCellWithoutChangingBoard() {
        board.getCell(23).push(TestChecker.BLACK_MAN);

        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.move(0, 23));
    }

    /**
     * Verifies for both colors that the top checker is added to its own bar.
     * The lower checker stays in place and the opposite bar remains empty.
     */
    @Test
    void movesTopCheckerToBarSelectedByItsColor() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.clear();
            TestChecker own = emir(color);
            TestChecker opponent =
                    man(StandardCheckerColor.getOpposite(color));

            board.getCell(5).setState(List.of(opponent, own));
            board.getBar(color).push(man(color));

            board.moveToBar(5);

            assertEquals(List.of(opponent), board.getCell(5).getState());
            assertEquals(List.of(man(color), own), board.getBar(color).getState());
            assertTrue(board.getBar(
                    StandardCheckerColor.getOpposite(color)).isEmpty());
        }
    }

    /**
     * Verifies for both colors that only the top checker leaves the selected
     * bar and is placed on top of the destination playing cell.
     */
    @Test
    void movesTopCheckerFromSpecifiedBarToPlayingCell() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.clear();
            TestChecker own = emir(color);
            TestChecker opponent =
                    man(StandardCheckerColor.getOpposite(color));

            board.getBar(color).setState(List.of(man(color), own));
            board.getCell(23).push(opponent);

            board.moveFromBar(color, 23);

            assertEquals(List.of(man(color)), board.getBar(color).getState());
            assertEquals(List.of(opponent, own), board.getCell(23).getState());
        }
    }

    /**
     * Verifies for both colors that the top checker is added to its own
     * borne-off cell, leaving the lower checker and opposite area unchanged.
     */
    @Test
    void movesTopCheckerToBorneOffCellSelectedByItsColor() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.clear();
            TestChecker own = emir(color);
            TestChecker opponent =
                    man(StandardCheckerColor.getOpposite(color));

            board.getCell(5).setState(List.of(opponent, own));
            board.getBorneOff(color).push(man(color));

            board.moveToBorneOff(5);

            assertEquals(List.of(opponent), board.getCell(5).getState());
            assertEquals(List.of(man(color), own),
                    board.getBorneOff(color).getState());
            assertTrue(board.getBorneOff(
                    StandardCheckerColor.getOpposite(color)).isEmpty());
        }
    }

    /**
     * Verifies that special transfers reject out-of-range playing cell indices
     * before any checker is removed or added.
     */
    @Test
    void rejectsInvalidIndicesForSpecialMovesWithoutChangingBoard() {
        board.getCell(0).push(TestChecker.WHITE_MAN);
        board.getBar(StandardCheckerColor.WHITE).push(TestChecker.WHITE_EMIR);

        for (int invalid : new int[]{-1, 24}) {
            assertUnchangedAfterFailure(IllegalArgumentException.class,
                    () -> board.moveToBar(invalid));
            assertUnchangedAfterFailure(IllegalArgumentException.class,
                    () -> board.moveToBorneOff(invalid));
            assertUnchangedAfterFailure(IllegalArgumentException.class,
                    () -> board.moveFromBar(StandardCheckerColor.WHITE, invalid));
        }
    }

    /**
     * Verifies that transfers to special cells and transfers from a bar
     * reject empty sources without changing the board.
     */
    @Test
    void rejectsSpecialMovesFromEmptySourcesWithoutChangingBoard() {
        board.getCell(23).push(TestChecker.BLACK_MAN);

        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.moveToBar(0));
        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.moveToBorneOff(0));
        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.moveFromBar(StandardCheckerColor.WHITE, 23));
    }

    /**
     * Verifies that a bar transfer rejects null and unsupported colors.
     * A custom color with code W is not the standard WHITE enum constant.
     */
    @Test
    void rejectsNullOrUnsupportedBarColorWithoutChangingBoard() {
        board.getCell(0).push(TestChecker.WHITE_MAN);
        CheckerColor unsupported = () -> "W";

        assertUnchangedAfterFailure(NullPointerException.class,
                () -> board.moveFromBar(null, 0));
        assertUnchangedAfterFailure(IllegalArgumentException.class,
                () -> board.moveFromBar(unsupported, 0));
    }

    /**
     * Verifies that a playing cell accepts a checker with a custom color,
     * but transfers to standard special cells reject it without losing it.
     */
    @Test
    void unsupportedCheckerColorCannotBeMovedToSpecialCells() {
        CheckerColor unsupported = () -> "W";
        CheckerType type = () -> "M";

        Checker<CheckerType, CheckerColor> checker = new Checker<>() {
            @Override
            public String getCode() {
                return "WM";
            }

            @Override
            public CheckerType getType() {
                return type;
            }

            @Override
            public CheckerColor getColor() {
                return unsupported;
            }
        };

        BackgammonBoard<Checker<?, ?>> customBoard = new StandardBoard<>();
        customBoard.getCell(0).push(checker);
        List<List<Checker<?, ?>>> before = snapshot(customBoard);

        assertThrows(IllegalArgumentException.class,
                () -> customBoard.moveToBar(0));
        assertEquals(before, snapshot(customBoard));

        assertThrows(IllegalArgumentException.class,
                () -> customBoard.moveToBorneOff(0));
        assertEquals(before, snapshot(customBoard));
    }

    /**
     * Verifies that repeated clearing empties every cell without replacing
     * the cell objects or invalidating existing references.
     */
    @Test
    void clearsEveryCellAndPreservesCellReferences() {
        List<BoardCell<TestChecker>> references = new ArrayList<>();

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            BoardCell<TestChecker> cell = board.getCell(index);
            cell.push(TestChecker.WHITE_MAN);
            references.add(cell);
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            BoardCell<TestChecker> bar = board.getBar(color);
            BoardCell<TestChecker> borneOff = board.getBorneOff(color);
            bar.push(man(color));
            borneOff.push(emir(color));
            references.add(bar);
            references.add(borneOff);
        }

        board.clear();
        board.clear();

        assertTrue(board.isEmpty());
        references.forEach(cell -> assertTrue(cell.isEmpty()));

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            assertSame(references.get(index), board.getCell(index));
        }

        int offset = board.getPlayingCellCount();
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertSame(references.get(offset++), board.getBar(color));
            assertSame(references.get(offset++), board.getBorneOff(color));
        }
    }

    /**
     * Verifies that copying through the read-only interface preserves all stacks,
     * creates distinct cells, and shares immutable checker objects.
     * Changes to either board do not affect the other.
     */
    @Test
    void copiesAllCellsThroughViewAndKeepsBoardsIndependent() {
        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            board.getCell(index).setState(List.of(
                    TestChecker.WHITE_MAN,
                    TestChecker.BLACK_EMIR));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.getBar(color).setState(List.of(man(color), emir(color)));
            board.getBorneOff(color).setState(List.of(emir(color), man(color)));
        }

        BackgammonBoard<TestChecker> copy = new StandardBoard<>(board);

        assertEquals(snapshot(board), snapshot(copy));

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            assertNotSame(board.getCell(index), copy.getCell(index));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertNotSame(board.getBar(color), copy.getBar(color));
            assertNotSame(board.getBorneOff(color), copy.getBorneOff(color));

            assertSame(board.getBar(color).peek(), copy.getBar(color).peek());
            assertSame(board.getBorneOff(color).peek(), copy.getBorneOff(color).peek());

            TestChecker opponent = man(StandardCheckerColor.getOpposite(color));

            assertThrows(IllegalArgumentException.class,
                    () -> copy.getBar(color).push(opponent));
            assertThrows(IllegalArgumentException.class,
                    () -> copy.getBorneOff(color).push(opponent));
        }

        assertSame(board.getCell(0).peek(), copy.getCell(0).peek());

        List<List<TestChecker>> copiedState = snapshot(copy);
        board.clear();
        assertEquals(copiedState, snapshot(copy));

        copy.moveToBar(0);
        assertTrue(board.isEmpty());
    }

    /**
     * Verifies that copying an empty board produces independent empty cells
     * and preserves the assigned colors of special cells.
     */
    @Test
    void copiesEmptyBoard() {
        BackgammonBoard<TestChecker> copy = new StandardBoard<>(board);

        assertTrue(copy.isEmpty());
        assertEquals(24, copy.getPlayingCellCount());

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            assertNotSame(board.getCell(index), copy.getCell(index));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertNotSame(board.getBar(color), copy.getBar(color));
            assertNotSame(board.getBorneOff(color), copy.getBorneOff(color));

            copy.getBar(color).push(man(color));
            copy.getBorneOff(color).push(emir(color));

            TestChecker opponent = man(StandardCheckerColor.getOpposite(color));

            assertThrows(IllegalArgumentException.class,
                    () -> copy.getBar(color).push(opponent));
            assertThrows(IllegalArgumentException.class,
                    () -> copy.getBorneOff(color).push(opponent));
        }

        assertTrue(board.isEmpty());
    }

    /**
     * Verifies that the copying constructor rejects a null source.
     */
    @Test
    void rejectsNullCopySource() {
        assertThrows(NullPointerException.class, () -> new StandardBoard<>((BackgammonBoardView<?>) null));
    }

    /**
     * Verifies that the constructor rejects a null board state.
     */
    @Test
    void rejectNullBoardState() {
        assertThrows(NullPointerException.class, () -> new StandardBoard<>((BoardState<?>) null));
    }

    /**
     * Verifies that an incompatible playing cell count is rejected
     * before the constructor attempts to access any source cell.
     */
    @Test
    void rejectsCopySourceWithDifferentCellCountBeforeCellAccess() {
        BackgammonBoardView<TestChecker> source = new StandardBoard<>() {
            @Override
            public int getPlayingCellCount() {
                return 12;
            }

            @Override
            public BoardCell<TestChecker> getCell(int index) {
                return fail("Invalid source must be rejected before cell access");
            }
        };

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> new StandardBoard<>(source));

        assertEquals("Source board has 12 playing cells; expected 24", exception.getMessage());
    }

    /**
     * Verifies that copying fails when the source cannot provide
     * a bar for a required standard color.
     */
    @Test
    void rejectsCopySourceWithUnsupportedStandardColors() {
        BackgammonBoardView<TestChecker> source = new StandardBoard<>() {
            @Override
            public BoardCell<TestChecker> getBar(CheckerColor color) {
                throw new IllegalArgumentException("Unsupported source color");
            }
        };

        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(source));
    }

    /**
     * Verifies that copying validates special cell contents and rejects
     * a black checker supplied as part of a white bar or borne-off cell.
     */
    @Test
    void rejectsCopySourceWithWrongColorInSpecialCell() {
        StandardCell<TestChecker> invalidCell = new StandardCell<>();
        invalidCell.push(TestChecker.BLACK_MAN);

        BackgammonBoardView<TestChecker> invalidBarSource = new StandardBoard<>() {
            @Override
            public BoardCell<TestChecker> getBar(CheckerColor color) {
                return color == StandardCheckerColor.WHITE
                        ? invalidCell : super.getBar(color);
            }
        };

        BackgammonBoardView<TestChecker> invalidBorneOffSource =
                new StandardBoard<>() {
                    @Override
                    public BoardCell<TestChecker> getBorneOff(CheckerColor color) {
                        return color == StandardCheckerColor.WHITE
                                ? invalidCell : super.getBorneOff(color);
                    }
                };

        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBarSource));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBorneOffSource));
    }

    /**
     * Verifies that restoration preserves every stack and checker identity.
     * Restored cells are independent of the source board and other restorations.
     * Changes to any board do not affect the supplied state.
     */
    @Test
    void restoresAllCellsFromStateAndKeepsBoardsIndependent() {
        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            board.getCell(index).setState(List.of(
                    man(index % 2 == 0
                            ? StandardCheckerColor.WHITE
                            : StandardCheckerColor.BLACK),
                    TestChecker.BLACK_EMIR,
                    TestChecker.WHITE_EMIR));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.getBar(color).setState(List.of(man(color), emir(color)));
            board.getBorneOff(color).setState(List.of(emir(color), man(color)));
        }

        BoardState<TestChecker> state = board.getState();
        BackgammonBoard<TestChecker> restored = new StandardBoard<>(state);
        BackgammonBoard<TestChecker> second = new StandardBoard<>(state);
        List<List<TestChecker>> expected = snapshot(board);

        assertEquals(expected, snapshot(restored));
        assertEquals(expected, snapshot(second));

        for (int index = 0; index < board.getPlayingCellCount(); index++) {
            assertNotSame(board.getCell(index), restored.getCell(index));
            assertNotSame(restored.getCell(index), second.getCell(index));
            assertSame(state.getCells().get(index).getLast(),
                    restored.getCell(index).peek());
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertNotSame(board.getBar(color), restored.getBar(color));
            assertNotSame(board.getBorneOff(color), restored.getBorneOff(color));
            assertNotSame(restored.getBar(color), second.getBar(color));
            assertNotSame(restored.getBorneOff(color), second.getBorneOff(color));

            assertSame(state.getBars().get(color).getLast(),
                    restored.getBar(color).peek());
            assertSame(state.getBorneOff().get(color).getLast(),
                    restored.getBorneOff(color).peek());

            TestChecker opponent = man(StandardCheckerColor.getOpposite(color));

            assertThrows(IllegalArgumentException.class,
                    () -> restored.getBar(color).push(opponent));
            assertThrows(IllegalArgumentException.class,
                    () -> restored.getBorneOff(color).push(opponent));
        }

        board.clear();

        assertEquals(expected, snapshot(restored));
        assertEquals(expected, snapshot(second));

        restored.clear();

        assertTrue(board.isEmpty());
        assertEquals(expected, snapshot(second));
        assertEquals(state.getCells(), second.getState().getCells());
        assertEquals(state.getBars(), second.getState().getBars());
        assertEquals(state.getBorneOff(), second.getState().getBorneOff());
    }

    /**
     * Verifies that an empty state creates an empty standard board.
     * Special cells retain their assigned colors despite having no checkers.
     */
    @Test
    void restoresEmptyStateAndPreservesSpecialCellColors() {
        BoardState<TestChecker> state = board.getState();
        BackgammonBoard<TestChecker> restored = new StandardBoard<>(state);

        assertTrue(restored.isEmpty());
        assertEquals(24, restored.getPlayingCellCount());

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            restored.getBar(color).push(man(color));
            restored.getBorneOff(color).push(emir(color));

            TestChecker opponent = man(StandardCheckerColor.getOpposite(color));

            assertThrows(IllegalArgumentException.class,
                    () -> restored.getBar(color).push(opponent));
            assertThrows(IllegalArgumentException.class,
                    () -> restored.getBorneOff(color).push(opponent));

            assertEquals(List.of(man(color)), restored.getBar(color).getState());
            assertEquals(List.of(emir(color)), restored.getBorneOff(color).getState());
            assertTrue(state.getBars().get(color).isEmpty());
            assertTrue(state.getBorneOff().get(color).isEmpty());
        }

        assertTrue(board.isEmpty());
    }

    /**
     * Verifies that states with fewer or more than 24 playing cells
     * are rejected.
     */
    @Test
    void rejectsStateWithDifferentPlayingCellCount() {
        BoardState<TestChecker> empty = board.getState();

        BoardState<TestChecker> smaller = new BoardState<>(
                empty.getCells().subList(0, 23),
                empty.getBars(),
                empty.getBorneOff());

        List<List<TestChecker>> largerCells = new ArrayList<>(empty.getCells());
        largerCells.add(List.of());

        BoardState<TestChecker> larger = new BoardState<>(
                largerCells,
                empty.getBars(),
                empty.getBorneOff());

        IllegalArgumentException smallerException =
                assertThrows(IllegalArgumentException.class, () -> new StandardBoard<>(smaller));
        IllegalArgumentException largerException =
                assertThrows(IllegalArgumentException.class, () -> new StandardBoard<>(larger));

        assertEquals("State has 23 playing cells; expected 24", smallerException.getMessage());
        assertEquals("State has 25 playing cells; expected 24", largerException.getMessage());
    }

    /**
     * Verifies that bar and borne-off maps must contain both standard colors.
     */
    @Test
    void rejectsStateWithMissingSpecialCellColor() {
        BoardState<TestChecker> empty = board.getState();
        Map<CheckerColor, List<TestChecker>> incomplete =
                Map.of(StandardCheckerColor.WHITE, List.of());

        BoardState<TestChecker> invalidBars = new BoardState<>(
                empty.getCells(),
                incomplete,
                empty.getBorneOff());

        BoardState<TestChecker> invalidBorneOff = new BoardState<>(
                empty.getCells(),
                empty.getBars(),
                incomplete);

        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBars));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBorneOff));
    }

    /**
     * Verifies that special cell maps reject additional colors,
     * even when both required standard colors are present.
     */
    @Test
    void rejectsStateWithAdditionalSpecialCellColor() {
        BoardState<TestChecker> empty = board.getState();
        CheckerColor unsupported = () -> "X";

        Map<CheckerColor, List<TestChecker>> extraColor = Map.of(
                StandardCheckerColor.WHITE, List.of(),
                StandardCheckerColor.BLACK, List.of(),
                unsupported, List.of());

        BoardState<TestChecker> invalidBars = new BoardState<>(
                empty.getCells(),
                extraColor,
                empty.getBorneOff());

        BoardState<TestChecker> invalidBorneOff = new BoardState<>(
                empty.getCells(),
                empty.getBars(),
                extraColor);

        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBars));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBorneOff));
    }

    /**
     * Verifies that a custom color with code W cannot replace
     * the standard WHITE key in either special cell map.
     */
    @Test
    void rejectsStateWithNonstandardSpecialCellColor() {
        BoardState<TestChecker> empty = board.getState();
        CheckerColor unsupported = () -> "W";

        Map<CheckerColor, List<TestChecker>> nonstandard = Map.of(
                unsupported, List.of(),
                StandardCheckerColor.BLACK, List.of());

        BoardState<TestChecker> invalidBars = new BoardState<>(
                empty.getCells(),
                nonstandard,
                empty.getBorneOff());

        BoardState<TestChecker> invalidBorneOff = new BoardState<>(
                empty.getCells(),
                empty.getBars(),
                nonstandard);

        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBars));
        assertThrows(IllegalArgumentException.class,
                () -> new StandardBoard<>(invalidBorneOff));
    }

    /**
     * Verifies that restoration rejects a checker of the wrong color
     * after a valid checker in either a bar or a borne-off stack.
     * The exception identifies the offending checker index.
     */
    @Test
    void rejectsStateWithWrongColorInSpecialCell() {
        BoardState<TestChecker> empty = board.getState();

        Map<CheckerColor, List<TestChecker>> invalid = Map.of(
                StandardCheckerColor.WHITE,
                List.of(TestChecker.WHITE_MAN, TestChecker.BLACK_EMIR),
                StandardCheckerColor.BLACK,
                List.of());

        BoardState<TestChecker> invalidBars = new BoardState<>(
                empty.getCells(),
                invalid,
                empty.getBorneOff());

        BoardState<TestChecker> invalidBorneOff = new BoardState<>(
                empty.getCells(),
                empty.getBars(),
                invalid);

        IllegalArgumentException barException =
                assertThrows(IllegalArgumentException.class, () -> new StandardBoard<>(invalidBars));
        IllegalArgumentException borneOffException =
                assertThrows(IllegalArgumentException.class, () -> new StandardBoard<>(invalidBorneOff));

        assertEquals("Checker at index 1 has color B; expected W", barException.getMessage());
        assertEquals("Checker at index 1 has color B; expected W", borneOffException.getMessage());
    }

    /**
     * Verifies that empty playing cells remain independent after restoration.
     * Adding a checker to one cell does not populate any other cell
     * or modify the supplied state.
     */
    @Test
    void restoredEmptyPlayingCellsAreIndependent() {
        BoardState<TestChecker> state = board.getState();
        BackgammonBoard<TestChecker> restored = new StandardBoard<>(state);

        restored.getCell(0).push(TestChecker.WHITE_MAN);

        assertEquals(List.of(TestChecker.WHITE_MAN), restored.getCell(0).getState());
        assertTrue(IntStream.range(1, restored.getPlayingCellCount())
                .allMatch(index -> restored.getCell(index).isEmpty()));
        assertTrue(state.getCells().stream().allMatch(List::isEmpty));
    }

    /**
     * Verifies that an operation throws the expected exception
     * and preserves the contents of every board cell.
     *
     * @param exceptionType the expected exception class
     * @param operation     the operation expected to fail
     * @param <E>           the expected exception type
     */
    private <E extends Throwable> void assertUnchangedAfterFailure(
            Class<E> exceptionType,
            Runnable operation) {

        List<List<TestChecker>> before = snapshot(board);

        assertThrows(exceptionType, operation::run);

        assertEquals(before, snapshot(board));
    }

    /**
     * Captures all cell contents for comparison.
     *
     * <p>Playing cells are recorded in traversal order, followed by the bar
     * and borne-off cell for each standard color in enum declaration order.
     * Checker order within each cell is preserved.</p>
     *
     * @param board the board to inspect
     * @param <P>   the checker type
     * @return an unmodifiable list of unmodifiable cell snapshots
     */
    private static <P extends Checker<?, ?>> List<List<P>> snapshot(
            BackgammonBoardView<P> board) {

        List<List<P>> state = new ArrayList<>();
        board.forEachCell((index, cell) -> state.add(cell.getState()));

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            state.add(board.getBar(color).getState());
            state.add(board.getBorneOff(color).getState());
        }

        return List.copyOf(state);
    }

    /**
     * Returns the ordinary test checker of the specified standard color.
     *
     * @param color the standard color
     * @return the corresponding ordinary checker
     */
    private static TestChecker man(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_MAN
                : TestChecker.BLACK_MAN;
    }

    /**
     * Returns the emir test checker of the specified standard color.
     *
     * @param color the standard color
     * @return the corresponding emir checker
     */
    private static TestChecker emir(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_EMIR
                : TestChecker.BLACK_EMIR;
    }
}
