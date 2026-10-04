package ivs.games.accessories.backgammonboard;

/**
 * A backgammon checker with a code, type, and color.
 *
 * <p>Each checker describes a type and color, not an individual physical piece.
 * The same checker instance may occur multiple times on the board.</p>
 *
 * <p>Implementations must be immutable. All properties must remain unchanged
 * after creation, including properties of referenced type and color objects.</p>
 *
 * <p>Movement and interaction rules are defined by the game, not by the checker.</p>
 *
 * @param <T> the type used to classify checkers
 * @param <C> the color used to distinguish sides
 */
public interface Checker<T extends CheckerType, C extends CheckerColor> {

    /**
     * Returns a stable text code identifying this checker's type and color.
     *
     * <p>The code does not identify an individual physical piece and need not
     * be unique across different checker sets.</p>
     *
     * @return the non-null checker code
     */
    String getCode();

    /**
     * Returns the type of this checker.
     *
     * @return the non-null checker type
     */
    T getType();

    /**
     * Returns the color of this checker.
     *
     * @return the non-null checker color
     */
    C getColor();
}
