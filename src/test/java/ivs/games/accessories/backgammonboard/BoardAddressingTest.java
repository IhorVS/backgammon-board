package ivs.games.accessories.backgammonboard;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import ivs.games.accessories.backgammonboard.standard.StandardCheckerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static ivs.games.accessories.backgammonboard.standard.StandardCheckerColor.BLACK;
import static ivs.games.accessories.backgammonboard.standard.StandardCheckerColor.WHITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies side-relative board addressing, inverse conversions, immutable mapping
 * copies, and validation of side identifiers and complete mapping ranges.
 */
class BoardAddressingTest {

    /**
     * Checks every point of both long-backgammon routes and its inverse conversion.
     */
    @ParameterizedTest
    @CsvSource({"WHITE, 0", "BLACK, 12"})
    void convertsEveryRoutePoint(StandardCheckerColor side, int offset) {
        BoardAddressing<StandardCheckerColor> addressing = createAddressing();

        for (int point = 1; point <= 24; point++) {
            int expectedIndex = (point - 1 + offset) % 24;

            assertEquals(expectedIndex, addressing.toBoardIndex(side, point));
            assertEquals(point, addressing.toRoutePoint(side, expectedIndex));
        }
    }

    /**
     * Checks arbitrary permutations and side identifiers compared by value.
     */
    @Test
    void supportsCustomRoutesAndSideIdentifiers() {
        BiMap<Integer, Integer> first = HashBiMap.create();
        BiMap<Integer, Integer> second = HashBiMap.create();

        for (int point = 1; point <= 24; point++) {
            first.put(point, (point - 1) * 5 % 24);
            second.put(point, 24 - point);
        }

        BoardAddressing<String> addressing = new BoardAddressing<>("north", first, "south", second);
        String equivalentFirstSide = new String(new char[]{'n', 'o', 'r', 't', 'h'});

        for (int point = 1; point <= 24; point++) {
            int firstIndex = (point - 1) * 5 % 24;
            int secondIndex = 24 - point;

            assertEquals(firstIndex, addressing.toBoardIndex(equivalentFirstSide, point));
            assertEquals(point, addressing.toRoutePoint(equivalentFirstSide, firstIndex));
            assertEquals(secondIndex, addressing.toBoardIndex("south", point));
            assertEquals(point, addressing.toRoutePoint("south", secondIndex));
        }
    }

    /**
     * Checks that distinct sides may use the same mapping.
     */
    @Test
    void acceptsIdenticalMappingsForDifferentSides() {
        BiMap<Integer, Integer> mapping = createMapping(0);
        BoardAddressing<String> addressing = new BoardAddressing<>("A", mapping, "B", mapping);

        assertEquals(6, addressing.toBoardIndex("A", 7));
        assertEquals(6, addressing.toBoardIndex("B", 7));
    }

    /**
     * Checks that changing supplied maps does not alter either stored mapping or its inverse.
     */
    @Test
    void copiesSuppliedMappings() {
        BiMap<Integer, Integer> white = createMapping(0);
        BiMap<Integer, Integer> black = createMapping(12);
        BoardAddressing<StandardCheckerColor> addressing = new BoardAddressing<>(WHITE, white, BLACK, black);

        white.clear();
        black.clear();
        white.put(1, 23);
        black.put(1, 0);

        assertEquals(0, addressing.toBoardIndex(WHITE, 1));
        assertEquals(1, addressing.toRoutePoint(WHITE, 0));
        assertEquals(12, addressing.toBoardIndex(BLACK, 1));
        assertEquals(1, addressing.toRoutePoint(BLACK, 12));
    }

    /**
     * Checks that side identifiers must be distinct according to equals().
     */
    @Test
    void rejectsEqualSides() {
        String firstSide = "same";
        String secondSide = new String(new char[]{'s', 'a', 'm', 'e'});

        assertThrows(IllegalArgumentException.class,
                () -> new BoardAddressing<>(firstSide, createMapping(0), secondSide, createMapping(12)));
    }

    /**
     * Checks null side identifiers in both constructor positions.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectsNullSideInConstructor(boolean firstSide) {
        StandardCheckerColor first = firstSide ? null : WHITE;
        StandardCheckerColor second = firstSide ? BLACK : null;

        assertThrows(NullPointerException.class,
                () -> new BoardAddressing<>(first, createMapping(0), second, createMapping(12)));
    }

    /**
     * Checks null mappings in both constructor positions.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectsNullMapping(boolean firstMapping) {
        BiMap<Integer, Integer> first = firstMapping ? null : createMapping(0);
        BiMap<Integer, Integer> second = firstMapping ? createMapping(12) : null;

        assertThrows(NullPointerException.class, () -> new BoardAddressing<>(WHITE, first, BLACK, second));
    }

    /**
     * Checks that both mappings must contain exactly 24 entries.
     */
    @ParameterizedTest
    @CsvSource({
            "true, 0",
            "true, 23",
            "true, 25",
            "false, 0",
            "false, 23",
            "false, 25"
    })
    void rejectsIncorrectMappingSize(boolean firstMapping, int size) {
        BiMap<Integer, Integer> mapping = HashBiMap.create();

        for (int point = 1; point <= size; point++) {
            mapping.put(point, point - 1);
        }

        assertThrows(IllegalArgumentException.class, () -> createWithMapping(firstMapping, mapping));
    }

