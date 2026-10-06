package ivs.games.accessories.backgammonboard;

/**
 * A mutable backgammon board.
 *
 * <p>Playing cells accept checkers of any type and color.
 * Bar and borne-off cells accept only checkers of their assigned color.</p>
 *
 * <p>The board enforces structural restrictions, not game rules.
 * The game determines whether a checker may be moved between cells.</p>
 *
 * @param <P> the type of checkers stored on this board
 */
public interface BackgammonBoard<P extends Checker<?, ?>>
        extends BackgammonBoardView<P> {

    /**
     * {@inheritDoc}
     *
     * @return the mutable playing cell
     */
    @Override
    BoardCell<P> getCell(int index);

    /**
     * {@inheritDoc}
     *
     * @return the mutable bar cell
     */
    @Override
    BoardCell<P> getBar(CheckerColor color);

    /**
     * {@inheritDoc}
     *
     * @return the mutable borne-off cell
     */
    @Override
    BoardCell<P> getBorneOff(CheckerColor color);

    /**
     * Removes all checkers from all playing, bar, and borne-off cells.
     *
     * <p>Existing references to cells remain valid and refer to the cleared cells.</p>
     */
    void clear();

    /**
     * Moves the top checker between playing cells.
     *
     * <p>Game rules are not checked. If validation fails,
     * both cells remain unchanged.</p>
     *
     * @param sourceIndex      the source playing cell index, from 0 to 23
     * @param destinationIndex the destination playing cell index, from 0 to 23
     * @throws IllegalArgumentException if either index is outside the board,
     *                                  the indices are equal, or the source is empty
     */
    void move(int sourceIndex, int destinationIndex);

    /**
     * Moves the top checker from a playing cell to the bar of its color.
     *
     * <p>Game rules are not checked. If validation fails,
     * both cells remain unchanged.</p>
     *
     * @param sourceIndex the source playing cell index, from 0 to 23
     * @throws IllegalArgumentException if the index is outside the board,
     *                                  the source is empty, or the checker color
     *                                  is not supported by this board
     */
    void moveToBar(int sourceIndex);

    /**
     * Moves the top checker from the specified color's bar to a playing cell.
     *
     * <p>Game rules are not checked. If validation fails,
     * both cells remain unchanged.</p>
     *
     * @param color            the color identifying the source bar
     * @param destinationIndex the destination playing cell index, from 0 to 23
     * @throws NullPointerException     if {@code color} is {@code null}
     * @throws IllegalArgumentException if the color is not supported by this board,
     *                                  the index is outside the board,
     *                                  or the source bar is empty
     */
    void moveFromBar(CheckerColor color, int destinationIndex);

    /**
     * Moves the top checker from a playing cell to the borne-off cell of its color.
     *
     * <p>Game rules are not checked. If validation fails,
     * both cells remain unchanged.</p>
     *
     * @param sourceIndex the source playing cell index, from 0 to 23
     * @throws IllegalArgumentException if the index is outside the board,
     *                                  the source is empty, or the checker color
     *                                  is not supported by this board
     */
    void moveToBorneOff(int sourceIndex);

}

