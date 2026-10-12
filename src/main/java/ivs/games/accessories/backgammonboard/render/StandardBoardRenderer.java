package ivs.games.accessories.backgammonboard.render;

import ivs.games.accessories.backgammonboard.BackgammonBoardView;
import ivs.games.accessories.backgammonboard.BoardState;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.standard.StandardBoard;
import ivs.games.accessories.backgammonboard.standard.StandardCheckerColor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Renders standard backgammon boards as plain text.
 *
 * <p>Playing cells use their technical board indices.
 * The upper row displays indices 23 through 12 from left to right.
 * The lower row displays indices 0 through 11 from left to right.
 * Index 0 is at the lower-left corner. Indices increase along the lower
 * edge to the right, then along the upper edge to the left.
 * This layout does not determine checker movement directions.</p>
 *
 * <p>Checkers are displayed individually, with the bottom of each stack
 * at the outer board edge and the top towards the center.
 * Each half has at least five checker rows and expands independently
 * to accommodate its tallest stack. One blank row separates the halves.</p>
 *
 * <p>Bar and borne-off cells are displayed below the board.
 * Their checker lists are ordered from bottom to top and include counts.
 * White and black sections are aligned across both lines.</p>
 *
 * <p>Checker codes are displayed as supplied by {@link Checker#getCode()}.
 * Codes must be non-empty and contain no whitespace.
 * Alignment assumes that each code character occupies one console column.</p>
 *
 * <p>This renderer has no mutable state. The source board must not
 * be modified while its state is being captured.</p>
 */
@NoArgsConstructor
public class StandardBoardRenderer {

    private static final int HALF_CELL_COUNT = StandardBoard.PLAYING_CELL_COUNT / 2;
    private static final int GROUP_CELL_COUNT = HALF_CELL_COUNT / 2;
    private static final int MIN_HALF_HEIGHT = 5;

    private static final String EMPTY_SLOT = "..";
    private static final String INDENT = "     ";
    private static final String CELL_SEPARATOR = "  ";
    private static final String LINE_SEPARATOR = "\n";

    /**
     * Renders the supplied board using a snapshot of its state.
     *
     * <p>The returned text uses {@code '\n'} as its line separator
     * and has no trailing line separator.</p>
     *
     * @param board the board to render
     * @return the rendered board
     * @throws NullPointerException     if {@code board} is {@code null}
     * @throws IllegalArgumentException if the playing cell count is not 24
     *                                  or a checker code is empty or contains whitespace
     */
    @SuppressWarnings({"StringRepeatCanBeUsed"})
    public String render(@NonNull BackgammonBoardView<?> board) {
        Validate.isTrue(board.getPlayingCellCount() == StandardBoard.PLAYING_CELL_COUNT,
                "Board has %d playing cells; expected %d",
                board.getPlayingCellCount(), StandardBoard.PLAYING_CELL_COUNT);

        BoardState<?> state = board.getState();

        Validate.isTrue(state.getCells().size() == StandardBoard.PLAYING_CELL_COUNT,
                "State has %d playing cells; expected %d",
                state.getCells().size(), StandardBoard.PLAYING_CELL_COUNT);

        List<List<String>> cells = state.getCells().stream()
                .map(StandardBoardRenderer::getCodes)
                .toList();

        List<String> whiteBar = getCodes(state.getBars().get(StandardCheckerColor.WHITE));
        List<String> blackBar = getCodes(state.getBars().get(StandardCheckerColor.BLACK));
        List<String> whiteBorneOff = getCodes(state.getBorneOff().get(StandardCheckerColor.WHITE));
        List<String> blackBorneOff = getCodes(state.getBorneOff().get(StandardCheckerColor.BLACK));

        int cellWidth = Math.max(EMPTY_SLOT.length(), cells.stream()
                .flatMap(List::stream)
                .mapToInt(String::length)
                .max()
                .orElse(0));

        int groupWidth = GROUP_CELL_COUNT * cellWidth
                + (GROUP_CELL_COUNT - 1) * CELL_SEPARATOR.length();

        String border = INDENT + "+"
                + "-".repeat(groupWidth + 2) + "+"
                + "-".repeat(groupWidth + 2) + "+";

        int topHeight = Math.max(MIN_HALF_HEIGHT, cells.subList(HALF_CELL_COUNT, cells.size()).stream()
                .mapToInt(List::size)
                .max()
                .orElse(0));

        int bottomHeight = Math.max(MIN_HALF_HEIGHT, cells.subList(0, HALF_CELL_COUNT).stream()
                .mapToInt(List::size)
                .max()
                .orElse(0));

        StringBuilder result = new StringBuilder();

        appendIndices(result, true, cellWidth);
        result.append(border).append(LINE_SEPARATOR);

        appendHalf(result, cells, true, topHeight, cellWidth);

        result.append(INDENT).append("| ")
                .append(" ".repeat(groupWidth)).append(" | ")
                .append(" ".repeat(groupWidth)).append(" |")
                .append(LINE_SEPARATOR);

        appendHalf(result, cells, false, bottomHeight, cellWidth);

        result.append(border).append(LINE_SEPARATOR);
        appendIndices(result, false, cellWidth);
        result.append(LINE_SEPARATOR);

        appendSpecialCells(result, whiteBar, blackBar, whiteBorneOff, blackBorneOff);

        return result.toString();
    }

    /*
     * Captures and validates checker codes for one cell.
     * The list preserves bottom-to-top checker order.
     */
    private static List<String> getCodes(List<? extends Checker<?, ?>> checkers) {
        return checkers.stream()
                .map(checker -> {
                    String code = checker.getCode();

                    Validate.notEmpty(code, "Checker code must not be empty");
                    Validate.isTrue(code.chars().noneMatch(Character::isWhitespace),
                            "Checker code must not contain whitespace: %s", code);

                    return code;
                })
                .toList();
    }

    /*
     * Returns the board index for a displayed column.
     * Upper indices decrease from left to right; lower indices increase.
     */
    private static int getCellIndex(boolean upper, int column) {
        return upper ? StandardBoard.PLAYING_CELL_COUNT - 1 - column : column;
    }

    /*
     * Writes technical cell indices using the same column widths
     * and group spacing as the checker rows.
     */
    private void appendIndices(StringBuilder result, boolean upper, int cellWidth) {
        String format = "%" + cellWidth + "d";

        result.append(INDENT).append("  ");

        for (int column = 0; column < HALF_CELL_COUNT; column++) {
            if (column > 0) {
                result.append(column == GROUP_CELL_COUNT ? "   " : CELL_SEPARATOR);
            }

            int index = getCellIndex(upper, column);
            result.append(String.format(Locale.ROOT, format, index));
        }

        result.append(LINE_SEPARATOR);
    }

    /*
     * Writes one half of the board.
     * Upper stacks grow downwards; lower stacks grow upwards.
     * Unoccupied positions are filled with the empty-slot marker.
     */
    @SuppressWarnings("StringRepeatCanBeUsed")
    private void appendHalf(StringBuilder result, List<List<String>> cells, boolean upper, int height, int cellWidth) {
        for (int row = 0; row < height; row++) {
            int checkerIndex = upper ? row : height - 1 - row;

            result.append(INDENT).append("| ");

            for (int column = 0; column < HALF_CELL_COUNT; column++) {
                if (column > 0) {
                    result.append(column == GROUP_CELL_COUNT ? " | " : CELL_SEPARATOR);
                }

                int cellIndex = getCellIndex(upper, column);
                List<String> checkers = cells.get(cellIndex);
                String code = checkerIndex < checkers.size() ? checkers.get(checkerIndex) : EMPTY_SLOT;

                result.append(" ".repeat(cellWidth - code.length())).append(code);
            }

            result.append(" |").append(LINE_SEPARATOR);
        }
    }

    /*
     * Formats special cell counts and complete checker lists.
     * Counts share a width of at least two characters.
     * Black sections begin at the same position on both lines.
     */
    @SuppressWarnings("StringRepeatCanBeUsed")
    private void appendSpecialCells(
            StringBuilder result,
            List<String> whiteBar,
            List<String> blackBar,
            List<String> whiteBorneOff,
            List<String> blackBorneOff) {

        int largestCount = IntStream.of(
                        whiteBar.size(), blackBar.size(),
                        whiteBorneOff.size(), blackBorneOff.size())
                .max()
                .orElse(0);

        int countWidth = Math.max(2, Integer.toString(largestCount).length());
        String format = "%s (%" + countWidth + "d): %s";

        String whiteBarText = String.format(Locale.ROOT, format,
                "W", whiteBar.size(), whiteBar.stream().collect(Collectors.joining(", ", "[", "]")));

        String blackBarText = String.format(Locale.ROOT, format,
                "B", blackBar.size(), blackBar.stream().collect(Collectors.joining(", ", "[", "]")));

        String whiteBorneOffText = String.format(Locale.ROOT, format,
                "W", whiteBorneOff.size(), whiteBorneOff.stream().collect(Collectors.joining(", ", "[", "]")));

        String blackBorneOffText = String.format(Locale.ROOT, format,
                "B", blackBorneOff.size(), blackBorneOff.stream().collect(Collectors.joining(", ", "[", "]")));

        int whiteWidth = Math.max(whiteBarText.length(), whiteBorneOffText.length());

        result.append(INDENT).append("BAR       ")
                .append(whiteBarText)
                .append(" ".repeat(whiteWidth - whiteBarText.length() + 5))
                .append(blackBarText)
                .append(LINE_SEPARATOR);

        result.append(INDENT).append("BORNE OFF ")
                .append(whiteBorneOffText)
                .append(" ".repeat(whiteWidth - whiteBorneOffText.length() + 5))
                .append(blackBorneOffText);
    }
}
