package ivs.games.accessories.backgammonboard;

import java.util.List;
import java.util.function.Consumer;

/**
 * A read-only view of a cell containing a stack of checkers.
 *
 * <p>Checkers are ordered from bottom to top. The top checker corresponds
 * to the last element in the cell state.</p>
 *
 * <p>This interface provides no operations for changing the cell.
 * The underlying cell may change through another reference.</p>
 *
 * @param <P> the type of checkers stored in this cell
 */
public interface BoardCellView<P extends Checker<?, ?>> {

    /**
     * Returns the number of checkers in this cell.
     *
     * @return the checker count
     */
    int size();

    /**
     * Returns whether this cell contains no checkers.
     *
     * @return {@code true} if the cell is empty
     */
    boolean isEmpty();

    /**
     * Returns the top checker without removing it.
     *
     * @return the top checker
     * @throws IllegalArgumentException if the cell is empty
     */
    P peek();

    /**
     * Returns a snapshot of the checkers ordered from bottom to top.
     *
     * <p>The returned list is unmodifiable and does not change when
     * the cell changes. Checker objects are shared and must be immutable
     * according to the {@link Checker} contract.</p>
     *
     * @return an unmodifiable list of checkers
     */
    List<P> getState();

    /**
     * Returns the number of checkers of the specified color in this cell.
     *
     * @param color the color to count
     * @return the checker count, or {@code 0} if none have this color
     * @throws NullPointerException if {@code color} is {@code null}
     */
    int count(CheckerColor color);

    /**
     * Performs an action for each checker, from bottom to top.
     *
     * <p>The action must not modify the cell during traversal.</p>
     *
     * @param action the action to perform with each checker
     * @throws NullPointerException if {@code action} is {@code null}
     */
    void forEach(Consumer<? super P> action);

    /**
     * Returns the color of the top checker.
     *
     * @return the top checker color
     * @throws IllegalArgumentException if the cell is empty
     */
    CheckerColor getTopCheckerColor();
}
