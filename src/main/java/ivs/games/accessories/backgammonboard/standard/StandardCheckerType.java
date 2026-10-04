package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.CheckerType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * Types of checkers in a standard backgammon set.
 *
 * <p>Movement and interaction rules associated with each type
 * are defined by the game.</p>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StandardCheckerType implements CheckerType {

    /** An ordinary checker. */
    MAN("M"),

    /** An emir checker. */
    EMIR("E");

    private final String code;

    /**
     * Returns the checker type identified by its code.
     *
     * @param code the case-sensitive type code, such as {@code M} or {@code E}
     * @return the corresponding standard checker type
     * @throws NullPointerException     if {@code code} is {@code null}
     * @throws IllegalArgumentException if the code is unknown
     */
    public static StandardCheckerType fromCode(@NonNull String code) {
        return switch (code) {
            case "M" -> MAN;
            case "E" -> EMIR;
            default -> throw new IllegalArgumentException("Unknown checker type code: " + code);
        };
    }
}

