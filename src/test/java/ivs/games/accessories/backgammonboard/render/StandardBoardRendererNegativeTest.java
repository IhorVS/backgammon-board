package ivs.games.accessories.backgammonboard.render;

import ivs.games.accessories.backgammonboard.BoardState;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.standard.StandardBoard;
import ivs.games.accessories.backgammonboard.standard.StandardChecker;
import ivs.games.accessories.backgammonboard.standard.StandardCheckerColor;
import ivs.games.accessories.backgammonboard.standard.StandardCheckerType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests renderer rejection of null boards, incompatible board sizes,
 * and invalid checker codes in playing, bar, and borne-off cells.
 */
@SuppressWarnings("DataFlowIssue")
class StandardBoardRendererNegativeTest {

    private final StandardBoardRenderer renderer = new StandardBoardRenderer();

    /**
     * Verifies that a null board is rejected.
     */
    @Test
    void rejectsNullBoard() {
        assertThrows(NullPointerException.class, () -> renderer.render(null));
    }

    /**
     * Verifies that an incompatible playing cell count is rejected
     * before the renderer requests the board state.
     *
     * @param count the unsupported playing cell count
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 12, 23, 25})
    void rejectsInvalidBoardCellCountBeforeReadingState(int count) {
        StandardBoard<StandardChecker> board = new StandardBoard<>() {
            @Override
            public int getPlayingCellCount() {
                return count;
            }

            @Override
            public BoardState<StandardChecker> getState() {
                return fail("Invalid board must be rejected before reading its state");
            }
        };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(board));

        assertEquals("Board has %d playing cells; expected 24".formatted(count),
                exception.getMessage());
    }

    /**
     * Verifies that a state with an incompatible playing cell count
     * is rejected even if the board reports the standard count.
     *
     * @param count the unsupported state playing cell count
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 12, 23, 25})
    void rejectsInvalidStateCellCount(int count) {
        StandardBoard<StandardChecker> board = new StandardBoard<>() {
            @Override
            public BoardState<StandardChecker> getState() {
                BoardState<StandardChecker> state = super.getState();

                List<List<StandardChecker>> cells =
                        java.util.Collections.nCopies(count, List.of());

                return new BoardState<>(cells, state.getBars(), state.getBorneOff());
            }
        };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(board));

        assertEquals("State has %d playing cells; expected 24".formatted(count),
                exception.getMessage());
    }

    /**
     * Verifies that null and empty playing checker codes are rejected.
     * A null code causes a null-pointer exception; an empty code
     * causes an illegal-argument exception.
     *
     * @param code the invalid checker code
     */
    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullOrEmptyPlayingCheckerCode(String code) {
        StandardBoard<CodedChecker> board = new StandardBoard<>();
        board.getCell(0).push(new CodedChecker(code));

        if (code == null) {
            assertThrows(NullPointerException.class, () -> renderer.render(board));
        } else {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> renderer.render(board));

            assertEquals("Checker code must not be empty", exception.getMessage());
        }
    }

    /**
     * Verifies that whitespace anywhere in a checker code is rejected,
     * including spaces, tabs, and line separators.
     *
     * @param code the checker code containing whitespace
     */
    @ParameterizedTest
    @ValueSource(strings = {" ", " WM", "WM ", "W M", "W\tM", "W\nM", "W\rM", "W\u2003M"})
    void rejectsPlayingCheckerCodeContainingWhitespace(String code) {
        StandardBoard<CodedChecker> board = new StandardBoard<>();
        board.getCell(23).push(new CodedChecker(code));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(board));

        assertEquals("Checker code must not contain whitespace: " + code,
                exception.getMessage());
    }

    /**
     * Verifies that a checker with an empty code is rejected
     * when stored in a bar or a borne-off cell.
     *
     * @param color the standard color of the special cells
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void rejectsEmptySpecialCellCheckerCodes(StandardCheckerColor color) {
        StandardBoard<CodedChecker> barBoard = new StandardBoard<>();
        StandardBoard<CodedChecker> borneOffBoard = new StandardBoard<>();

        CodedChecker checker = new CodedChecker("", color);

        barBoard.getBar(color).push(checker);
        borneOffBoard.getBorneOff(color).push(checker);

        IllegalArgumentException barException = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(barBoard));
        IllegalArgumentException borneOffException = assertThrows(
                IllegalArgumentException.class,
                () -> renderer.render(borneOffBoard));

        assertEquals("Checker code must not be empty", barException.getMessage());
        assertEquals("Checker code must not be empty", borneOffException.getMessage());
    }

    /**
     * Verifies that a checker with a null code is rejected
     * when stored in a bar or a borne-off cell.
     *
     * @param color the standard color of the special cells
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void rejectsNullSpecialCellCheckerCodes(StandardCheckerColor color) {
        StandardBoard<CodedChecker> barBoard = new StandardBoard<>();
        StandardBoard<CodedChecker> borneOffBoard = new StandardBoard<>();

        CodedChecker checker = new CodedChecker(null, color);

        barBoard.getBar(color).push(checker);
        borneOffBoard.getBorneOff(color).push(checker);

        assertThrows(NullPointerException.class, () -> renderer.render(barBoard));
        assertThrows(NullPointerException.class, () -> renderer.render(borneOffBoard));
    }

    /**
     * Verifies that whitespace-containing checker codes are rejected
     * in bars and borne-off cells for both standard colors.
     *
     * @param color the standard color of the special cells
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void rejectsWhitespaceInSpecialCellCheckerCodes(StandardCheckerColor color) {
        StandardBoard<CodedChecker> barBoard = new StandardBoard<>();
        StandardBoard<CodedChecker> borneOffBoard = new StandardBoard<>();

        CodedChecker checker = new CodedChecker("M\n", color);

        barBoard.getBar(color).push(checker);
        borneOffBoard.getBorneOff(color).push(checker);

        assertThrows(IllegalArgumentException.class, () -> renderer.render(barBoard));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(borneOffBoard));
    }

    /**
     * Verifies that rendering failure does not alter board contents,
     * including valid checkers below and above the invalid checker.
     */
    @Test
    void invalidCheckerCodeDoesNotChangeBoard() {
        StandardBoard<CodedChecker> board = new StandardBoard<>();

        CodedChecker bottom = new CodedChecker("WM");
        CodedChecker invalid = new CodedChecker("");
        CodedChecker top = new CodedChecker("WE");

        board.getCell(0).setState(List.of(bottom, invalid, top));
        board.getBar(StandardCheckerColor.WHITE).push(bottom);
        board.getBorneOff(StandardCheckerColor.WHITE).push(top);

        BoardState<CodedChecker> before = board.getState();

        assertThrows(IllegalArgumentException.class, () -> renderer.render(board));

        BoardState<CodedChecker> after = board.getState();

        assertEquals(before.getCells(), after.getCells());
        assertEquals(before.getBars(), after.getBars());
        assertEquals(before.getBorneOff(), after.getBorneOff());
        assertSame(top, board.getCell(0).peek());
    }

    /*
     * Supplies immutable test checkers with deliberately invalid codes.
     * Type and color remain valid so that board cells accept the checkers.
     */
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static final class CodedChecker
            implements Checker<StandardCheckerType, StandardCheckerColor> {

        private final String code;
        private final StandardCheckerColor color;

        private CodedChecker(String code) {
            this(code, StandardCheckerColor.WHITE);
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public StandardCheckerType getType() {
            return StandardCheckerType.MAN;
        }

        @Override
        public StandardCheckerColor getColor() {
            return color;
        }
    }
}