    /**
     * Checks complete route-point coverage even when the mapping contains 24 entries.
     */
    @ParameterizedTest
    @CsvSource({"true, 0", "true, 25", "false, 0", "false, 25"})
    void rejectsInvalidRoutePointInMapping(boolean firstMapping, int invalidPoint) {
        BiMap<Integer, Integer> mapping = createMapping(0);
        int index = mapping.remove(1);
        mapping.put(invalidPoint, index);

        assertThrows(IllegalArgumentException.class, () -> createWithMapping(firstMapping, mapping));
    }

    /**
     * Checks complete board-index coverage even when all route points are present.
     */
    @ParameterizedTest
    @CsvSource({"true, -1", "true, 24", "false, -1", "false, 24"})
    void rejectsInvalidBoardIndexInMapping(boolean firstMapping, int invalidIndex) {
        BiMap<Integer, Integer> mapping = createMapping(0);
        mapping.put(1, invalidIndex);

        assertThrows(IllegalArgumentException.class, () -> createWithMapping(firstMapping, mapping));
    }

    /**
     * Checks that neither supplied mapping may contain a null route point.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectsNullMappingKey(boolean firstMapping) {
        BiMap<Integer, Integer> mapping = createMapping(0);
        int index = mapping.remove(1);
        mapping.put(null, index);

        assertThrows(NullPointerException.class, () -> createWithMapping(firstMapping, mapping));
    }

    /**
     * Checks that neither supplied mapping may contain a null board index.
     */
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void rejectsNullMappingValue(boolean firstMapping) {
        BiMap<Integer, Integer> mapping = createMapping(0);
        mapping.put(1, null);

        assertThrows(NullPointerException.class, () -> createWithMapping(firstMapping, mapping));
    }

    /**
     * Checks route-point bounds for both sides without allowing modular wrapping.
     */
    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 25, Integer.MAX_VALUE})
    void rejectsRoutePointOutsideRange(int routePoint) {
        BoardAddressing<StandardCheckerColor> addressing = createAddressing();

        assertThrows(IllegalArgumentException.class, () -> addressing.toBoardIndex(WHITE, routePoint));
        assertThrows(IllegalArgumentException.class, () -> addressing.toBoardIndex(BLACK, routePoint));
    }

    /**
     * Checks absolute board-index bounds for both sides without allowing modular wrapping.
     */
    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 24, Integer.MAX_VALUE})
    void rejectsBoardIndexOutsideRange(int boardIndex) {
        BoardAddressing<StandardCheckerColor> addressing = createAddressing();

        assertThrows(IllegalArgumentException.class, () -> addressing.toRoutePoint(WHITE, boardIndex));
        assertThrows(IllegalArgumentException.class, () -> addressing.toRoutePoint(BLACK, boardIndex));
    }

    /**
     * Checks null sides in both conversion methods.
     */
    @Test
    void rejectsNullSideDuringConversion() {
        BoardAddressing<StandardCheckerColor> addressing = createAddressing();

        assertThrows(NullPointerException.class, () -> addressing.toBoardIndex(null, 1));
        assertThrows(NullPointerException.class, () -> addressing.toRoutePoint(null, 0));
    }

    /**
     * Checks that a side not configured in the constructor cannot be used.
     */
    @Test
    void rejectsUnknownSide() {
        BoardAddressing<String> addressing = new BoardAddressing<>("A", createMapping(0), "B", createMapping(12));

        assertThrows(IllegalArgumentException.class, () -> addressing.toBoardIndex("C", 1));
        assertThrows(IllegalArgumentException.class, () -> addressing.toRoutePoint("C", 0));
    }

    private static BoardAddressing<StandardCheckerColor> createAddressing() {
        return new BoardAddressing<>(WHITE, createMapping(0), BLACK, createMapping(12));
    }

    private static void createWithMapping(
            boolean firstMapping, BiMap<Integer, Integer> mapping) {

        if (firstMapping) {
            new BoardAddressing<>(WHITE, mapping, BLACK, createMapping(12));
        } else {
            new BoardAddressing<>(WHITE, createMapping(0), BLACK, mapping);
        }
    }

    private static BiMap<Integer, Integer> createMapping(int offset) {
        BiMap<Integer, Integer> mapping = HashBiMap.create();

        for (int point = 1; point <= 24; point++) {
            mapping.put(point, (point - 1 + offset) % 24);
        }

        return mapping;
    }
}
