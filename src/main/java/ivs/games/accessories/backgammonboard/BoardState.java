package ivs.games.accessories.backgammonboard;

import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An immutable snapshot of a backgammon board.
 *
 * <p>The playing cell list is indexed by cell position and includes empty cells.
 * Each checker list is ordered from bottom to top.</p>
 *
 * <p>All supplied collections are copied, including nested checker lists.
 * Checker and color objects are shared and must be immutable.</p>
 *
 * <p>This class does not enforce board-specific restrictions, such as
 * the playing cell count, supported colors, or special cell color matching.
 * These restrictions are validated by the board when applying the state.</p>
 *
 * <p>Subclasses may add state properties but must preserve immutability,
 * including the immutability of any referenced objects.</p>
 *
 * @param <P> the type of checkers contained in this state
 */
public class BoardState<P extends Checker<?, ?>> {

    private final List<List<P>> cells;
    private final Map<CheckerColor, List<P>> bars;
    private final Map<CheckerColor, List<P>> borneOff;

    /**
     * Creates an immutable snapshot of the supplied contents.
     *
     * <p>Subsequent changes to the supplied collections do not affect
     * this state.</p>
     *
     * @param cells    the playing cell contents in cell index order
     * @param bars     the bar contents indexed by color
     * @param borneOff the borne-off contents indexed by color
     * @throws NullPointerException if any supplied collection, map key,
     *                              nested list, or checker is {@code null}
     */
    public BoardState(
            @NonNull List<List<P>> cells,
            @NonNull Map<CheckerColor, List<P>> bars,
            @NonNull Map<CheckerColor, List<P>> borneOff) {

        this.cells = cells.stream().map(List::copyOf).toList();

        this.bars = copyColoredCells(bars);
        this.borneOff = copyColoredCells(borneOff);
    }

    /**
     * Returns the playing cell contents in cell index order.
     *
     * <p>Both the outer list and the nested checker lists are unmodifiable.
     * Checkers in each cell are ordered from bottom to top.</p>
     *
     * @return the immutable playing cell contents
     */
    public final List<List<P>> getCells() {
        return cells;
    }

    /**
     * Returns the bar contents indexed by color.
     *
     * <p>The map and its checker lists are unmodifiable.
     * Checkers in each bar are ordered from bottom to top.</p>
     *
     * @return the immutable bar contents
     */
    public final Map<CheckerColor, List<P>> getBars() {
        return bars;
    }

    /**
     * Returns the borne-off contents indexed by color.
     *
     * <p>The map and its checker lists are unmodifiable.
     * Checkers in each cell are ordered from bottom to top.</p>
     *
     * @return the immutable borne-off contents
     */
    public final Map<CheckerColor, List<P>> getBorneOff() {
        return borneOff;
    }

    /**
     * Copies special cell contents, preserving checker order.
     *
     * @param source the contents indexed by color
     * @return an unmodifiable map containing unmodifiable checker lists
     * @throws NullPointerException if a key, checker list,
     *                              or checker is {@code null}
     */
    private Map<CheckerColor, List<P>> copyColoredCells(Map<CheckerColor, List<P>> source) {

        return source.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> List.copyOf(entry.getValue()))
        );
    }
}
