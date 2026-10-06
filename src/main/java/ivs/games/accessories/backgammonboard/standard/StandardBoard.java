package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BackgammonBoard;
import ivs.games.accessories.backgammonboard.BackgammonBoardView;
import ivs.games.accessories.backgammonboard.BoardCell;
import ivs.games.accessories.backgammonboard.BoardCellView;
import ivs.games.accessories.backgammonboard.BoardState;
import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A standard mutable backgammon board.
 *
 * <p>The board contains 24 playing cells, indexed from 0 to 23,
 * and separate bar and borne-off cells for white and black checkers.</p>
 *
 * <p>Playing cells accept checkers of any type and color.
 * Special cells accept only their assigned standard color.
 * The board does not enforce game rules.</p>
 *
 * <p>Cell access methods return the actual mutable cells, not copies.
 * Access through {@link BackgammonBoardView} exposes cells through
 * {@link BoardCellView}, which provides only read operations.
 * Access through {@link BackgammonBoard} exposes {@link BoardCell},
 * which also provides modification operations.
 * Both interfaces refer to the same underlying cells.</p>
 *
 * <p>This implementation is not thread-safe.</p>
 *
 * @param <P> the type of checkers stored on this board
 */
public class StandardBoard<P extends Checker<?, ?>>
        implements BackgammonBoard<P> {

    /**
     * The number of playing cells on a standard backgammon board.
     */
    public static final int PLAYING_CELL_COUNT = 24;

    private final Map<CheckerColor, ColoredCell<P>> bars;
    private final Map<CheckerColor, ColoredCell<P>> borneOff;
    private final List<StandardCell<P>> cells;

    /**
     * Creates an empty standard backgammon board.
     */
    public StandardBoard() {
        this.cells = IntStream.range(0, PLAYING_CELL_COUNT)
                .mapToObj(index -> new StandardCell<P>())
                .toList();

        this.bars = createColoredCells();
        this.borneOff = createColoredCells();
    }

    /**
     * Creates an independent copy of the supplied board.
     *
     * <p>All playing, bar, and borne-off cells are copied,
     * preserving checker order. Immutable checker objects are shared.
     * Subsequent changes to either board do not affect the other.</p>
     *
     * @param source the board to copy
     * @throws NullPointerException     if {@code source} is {@code null}
     * @throws IllegalArgumentException if the playing cell count differs
     *                                  from {@link #PLAYING_CELL_COUNT},
     *                                  the source does not support standard colors,
     *                                  or a special cell contains a checker
     *                                  of a different color
     */
    public StandardBoard(@NonNull BackgammonBoardView<P> source) {
        int sourceCellCount = source.getPlayingCellCount();
        Validate.isTrue(sourceCellCount == PLAYING_CELL_COUNT,
                "Source board has %d playing cells; expected %d",
                sourceCellCount, PLAYING_CELL_COUNT);

        this.cells = IntStream.range(0, PLAYING_CELL_COUNT)
                .mapToObj(index -> new StandardCell<>(source.getCell(index)))
                .toList();

        this.bars = Map.of(
                StandardCheckerColor.WHITE,
                new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        source.getBar(StandardCheckerColor.WHITE)),
                StandardCheckerColor.BLACK,
                new ColoredCell<>(
                        StandardCheckerColor.BLACK,
                        source.getBar(StandardCheckerColor.BLACK)));

        this.borneOff = Map.of(
                StandardCheckerColor.WHITE,
                new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        source.getBorneOff(StandardCheckerColor.WHITE)),
                StandardCheckerColor.BLACK,
                new ColoredCell<>(
                        StandardCheckerColor.BLACK,
                        source.getBorneOff(StandardCheckerColor.BLACK)));
    }

    /**
     * Creates a board from the supplied state.
     *
     * <p>Checker order is preserved. Subsequent changes to the board
     * do not affect the supplied state. Immutable checker objects are shared.</p>
     *
     * <p>The state must contain {@link #PLAYING_CELL_COUNT} playing cells.
     * Bar and borne-off maps must each contain exactly the standard
     * white and black colors. Special cell checkers must match their cell color.</p>
     *
     * <p>Additional properties defined by state subclasses are not processed.</p>
     *
     * @param state the state from which to create the board
     * @throws NullPointerException     if {@code state} is {@code null}
     * @throws IllegalArgumentException if the playing cell count or special
     *                                  cell colors are invalid, or a special
     *                                  cell contains a checker of a different color
     */
    public StandardBoard(@NonNull BoardState<P> state) {
        int cellCount = state.getCells().size();
        Validate.isTrue(cellCount == PLAYING_CELL_COUNT,
                "State has %d playing cells; expected %d",
                cellCount, PLAYING_CELL_COUNT);

        var expectedColors = Set.of(
                StandardCheckerColor.WHITE,
                StandardCheckerColor.BLACK);

        Validate.isTrue(state.getBars().keySet().equals(expectedColors),
                "Bar colors must be exactly %s", expectedColors);
        Validate.isTrue(state.getBorneOff().keySet().equals(expectedColors),
                "Borne-off colors must be exactly %s", expectedColors);

        this.cells = state.getCells().stream()
                .map(StandardCell::new)
                .toList();

        this.bars = Map.of(
                StandardCheckerColor.WHITE,
                new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        state.getBars().get(StandardCheckerColor.WHITE)),
                StandardCheckerColor.BLACK,
                new ColoredCell<>(
                        StandardCheckerColor.BLACK,
                        state.getBars().get(StandardCheckerColor.BLACK)));

        this.borneOff = Map.of(
                StandardCheckerColor.WHITE,
                new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        state.getBorneOff().get(StandardCheckerColor.WHITE)),
                StandardCheckerColor.BLACK,
                new ColoredCell<>(
                        StandardCheckerColor.BLACK,
                        state.getBorneOff().get(StandardCheckerColor.BLACK)));
    }

    /**
     * Returns the mutable playing cell at the specified index.
     *
     * <p>The returned cell belongs to this board. Changes to its contents
     * are reflected in the board.</p>
     *
     * @param index the playing cell index, from {@code 0} inclusive
     *              to {@link #PLAYING_CELL_COUNT} exclusive
     * @return the mutable playing cell
     * @throws IllegalArgumentException if the index is outside the board
     */
    @Override
    public BoardCell<P> getCell(int index) {
        Validate.isTrue(index >= 0 && index < PLAYING_CELL_COUNT,
                "Cell index %d is outside the board. Valid indices: 0 to %d",
                index, PLAYING_CELL_COUNT - 1);

        return cells.get(index);
    }

    /**
     * Returns the mutable bar cell for the specified standard color.
     *
     * <p>The returned cell belongs to this board and accepts only checkers
     * of the specified color. Changes to its contents are reflected
     * in the board.</p>
     *
     * @param color the standard color identifying the bar cell
     * @return the mutable bar cell
     * @throws NullPointerException     if {@code color} is {@code null}
     * @throws IllegalArgumentException if the color is not supported
     */
    @Override
    public BoardCell<P> getBar(@NonNull CheckerColor color) {
        return getPColoredCell(color, bars);
    }

    /**
     * Returns the mutable borne-off cell for the specified standard color.
     *
     * <p>The returned cell belongs to this board and accepts only checkers
     * of the specified color. Changes to its contents are reflected
     * in the board.</p>
     *
     * @param color the standard color identifying the borne-off cell
     * @return the mutable borne-off cell
     * @throws NullPointerException     if {@code color} is {@code null}
     * @throws IllegalArgumentException if the color is not supported
     */
    @Override
    public BoardCell<P> getBorneOff(@NonNull CheckerColor color) {
        return getPColoredCell(color, borneOff);
    }

    /**
     * Returns the special cell associated with the requested color.
     *
     * @param color the requested non-null color
     * @param cell  the special cells indexed by color
     * @param <P>   the checker type
     * @return the matching colored cell
     * @throws IllegalArgumentException if the color is not supported
     */
    private static <P extends Checker<?, ?>> ColoredCell<P> getPColoredCell(
            CheckerColor color,
            Map<CheckerColor, ColoredCell<P>> cell) {

        return Optional.ofNullable(cell.get(color))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported color: %s".formatted(color)));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return allCells().allMatch(BoardCell::isEmpty);
    }

    /** {@inheritDoc} */
    @Override
    public int getPlayingCellCount() {
        return PLAYING_CELL_COUNT;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        allCells().forEach(BoardCell::clear);
    }

    /** {@inheritDoc} */
    @Override
    public void move(int sourceIndex, int destinationIndex) {
        BoardCell<P> source = getCell(sourceIndex);
        BoardCell<P> destination = getCell(destinationIndex);

        Validate.isTrue(sourceIndex != destinationIndex,
                "Source and destination cell indices must differ: %d",
                sourceIndex);

        transfer(source, destination);
    }

    /** {@inheritDoc} */
    @Override
    public void moveToBar(int sourceIndex) {
        BoardCell<P> source = getCell(sourceIndex);
        P checker = source.peek();
        BoardCell<P> destination = getBar(checker.getColor());

        transfer(source, destination, checker);
    }

    /** {@inheritDoc} */
    @Override
    public void moveFromBar(
            @NonNull CheckerColor color,
            int destinationIndex) {

        BoardCell<P> source = getBar(color);
        BoardCell<P> destination = getCell(destinationIndex);

        transfer(source, destination);
    }

    /** {@inheritDoc} */
    @Override
    public void moveToBorneOff(int sourceIndex) {
        BoardCell<P> source = getCell(sourceIndex);
        P checker = source.peek();
        BoardCell<P> destination = getBorneOff(checker.getColor());

        transfer(source, destination, checker);
    }

    /**
     * Creates one empty color-restricted cell for each standard color.
     *
     * @return the cells indexed by color
     */
    private Map<CheckerColor, ColoredCell<P>> createColoredCells() {
        return Map.of(
                StandardCheckerColor.WHITE,
                new ColoredCell<>(StandardCheckerColor.WHITE),
                StandardCheckerColor.BLACK,
                new ColoredCell<>(StandardCheckerColor.BLACK));
    }

    /**
     * Returns all playing, bar, and borne-off cells.
     *
     * @return a stream of all cells
     */
    private Stream<BoardCell<P>> allCells() {
        return Stream.concat(
                cells.stream(),
                Stream.concat(
                        bars.values().stream(),
                        borneOff.values().stream()));
    }

    /**
     * Moves the top checker between distinct cells owned by this board.
     *
     * @param source      the source cell
     * @param destination the destination cell
     */
    private void transfer(BoardCell<P> source, BoardCell<P> destination) {
        transfer(source, destination, source.peek());
    }

    /**
     * Adds the top checker to the destination before removing it from the source.
     *
     * <p>The source is non-empty and both cells are distinct standard cells
     * owned by this board. Once the destination accepts the checker,
     * removal from the source requires no further validation.</p>
     *
     * @param source      the source cell
     * @param destination the destination cell
     * @param checker     the source cell's top checker
     */
    private void transfer(
            BoardCell<P> source,
            BoardCell<P> destination,
            P checker) {

        destination.push(checker);
        source.pop();
    }

    /** {@inheritDoc} */
    @Override
    public void forEachCell(
            @NonNull BiConsumer<Integer, BoardCellView<P>> action) {

        for (int index = 0; index < cells.size(); index++) {
            action.accept(index, cells.get(index));
        }
    }

    /** {@inheritDoc} */
    @Override
    public BoardState<P> getState() {
        return new BoardState<>(
                cells.stream()
                        .map(StandardCell::getState)
                        .toList(),
                Map.of(
                        StandardCheckerColor.WHITE,
                        bars.get(StandardCheckerColor.WHITE).getState(),
                        StandardCheckerColor.BLACK,
                        bars.get(StandardCheckerColor.BLACK).getState()),
                Map.of(
                        StandardCheckerColor.WHITE,
                        borneOff.get(StandardCheckerColor.WHITE).getState(),
                        StandardCheckerColor.BLACK,
                        borneOff.get(StandardCheckerColor.BLACK).getState()));
    }
}
