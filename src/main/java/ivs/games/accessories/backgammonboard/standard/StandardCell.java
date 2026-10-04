package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BoardCell;
import ivs.games.accessories.backgammonboard.BoardCellView;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A standard mutable cell containing a stack of checkers.
 *
 * <p>Checkers are stored from bottom to top. Checkers of any type
 * and color may be placed on top; the cell does not enforce game rules.</p>
 *
 * <p>This implementation is not thread-safe.</p>
 *
 * @param <P> the type of checkers stored in this cell
 */
@NoArgsConstructor
public class StandardCell<P extends Checker<?, ?>> implements BoardCell<P> {

    private static final String CELL_IS_EMPTY = "Cell is empty";

    private final List<P> checkers = new ArrayList<>();

    /**
     * Creates a cell containing a snapshot of the supplied cell.
     *
     * <p>The checker order is preserved. Subsequent changes to either cell
     * do not affect the other. Immutable checker objects are shared.</p>
     *
     * @param cellView the cell to copy
     * @throws NullPointerException if {@code cellView} is {@code null}
     */
    public StandardCell(@NonNull BoardCellView<P> cellView) {
        this.checkers.addAll(cellView.getState());
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return checkers.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return checkers.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public P peek() {
        Validate.isTrue(!checkers.isEmpty(), CELL_IS_EMPTY);
        return checkers.getLast();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation accepts checkers of any type and color.</p>
     */
    @Override
    public void push(@NonNull P checker) {
        checkers.add(checker);
    }

    /** {@inheritDoc} */
    @Override
    public P pop() {
        Validate.isTrue(!checkers.isEmpty(), CELL_IS_EMPTY);
        return checkers.removeLast();
    }

    /** {@inheritDoc} */
    @Override
    public List<P> getState() {
        return List.copyOf(checkers);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation accepts checkers of any type and color.
     * The supplied list is checked for null elements before the current
     * contents are replaced.</p>
     */
    @Override
    public void setState(@NonNull List<P> checkers) {
        Validate.noNullElements(checkers);

        this.checkers.clear();
        this.checkers.addAll(checkers);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        checkers.clear();
    }

    /** {@inheritDoc} */
    @Override
    public int count(@NonNull CheckerColor color) {
        return (int) checkers.stream()
                .filter(checker -> color.equals(checker.getColor()))
                .count();
    }

    /** {@inheritDoc} */
    @Override
    public void forEach(@NonNull Consumer<? super P> action) {
        checkers.forEach(action);
    }

    /** {@inheritDoc} */
    @Override
    public CheckerColor getTopCheckerColor() {
        Validate.isTrue(!checkers.isEmpty(), CELL_IS_EMPTY);
        return checkers.getLast().getColor();
    }
}
