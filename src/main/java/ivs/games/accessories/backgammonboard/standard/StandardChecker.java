package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.Checker;
import lombok.Getter;
import lombok.NonNull;

/**
 * Checkers in a standard backgammon set.
 *
 * <p>Each constant identifies a type and color, not an individual physical
 * checker. The same constant may occur multiple times on the board.</p>
 *
 * <p>Movement and interaction rules are defined by the game.</p>
 */
@Getter
public enum StandardChecker implements Checker<StandardCheckerType, StandardCheckerColor> {

    /**
     * An ordinary white checker, identified by the code {@code WM}.
     */
    WHITE_MAN(StandardCheckerType.MAN, StandardCheckerColor.WHITE),

    /**
     * A white emir checker, identified by the code {@code WE}.
     */
    WHITE_EMIR(StandardCheckerType.EMIR, StandardCheckerColor.WHITE),

    /**
     * An ordinary black checker, identified by the code {@code BM}.
     */
    BLACK_MAN(StandardCheckerType.MAN, StandardCheckerColor.BLACK),

    /**
     * A black emir checker, identified by the code {@code BE}.
     */
    BLACK_EMIR(StandardCheckerType.EMIR, StandardCheckerColor.BLACK);

    /**
     * The checker type.
     */
    private final StandardCheckerType type;

    /**
     * The checker color.
     */
    private final StandardCheckerColor color;

    /**
     * The stable checker code, consisting of the color code
     * followed by the type code.
     */
    private final String code;

    StandardChecker(StandardCheckerType type, StandardCheckerColor color) {
        this.type = type;
        this.color = color;
        this.code = color.getCode() + type.getCode();
    }

    /**
     * Returns the standard checker identified by its code.
     *
     * @param code the case-sensitive checker code:
     *             {@code WM}, {@code WE}, {@code BM}, or {@code BE}
     * @return the corresponding standard checker
     * @throws NullPointerException     if {@code code} is {@code null}
     * @throws IllegalArgumentException if the code is unknown
     */
    public static StandardChecker fromCode(@NonNull String code) {
        return switch (code) {
            case "WM" -> WHITE_MAN;
            case "WE" -> WHITE_EMIR;
            case "BM" -> BLACK_MAN;
            case "BE" -> BLACK_EMIR;
            default -> throw new IllegalArgumentException("Unknown checker code: " + code);
        };
    }
}
