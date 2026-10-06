package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BackgammonBoard;
import ivs.games.accessories.backgammonboard.BackgammonBoardView;
import ivs.games.accessories.backgammonboard.BoardCellView;
import ivs.games.accessories.backgammonboard.BoardState;
import ivs.games.accessories.backgammonboard.CheckerColor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests read access to a standard backgammon board through
 * {@link BackgammonBoardView}, including cell access, validation,
 * traversal, state snapshots, and visibility of changes made
 * through the mutable interface.
 */
@SuppressWarnings("DataFlowIssue")
class StandardBoardViewTest {

    private final BackgammonBoard<TestChecker> board = new StandardBoard<>();
    private final BackgammonBoardView<TestChecker> view = board;

    /**
     * Verifies that a new board exposes 24 empty playing cells
     * and empty bar and borne-off cells for both standard colors.
     */
    @Test
    void exposesAllCellsOfEmptyBoard() {
        assertEquals(24, view.getPlayingCellCount());
        assertTrue(view.isEmpty());

        for (int index = 0; index < view.getPlayingCellCount(); index++) {
            assertTrue(view.getCell(index).isEmpty());
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertTrue(view.getBar(color).isEmpty());
            assertTrue(view.getBorneOff(color).isEmpty());
        }
    }

    /**
     * Verifies that the read-only and mutable interfaces return
     * the same underlying cell objects rather than separate copies.
     */
    @Test
    void returnsSameCellsAsFullInterface() {
        for (int index = 0; index < view.getPlayingCellCount(); index++) {
            assertSame(board.getCell(index), view.getCell(index));
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            assertSame(board.getBar(color), view.getBar(color));
            assertSame(board.getBorneOff(color), view.getBorneOff(color));
        }
    }

    /**
     * Verifies that previously obtained cell views reflect additions
     * and clearing performed through the mutable board interface.
     */
    @Test
    void cellViewsReflectChangesThroughFullInterface() {
        BoardCellView<TestChecker> playing = view.getCell(0);
        BoardCellView<TestChecker> bar =
                view.getBar(StandardCheckerColor.WHITE);
        BoardCellView<TestChecker> borneOff =
                view.getBorneOff(StandardCheckerColor.BLACK);

        board.getCell(0).push(TestChecker.WHITE_MAN);
        board.getBar(StandardCheckerColor.WHITE).push(TestChecker.WHITE_EMIR);
        board.getBorneOff(StandardCheckerColor.BLACK).push(TestChecker.BLACK_MAN);

        assertEquals(List.of(TestChecker.WHITE_MAN), playing.getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR), bar.getState());
        assertEquals(List.of(TestChecker.BLACK_MAN), borneOff.getState());

        board.clear();

