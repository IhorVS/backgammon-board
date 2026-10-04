package ivs.games.accessories.backgammonboard;

import java.util.List;

/**
 * A mutable cell containing a stack of checkers.
 *
 * <p>The cell does not enforce game rules. Implementations may impose
 * structural restrictions, such as accepting only a specified color.</p>
 *
 * @param <P> the type of checkers stored in this cell
 */
public interface BoardCell<P extends Checker<?, ?>>
        extends BoardCellView<P> {

    /**
     * Adds a checker to the top of this cell.
     *
     * @param checker the checker to add
     * @throws NullPointerException     if {@code checker} is {@code null}
     * @throws IllegalArgumentException if the checker violates a restriction
     *                                  imposed by this cell
     */
    void push(P checker);

    /**
     * Removes and returns the top checker.
     *
     * @return the removed checker
     * @throws IllegalArgumentException if the cell is empty
     */
    P pop();

    /**
     * Replaces the contents of this cell.
     *
     * <p>Checkers are ordered from bottom to top; the last element
     * becomes the top checker. An empty list clears the cell.</p>
     *
     * <p>The list contents are copied. Subsequent changes to the supplied list
     * do not affect the cell. If validation fails, the cell remains unchanged.</p>
     *
     * @param checkers the new cell contents
     * @throws NullPointerException     if {@code checkers} is {@code null}
     * @throws IllegalArgumentException if any element is {@code null} or violates
     *                                  a restriction imposed by this cell
     */
    void setState(List<P> checkers);

    /**
     * Removes all checkers from this cell.
     */
    void clear();
}
