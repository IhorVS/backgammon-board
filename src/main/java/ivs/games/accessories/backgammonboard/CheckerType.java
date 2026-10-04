package ivs.games.accessories.backgammonboard;

/**
 * A type used to classify checkers.
 *
 * <p>Implementations must be immutable. All properties must remain
 * unchanged after creation.</p>
 *
 * <p>Behavior associated with a checker type is defined by the game.</p>
 */
public interface CheckerType {

    /**
     * Returns a stable text code identifying this type.
     *
     * <p>The code need not be unique across different checker sets.</p>
     *
     * @return the non-null type code
     */
    String getCode();
}
