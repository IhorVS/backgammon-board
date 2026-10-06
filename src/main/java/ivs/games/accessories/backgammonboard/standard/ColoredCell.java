package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BoardCellView;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * A cell that accepts checkers of a specified color only.
 *
 * <p>This cell can be used as a bar or a borne-off area.
 * Checkers of any type are permitted if their color matches
 * the configured cell color.</p>
 *
 * <p>This implementation is not thread-safe.</p>
 *
 * @param <P> the type of checkers stored in this cell
 */
@RequiredArgsConstructor
public class ColoredCell<P extends Checker<?, ?>> extends StandardCell<P> {

    /**
     * The color accepted by this cell.
     */
    @Getter
    @NonNull
    private final CheckerColor color;

    /**
     * Creates an independent copy of the supplied colored cell.
     *
     * <p>The configured color and checker order are preserved.
     * Immutable checker and color objects are shared.</p>
     *
     * @param source the cell to copy
     * @throws NullPointerException     if {@code source} is {@code null}
     * @throws IllegalArgumentException if a source checker has a different color
     */
    public ColoredCell(@NonNull ColoredCell<P> source) {
        this(source.getColor(), source.getState());
    }

    /**
     * Creates a cell of the specified color and copies the source contents.
     *
     * <p>Checker order is preserved. Subsequent changes to either cell
     * do not affect the other. Immutable checker objects are shared.</p>
     *
     * @param color  the color accepted by this cell
     * @param source the cell to copy
     * @throws NullPointerException     if either argument is {@code null}
     * @throws IllegalArgumentException if a source checker has a different color
     */
    public ColoredCell(
            @NonNull CheckerColor color,
            @NonNull BoardCellView<P> source) {

        this(color, source.getState());
    }

    /**
     * Creates a cell of the specified color from the supplied checker list.
     *
     * <p>Checkers are ordered from bottom to top; the last element becomes
     * the top checker. An empty list creates an empty cell.</p>
     *
     * <p>The list contents are copied. Subsequent changes to the supplied list
     * do not affect the cell. Immutable checker objects are shared.</p>
     *
     * @param color    the color accepted by this cell
     * @param checkers the initial cell contents
     * @throws NullPointerException     if {@code color}, {@code checkers},
     *                                  or any checker is {@code null}
     * @throws IllegalArgumentException if any checker has a different color
     */
    public ColoredCell(
            @NonNull CheckerColor color,
            @NonNull List<P> checkers) {

        this.color = color;
        setState(checkers);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The checker must match the configured cell color.
     * If validation fails, the cell remains unchanged.</p>
     *
     * @throws IllegalArgumentException if the checker has a different color
     */
    @Override
    public void push(@NonNull P checker) {
        Validate.isTrue(color.equals(checker.getColor()),
                "Checker color %s does not match cell color %s",
                checker.getColor().getCode(), color.getCode());

        super.push(checker);
    }

    /**
     * {@inheritDoc}
     *
     * <p>All checkers must match the configured cell color.
     * The entire supplied list is validated before the current
     * contents are replaced.</p>
     *
     * @throws IllegalArgumentException if any element is {@code null}
     *                                  or has a different color
     */
    @Override
    public void setState(@NonNull List<P> checkers) {
        for (int i = 0; i < checkers.size(); i++) {
            P checker = Validate.notNull(checkers.get(i), "Checker at index %d is null", i);

            CheckerColor checkerColor = checker.getColor();
            Validate.isTrue(this.color.equals(checkerColor),
                    "Checker at index %d has color %s; expected %s",
                    i, checkerColor.getCode(), this.color.getCode());
        }
        super.setState(checkers);
    }

}
