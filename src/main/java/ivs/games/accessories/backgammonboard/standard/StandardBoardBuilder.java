package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds standard backgammon boards with specified checker arrangements.
 *
 * <p>The builder stores checker lists for configured playing, bar,
 * and borne-off cells. Unconfigured cells remain empty.
 * Checkers are ordered from bottom to top.</p>
 *
 * <p>Configuration methods replace the previously supplied contents
 * of the selected cell. Supplied lists are copied, and immutable
 * checker objects are shared.</p>
 *
 * <p>Each call to {@link #build()} creates a new independent board
 * without changing the builder. Playing cell indices and special
 * cell color restrictions are validated during board creation.
 * Game rules are not enforced.</p>
 *
 * <p>This implementation is not thread-safe.</p>
 *
 * @param <P> the type of checkers stored on the board
 */
@NoArgsConstructor
public class StandardBoardBuilder<P extends Checker<?, ?>> {

    private final Map<Integer, List<P>> cells = new HashMap<>();
    private final Map<CheckerColor, List<P>> bars = new HashMap<>();
    private final Map<CheckerColor, List<P>> borneOff = new HashMap<>();

    /**
     * Configures the contents of the specified playing cell.
     *
     * <p>The supplied list is copied and replaces any previous
     * configuration for this index. Its last element becomes
     * the top checker. An empty list configures an empty cell.</p>
     *
     * <p>The index is validated when {@link #build()} is called.</p>
     *
     * @param index    the playing cell index, from {@code 0} inclusive
     *                 to {@link StandardBoard#PLAYING_CELL_COUNT} exclusive
     * @param checkers the cell contents, ordered from bottom to top
     * @return this builder
     * @throws NullPointerException if {@code checkers}
     *                              or any element is {@code null}
     */
    public StandardBoardBuilder<P> cell(
            int index,
            @NonNull List<? extends P> checkers) {

        cells.put(index, List.copyOf(checkers));
        return this;
    }

    /**
     * Configures the bar contents for the specified color.
     *
     * <p>The supplied list is copied and replaces any previous
     * configuration for this color. Its last element becomes
     * the top checker. An empty list configures an empty cell.</p>
     *
     * <p>The color must be a standard color, and all checkers must
     * match it. These restrictions are validated when
     * {@link #build()} is called.</p>
     *
     * @param color    the standard color identifying the bar cell
     * @param checkers the cell contents, ordered from bottom to top
     * @return this builder
     * @throws NullPointerException if {@code color}, {@code checkers},
     *                              or any element is {@code null}
     */
    public StandardBoardBuilder<P> bar(
            @NonNull CheckerColor color,
            @NonNull List<? extends P> checkers) {

        bars.put(color, List.copyOf(checkers));
        return this;
    }

    /**
     * Configures the borne-off contents for the specified color.
     *
     * <p>The supplied list is copied and replaces any previous
     * configuration for this color. Its last element becomes
     * the top checker. An empty list configures an empty cell.</p>
     *
     * <p>The color must be a standard color, and all checkers must
     * match it. These restrictions are validated when
     * {@link #build()} is called.</p>
     *
     * @param color    the standard color identifying the borne-off cell
     * @param checkers the cell contents, ordered from bottom to top
     * @return this builder
     * @throws NullPointerException if {@code color}, {@code checkers},
     *                              or any element is {@code null}
     */
    public StandardBoardBuilder<P> borneOff(
            @NonNull CheckerColor color,
            @NonNull List<? extends P> checkers) {

        borneOff.put(color, List.copyOf(checkers));
        return this;
    }

    /**
     * Creates a new board with the configured checker arrangement.
     *
     * <p>Unconfigured cells remain empty. Checker order is preserved.
     * The board owns its cell contents, while immutable checker
     * objects are shared.</p>
     *
     * <p>This operation does not change the builder, including
     * when validation fails. Subsequent changes to the builder
     * or a returned board do not affect one another.</p>
     *
     * @return a new mutable standard board
     * @throws IllegalArgumentException if a configured playing cell
     *                                  index is outside the board,
     *                                  a special cell color is unsupported,
     *                                  or a special cell checker has
     *                                  a different color
     */
    public StandardBoard<P> build() {
        StandardBoard<P> board = new StandardBoard<>();

        cells.forEach((index, checkers) ->
                board.getCell(index).setState(checkers));

        bars.forEach((color, checkers) ->
                board.getBar(color).setState(checkers));

        borneOff.forEach((color, checkers) ->
                board.getBorneOff(color).setState(checkers));

        return board;
    }
}
