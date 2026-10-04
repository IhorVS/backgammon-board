package ivs.games.accessories.backgammonboard.standard;

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
     * Creates a copy of the supplied cell, preserving its color and checker order.
     *
     * <p>Subsequent changes to either cell do not affect the other.
     * Immutable checker and color objects are shared.</p>
     *
     * @param cell the cell to copy
     * @throws NullPointerException if {@code cell} is {@code null}
     */
    public ColoredCell(@NonNull ColoredCell<P> cell) {
        super(cell);
        this.color = cell.color;
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