        assertTrue(playing.isEmpty());
        assertTrue(bar.isEmpty());
        assertTrue(borneOff.isEmpty());
    }

    /**
     * Verifies that a checker in any playing, bar, or borne-off cell
     * makes the board non-empty, and removing it restores the empty state.
     */
    @Test
    void isEmptyIncludesPlayingBarAndBorneOffCells() {
        for (int index = 0; index < view.getPlayingCellCount(); index++) {
            board.getCell(index).push(TestChecker.WHITE_MAN);
            assertFalse(view.isEmpty());
            board.getCell(index).clear();
            assertTrue(view.isEmpty());
        }

        for (StandardCheckerColor color : StandardCheckerColor.values()) {
            board.getBar(color).push(man(color));
            assertFalse(view.isEmpty());
            board.getBar(color).clear();
            assertTrue(view.isEmpty());

            board.getBorneOff(color).push(man(color));
            assertFalse(view.isEmpty());
            board.getBorneOff(color).clear();
            assertTrue(view.isEmpty());
        }
    }

    /**
     * Verifies that playing cell access rejects indices outside
     * the valid range, including extreme integer values.
     */
    @Test
    void rejectsInvalidPlayingCellIndices() {
        for (int index : new int[]{-1, 24, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class,
                    () -> view.getCell(index));
        }
    }

    /**
     * Verifies that bar and borne-off cell access reject a null color.
     */
    @Test
    void rejectsNullSpecialCellColors() {
        assertThrows(NullPointerException.class, () -> view.getBar(null));
        assertThrows(NullPointerException.class, () -> view.getBorneOff(null));
    }

    /**
     * Verifies that a custom color with code W is not treated
     * as the standard WHITE enum constant.
     */
    @Test
    void rejectsNonstandardColorEvenWithStandardCode() {
        CheckerColor unsupported = () -> "W";

        assertThrows(IllegalArgumentException.class,
                () -> view.getBar(unsupported));
        assertThrows(IllegalArgumentException.class,
                () -> view.getBorneOff(unsupported));
    }

    /**
     * Verifies that traversal visits every playing cell exactly once
     * in ascending index order, including empty cells.
     * Occupied bar and borne-off cells are excluded.
     */
    @Test
    void traversesAllPlayingCellsInIndexOrderAndExcludesSpecialCells() {
        board.getCell(7).push(TestChecker.WHITE_MAN);
        board.getBar(StandardCheckerColor.WHITE).push(TestChecker.WHITE_EMIR);
        board.getBorneOff(StandardCheckerColor.BLACK).push(TestChecker.BLACK_MAN);

        List<Integer> indices = new ArrayList<>();
        List<BoardCellView<TestChecker>> visited = new ArrayList<>();

        view.forEachCell((index, cell) -> {
            indices.add(index);
            visited.add(cell);
        });

        assertEquals(IntStream.range(0, 24).boxed().toList(), indices);
        assertEquals(24, visited.size());

        for (int index = 0; index < visited.size(); index++) {
            assertSame(view.getCell(index), visited.get(index));
        }

        assertEquals(1, visited.stream().filter(cell -> !cell.isEmpty()).count());
        assertEquals(List.of(TestChecker.WHITE_MAN), visited.get(7).getState());
    }

    /**
     * Verifies that traversal rejects a null action.
     */
    @Test
    void rejectsNullTraversalAction() {
        assertThrows(NullPointerException.class,
                () -> view.forEachCell(null));
    }

    /**
     * Verifies that an exception thrown by the traversal action
     * is propagated as the same instance without wrapping.
     */
    @Test
    void propagatesTraversalActionException() {
        IllegalStateException failure = new IllegalStateException("Action failed");

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> view.forEachCell((index, cell) -> {
                    throw failure;
                }));

        assertSame(failure, actual);
    }

    /**
     * Verifies that an empty board snapshot includes all playing cells
     * and both standard colors in each special cell map.
     */
    @Test
    void stateOfEmptyBoardIncludesAllCells() {
        BoardState<TestChecker> state = view.getState();

        assertEquals(24, state.getCells().size());
        assertTrue(state.getCells().stream().allMatch(List::isEmpty));

        Set<StandardCheckerColor> expectedColors = Set.of(
                StandardCheckerColor.WHITE,
                StandardCheckerColor.BLACK);

        assertEquals(expectedColors, state.getBars().keySet());
        assertEquals(expectedColors, state.getBorneOff().keySet());

        assertTrue(state.getBars().get(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(state.getBars().get(StandardCheckerColor.BLACK).isEmpty());
        assertTrue(state.getBorneOff().get(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(state.getBorneOff().get(StandardCheckerColor.BLACK).isEmpty());
    }

    /**
     * Verifies that a snapshot preserves playing cell indices,
     * bottom-to-top checker order, and all special cell contents.
     * Immutable checker objects are shared with the board.
     */
    @Test
    void statePreservesCellIndicesCheckerOrderAndSpecialCells() {
        board.getCell(0).setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR));
        board.getCell(7).setState(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR));
        board.getCell(23).setState(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.BLACK_MAN));

        board.getBar(StandardCheckerColor.WHITE).setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR));
        board.getBar(StandardCheckerColor.BLACK).setState(List.of(
                TestChecker.BLACK_EMIR,
                TestChecker.BLACK_MAN));
        board.getBorneOff(StandardCheckerColor.WHITE).setState(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN));
        board.getBorneOff(StandardCheckerColor.BLACK).setState(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR));

        BoardState<TestChecker> state = view.getState();

        assertEquals(24, state.getCells().size());

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR), state.getCells().get(0));
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR), state.getCells().get(7));
        assertEquals(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.BLACK_MAN), state.getCells().get(23));

        assertTrue(IntStream.range(0, 24)
                .filter(index -> index != 0 && index != 7 && index != 23)
                .allMatch(index -> state.getCells().get(index).isEmpty()));

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR), state.getBars().get(StandardCheckerColor.WHITE));
        assertEquals(List.of(
                TestChecker.BLACK_EMIR,
                TestChecker.BLACK_MAN), state.getBars().get(StandardCheckerColor.BLACK));
        assertEquals(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN), state.getBorneOff().get(StandardCheckerColor.WHITE));
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), state.getBorneOff().get(StandardCheckerColor.BLACK));

        assertSame(view.getCell(0).peek(), state.getCells().getFirst().getLast());
        assertSame(view.getBar(StandardCheckerColor.WHITE).peek(),
                state.getBars().get(StandardCheckerColor.WHITE).getLast());
        assertSame(view.getBorneOff(StandardCheckerColor.BLACK).peek(),
                state.getBorneOff().get(StandardCheckerColor.BLACK).getLast());
    }

    /**
     * Verifies that snapshot collections cannot be modified,
     * including checker lists nested inside the outer list and maps.
     * Failed modifications leave the board unchanged.
     */
    @Test
    void stateCollectionsAreUnmodifiable() {
        board.getCell(0).push(TestChecker.WHITE_MAN);
        board.getBar(StandardCheckerColor.WHITE).push(TestChecker.WHITE_EMIR);
        board.getBorneOff(StandardCheckerColor.BLACK).push(TestChecker.BLACK_MAN);

        BoardState<TestChecker> state = view.getState();

        assertThrows(UnsupportedOperationException.class,
                () -> state.getCells().set(0, List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> state.getCells().getFirst().add(TestChecker.BLACK_MAN));

        assertThrows(UnsupportedOperationException.class,
                () -> state.getBars().put(StandardCheckerColor.WHITE, List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> state.getBars().get(StandardCheckerColor.WHITE).clear());

        assertThrows(UnsupportedOperationException.class,
                () -> state.getBorneOff().put(StandardCheckerColor.BLACK, List.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> state.getBorneOff().get(StandardCheckerColor.BLACK).clear());

        assertEquals(List.of(TestChecker.WHITE_MAN), view.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                view.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                view.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that a previously obtained snapshot retains its contents
     * after checker transfers and clearing.
     * Later snapshots reflect the board contents at the time of each call.
     */
    @Test
    void stateSnapshotsRemainIndependentOfSubsequentBoardChanges() {
        board.getCell(0).setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR));
        board.getBar(StandardCheckerColor.BLACK).push(TestChecker.BLACK_MAN);
        board.getBorneOff(StandardCheckerColor.WHITE).push(TestChecker.WHITE_EMIR);

        BoardState<TestChecker> before = view.getState();

        board.moveToBar(0);
        board.moveToBorneOff(0);

        BoardState<TestChecker> after = view.getState();

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR), before.getCells().getFirst());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                before.getBars().get(StandardCheckerColor.BLACK));
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                before.getBorneOff().get(StandardCheckerColor.WHITE));

        assertTrue(after.getCells().getFirst().isEmpty());
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), after.getBars().get(StandardCheckerColor.BLACK));
        assertEquals(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN), after.getBorneOff().get(StandardCheckerColor.WHITE));

        board.clear();

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR), before.getCells().getFirst());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                before.getBars().get(StandardCheckerColor.BLACK));
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                before.getBorneOff().get(StandardCheckerColor.WHITE));

        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), after.getBars().get(StandardCheckerColor.BLACK));
        assertEquals(List.of(
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN), after.getBorneOff().get(StandardCheckerColor.WHITE));

        BoardState<TestChecker> cleared = view.getState();

        assertTrue(cleared.getCells().stream().allMatch(List::isEmpty));
        assertTrue(cleared.getBars().values().stream().allMatch(List::isEmpty));
        assertTrue(cleared.getBorneOff().values().stream().allMatch(List::isEmpty));
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
}
