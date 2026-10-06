package ivs.games.accessories.backgammonboard;

import java.util.function.BiConsumer;

/**
 * A read-only view of a backgammon board.
 *
 * <p>The board contains 24 playing cells indexed from 0 to 23,
 * a bar cell and a borne-off cell for each supported color.
 * Playing cell indices are fixed and do not depend on player orientation.</p>
 *
 * <p>This interface provides no operations for changing the board or its cells.
 * The underlying board may change through another reference.</p>
 *
 * <p>Movement and interaction rules are defined by the game.</p>
 *
 * @param <P> the type of checkers stored on this board
 */
public interface BackgammonBoardView<P extends Checker<?, ?>> {

    /**
     * Returns a read-only view of the playing cell at the specified index.
     *
     * @param index the playing cell index, from 0 to 23
     * @return the cell view
     * @throws IllegalArgumentException if the index is outside the board
     */
    BoardCellView<P> getCell(int index);

    /**
     * Returns a read-only view of the bar cell for the specified color.
     *
     * @param color the color identifying the bar cell
     * @return the bar cell view
     * @throws NullPointerException     if {@code color} is {@code null}
     * @throws IllegalArgumentException if the color is not supported by this board
     */
    BoardCellView<P> getBar(CheckerColor color);

    /**
     * Returns a read-only view of the borne-off cell for the specified color.
     *
     * @param color the color identifying the borne-off cell
     * @return the borne-off cell view
     * @throws NullPointerException     if {@code color} is {@code null}
     * @throws IllegalArgumentException if the color is not supported by this board
     */
    BoardCellView<P> getBorneOff(CheckerColor color);

    /**
     * Returns whether all playing, bar, and borne-off cells are empty.
     *
     * @return {@code true} if the board contains no checkers
     */
    boolean isEmpty();

    /**
     * Returns the number of playing cells.
     *
     * @return the playing cell count
     */
    int getPlayingCellCount();

    /**
     * Performs an action for each playing cell in ascending index order.
     *
     * <p>Empty cells are included. Bar and borne-off cells are excluded.
     * The action must not modify the board during traversal.</p>
     *
     * @param action the action to perform with the index and cell view
     * @throws NullPointerException if {@code action} is {@code null}
     */
    void forEachCell(BiConsumer<Integer, BoardCellView<P>> action);

    /**
     * Returns an immutable snapshot of all playing, bar, and borne-off cells.
     *
     * <p>Checker order is preserved. Subsequent changes to the board
     * do not affect the returned state. Immutable checker objects are shared.</p>
     *
     * @return the board state
     */
    BoardState<P> getState();
}
