package ivs.games.accessories.backgammonboard;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.apache.commons.lang3.Validate;

/**
 * Translates side-relative route points to absolute playing cell indices and back.
 *
 * <p>The board stores playing cells using fixed indices from {@code 0} to {@code 23}.
 * Each side can address those cells using its own route points from {@code 1} to
 * {@code 24}. This component associates each side with an independently configured
 * addressing scheme.</p>
 *
 * <p>Mapping keys are route points, and values are absolute board indices. Each
 * mapping must contain every route point and every board index exactly once.
 * The mappings may be identical or describe different orders.</p>
 *
 * <p>Side identifiers are supplied by the game. They may be colors, player sides,
 * or other immutable values with stable {@code equals()} and {@code hashCode()}
 * implementations. This component does not require a particular side enum.</p>
 *
 * <p>Addressing does not define legal moves, initial arrangements, or rules for
 * entering and leaving the board. Bar and borne-off cells are not included.
 * The game engine determines how route points are used during play.</p>
 *
 * <h2>Example: long backgammon</h2>
 *
 * <p>The following example uses standard checker colors as side identifiers.
 * White route points map to board indices after subtracting one. Black uses
 * the same sequence shifted by twelve board cells.</p>
 *
 * <pre>{@code
 * ImmutableBiMap.Builder<Integer, Integer> white = ImmutableBiMap.builder();
 * ImmutableBiMap.Builder<Integer, Integer> black = ImmutableBiMap.builder();
 *
 * for (int point = 1; point <= BoardAddressing.PLAYING_CELL_COUNT; point++) {
 *     int index = point - 1;
 *     white.put(point, index);
 *     black.put(point, (index + 12) % BoardAddressing.PLAYING_CELL_COUNT);
 * }
 *
 * BoardAddressing<StandardCheckerColor> addressing = new BoardAddressing<>(
 *         StandardCheckerColor.WHITE, white.build(),
 *         StandardCheckerColor.BLACK, black.build());
 *
 * int whiteStart = addressing.toBoardIndex(StandardCheckerColor.WHITE, 1);  // 0
 * int blackStart = addressing.toBoardIndex(StandardCheckerColor.BLACK, 1);  // 12
 * int blackPoint = addressing.toRoutePoint(StandardCheckerColor.BLACK, 0);  // 13
 * }</pre>
 *
 * <p>The example requires imports for {@code ImmutableBiMap} and
 * {@code ivs.games.accessories.backgammonboard.standard.StandardCheckerColor}.</p>
 *
 * <p>A game engine can accept commands and report available moves using route
 * points relative to the acting side. Before accessing a playing cell, it calls
 * {@link #toBoardIndex(Object, int)}. To report an absolute board position using
 * a side's addressing scheme, it calls {@link #toRoutePoint(Object, int)}.</p>
 *
 * <p>Both mappings are copied into immutable bidirectional maps. Subsequent
 * changes to the supplied maps do not affect this component. Side identifiers
 * are shared and must remain immutable. Under this requirement, instances are
 * immutable and thread-safe.</p>
 *
 * @param <S> the type identifying the two sides
 */
public final class BoardAddressing<S> {

    /**
     * The number of playing cells covered by each side's mapping.
     */
    public static final int PLAYING_CELL_COUNT = 24;

    private final S firstSide;
    private final S secondSide;

    private final ImmutableBiMap<Integer, Integer> firstMapping;
    private final ImmutableBiMap<Integer, Integer> secondMapping;

    /**
     * Creates addressing schemes for two distinct sides.
     *
     * <p>Map keys are route points from {@code 1} to {@code 24}, inclusive.
     * Map values are absolute board indices from {@code 0} to {@code 23},
     * inclusive. Each map must cover both ranges completely.</p>
     *
     * <p>Sides are compared using {@code equals()}. They must be distinct and
     * immutable. The supplied maps must not be modified while this constructor
     * is executing.</p>
     *
     * @param firstSide     the identifier of the first side
     * @param firstMapping  the route-point-to-board-index mapping for the first side
     * @param secondSide    the identifier of the second side
     * @param secondMapping the route-point-to-board-index mapping for the second side
     * @throws NullPointerException     if any argument, mapping key, or mapping value is {@code null}
     * @throws IllegalArgumentException if the sides are equal or a mapping does not cover the required ranges
     */
    public BoardAddressing(
            S firstSide,
            BiMap<Integer, Integer> firstMapping,
            S secondSide,
            BiMap<Integer, Integer> secondMapping) {

        this.firstSide = Validate.notNull(firstSide, "First side must not be null");
        this.secondSide = Validate.notNull(secondSide, "Second side must not be null");

        Validate.isTrue(!firstSide.equals(secondSide), "Sides must be distinct: %s", firstSide);

        this.firstMapping = copyAndValidate(firstMapping, firstSide);
        this.secondMapping = copyAndValidate(secondMapping, secondSide);
    }

