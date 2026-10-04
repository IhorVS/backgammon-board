package ivs.games.accessories.backgammonboard;

/**
 * A checker color used to distinguish sides.
 *
 * <p>Implementations must be immutable. All properties must remain
 * unchanged after creation.</p>
 */
public interface CheckerColor {

    /**
     * Returns a stable text code identifying this color.
     *
     * <p>The code need not be unique across different checker sets.</p>
     *
     * @return the non-null color code
     */
    String getCode();
}
