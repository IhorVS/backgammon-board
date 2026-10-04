package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.CheckerColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Colors of checkers in a standard backgammon set.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum StandardCheckerColor implements CheckerColor {

    /**
     * The white side, identified by the code {@code W}.
     */
    WHITE("W"),

    /**
     * The black side, identified by the code {@code B}.
     */
    BLACK("B");

    /**
     * The stable text code identifying this color.
     */
    private final String code;

    /**
     * Returns the color identified by the given code.
     *
     * @param code the case-sensitive color code, such as {@code W} or {@code B}
     * @return the corresponding standard backgammon color
     * @throws NullPointerException     if {@code code} is {@code null}
     * @throws IllegalArgumentException if the code is unknown
     */
    public static StandardCheckerColor fromCode(@NonNull String code) {
        return switch (code) {
            case "W" -> WHITE;
            case "B" -> BLACK;
            default -> throw new IllegalArgumentException(
                    "Unknown checker color code: " + code);
        };
    }

    /**
     * Returns the opposite checker color.
     *
     * @param color the color whose opposite is requested
     * @return {@code BLACK} for {@code WHITE}, or {@code WHITE} for {@code BLACK}
     * @throws NullPointerException if {@code color} is {@code null}
     */
    public static StandardCheckerColor getOpposite(@NonNull StandardCheckerColor color) {
        return switch (color) {
            case WHITE -> BLACK;
            case BLACK -> WHITE;
        };
    }
}