    /**
     * Returns the absolute board index corresponding to a side-relative route point.
     *
     * <p>The returned index can be passed to {@link BackgammonBoardView#getCell(int)}.</p>
     *
     * @param side       the side whose addressing scheme to use
     * @param routePoint the route point, from {@code 1} to {@code 24}, inclusive
     * @return the absolute board index, from {@code 0} to {@code 23}, inclusive
     * @throws NullPointerException     if {@code side} is {@code null}
     * @throws IllegalArgumentException if the side is unknown or the route point is outside the supported range
     */
    @SuppressWarnings("DataFlowIssue")
    public int toBoardIndex(S side, int routePoint) {
        ImmutableBiMap<Integer, Integer> mapping = getMapping(side);

        Validate.isTrue(routePoint >= 1 && routePoint <= PLAYING_CELL_COUNT,
                "Route point %d is outside the valid range: 1 to %d", routePoint, PLAYING_CELL_COUNT);

        return mapping.get(routePoint);
    }

    /**
     * Returns the side-relative route point corresponding to an absolute board index.
     *
     * <p>This is the inverse of {@link #toBoardIndex(Object, int)} for the same side.</p>
     *
     * @param side       the side whose addressing scheme to use
     * @param boardIndex the absolute board index, from {@code 0} to {@code 23}, inclusive
     * @return the route point, from {@code 1} to {@code 24}, inclusive
     * @throws NullPointerException     if {@code side} is {@code null}
     * @throws IllegalArgumentException if the side is unknown or the board index is outside the supported range
     */
    @SuppressWarnings("DataFlowIssue")
    public int toRoutePoint(S side, int boardIndex) {
        ImmutableBiMap<Integer, Integer> mapping = getMapping(side);

        Validate.isTrue(boardIndex >= 0 && boardIndex < PLAYING_CELL_COUNT,
                "Board index %d is outside the valid range: 0 to %d", boardIndex, PLAYING_CELL_COUNT - 1);

        return mapping.inverse().get(boardIndex);
    }

    /**
     * Returns the immutable mapping associated with a side.
     *
     * @param side the side whose mapping to return
     * @return the route-point-to-board-index mapping
     * @throws NullPointerException     if {@code side} is {@code null}
     * @throws IllegalArgumentException if the side was not configured
     */
    private ImmutableBiMap<Integer, Integer> getMapping(S side) {
        Validate.notNull(side, "Side must not be null");

        if (firstSide.equals(side)) {
            return firstMapping;
        }

        if (secondSide.equals(side)) {
            return secondMapping;
        }

        throw new IllegalArgumentException("Unknown side: " + side);
    }

    /**
     * Copies a mapping and validates complete coverage of route points and board indices.
     *
     * @param source the mapping to copy
     * @param side   the side used in validation messages
     * @return the validated immutable mapping
     * @throws NullPointerException     if the mapping, any key, or any value is {@code null}
     * @throws IllegalArgumentException if the mapping does not cover the required ranges exactly once
     */
    private static ImmutableBiMap<Integer, Integer> copyAndValidate(
            BiMap<Integer, Integer> source, Object side) {

        Validate.notNull(source, "Mapping must not be null for side: %s", side);
        ImmutableBiMap<Integer, Integer> mapping = ImmutableBiMap.copyOf(source);

        Validate.isTrue(mapping.size() == PLAYING_CELL_COUNT,
                "Mapping for side %s must contain %d entries; found %d",
                side, PLAYING_CELL_COUNT, mapping.size());

        for (int point = 1; point <= PLAYING_CELL_COUNT; point++) {
            Validate.isTrue(mapping.containsKey(point),
                    "Mapping for side %s is missing route point %d", side, point);
        }

        for (int index = 0; index < PLAYING_CELL_COUNT; index++) {
            Validate.isTrue(mapping.containsValue(index),
                    "Mapping for side %s is missing board index %d", side, index);
        }

        return mapping;
    }
}
