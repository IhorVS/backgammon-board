package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.Checker;
import ivs.games.accessories.backgammonboard.CheckerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests standard board builder configuration, defensive copying,
 * independent builds, and validation during board creation.
 */
@SuppressWarnings("DataFlowIssue")
class StandardBoardBuilderTest {

    private final StandardBoardBuilder<TestChecker> builder =
            new StandardBoardBuilder<>();

    /**
     * Verifies that an unconfigured builder creates an empty board
     * containing all standard playing and special cells.
     */
    @Test
    void buildsEmptyBoardWithoutConfiguration() {
        StandardBoard<TestChecker> board = builder.build();

        assertEquals(24, board.getPlayingCellCount());
        assertTrue(board.isEmpty());
        assertTrue(IntStream.range(0, board.getPlayingCellCount())
                .allMatch(index -> board.getCell(index).isEmpty()));

        assertTrue(board.getBar(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(board.getBar(StandardCheckerColor.BLACK).isEmpty());
        assertTrue(board.getBorneOff(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(board.getBorneOff(StandardCheckerColor.BLACK).isEmpty());
    }

    /**
     * Verifies that configuration methods return the same builder
     * to support chained calls.
     */
    @Test
    void configurationMethodsReturnThisBuilder() {
        assertSame(builder, builder.cell(0, List.of(TestChecker.WHITE_MAN)));
        assertSame(builder, builder.bar(
                StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR)));
        assertSame(builder, builder.borneOff(
                StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN)));
    }

    /**
     * Verifies that playing cell configuration preserves cell indices
     * and bottom-to-top order, including mixed colors and checker types.
     */
    @Test
    void buildsConfiguredPlayingCellsAndLeavesOthersEmpty() {
        builder.cell(0, List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.WHITE_EMIR));
        builder.cell(23, List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR));

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.WHITE_EMIR), board.getCell(0).getState());
        assertSame(TestChecker.WHITE_EMIR, board.getCell(0).peek());

        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), board.getCell(23).getState());
        assertSame(TestChecker.BLACK_EMIR, board.getCell(23).peek());

        assertTrue(IntStream.range(1, 23)
                .allMatch(index -> board.getCell(index).isEmpty()));

        assertTrue(board.getBar(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(board.getBar(StandardCheckerColor.BLACK).isEmpty());
        assertTrue(board.getBorneOff(StandardCheckerColor.WHITE).isEmpty());
        assertTrue(board.getBorneOff(StandardCheckerColor.BLACK).isEmpty());
    }

    /**
     * Verifies that bar and borne-off configurations preserve checker order
     * and affect only the selected color.
     *
     * @param color the standard color to configure
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void buildsConfiguredSpecialCells(StandardCheckerColor color) {
        TestChecker man = man(color);
        TestChecker emir = emir(color);

        builder.bar(color, List.of(man, emir));
        builder.borneOff(color, List.of(emir, man));

        StandardBoard<TestChecker> board = builder.build();
        StandardCheckerColor opposite = StandardCheckerColor.getOpposite(color);

        assertEquals(List.of(man, emir), board.getBar(color).getState());
        assertSame(emir, board.getBar(color).peek());
        assertEquals(List.of(emir, man), board.getBorneOff(color).getState());
        assertSame(man, board.getBorneOff(color).peek());

        assertTrue(board.getBar(opposite).isEmpty());
        assertTrue(board.getBorneOff(opposite).isEmpty());
        assertTrue(IntStream.range(0, board.getPlayingCellCount())
                .allMatch(index -> board.getCell(index).isEmpty()));
    }

    /**
     * Verifies that repeated configuration replaces the previous contents
     * of the selected cell without changing other configured cells.
     */
    @Test
    void replacesPreviousCellConfigurations() {
        builder.cell(0, List.of(TestChecker.WHITE_MAN));
        builder.cell(1, List.of(TestChecker.BLACK_MAN));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_MAN));
        builder.bar(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));
        builder.borneOff(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_MAN));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));

        builder.cell(0, List.of(TestChecker.WHITE_EMIR));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(TestChecker.WHITE_EMIR), board.getCell(0).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN), board.getCell(1).getState());

        assertEquals(List.of(TestChecker.WHITE_EMIR),
                board.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                board.getBar(StandardCheckerColor.BLACK).getState());

        assertEquals(List.of(TestChecker.WHITE_EMIR),
                board.getBorneOff(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                board.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that empty lists replace previously configured stacks
     * with empty cell contents.
     */
    @Test
    void emptyListsReplacePreviousConfigurations() {
        builder.cell(0, List.of(TestChecker.WHITE_MAN));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));

        builder.cell(0, List.of());
        builder.bar(StandardCheckerColor.WHITE, List.of());
        builder.borneOff(StandardCheckerColor.BLACK, List.of());

        assertTrue(builder.build().isEmpty());
    }

    /**
     * Verifies that configuration methods copy supplied lists.
     * Later changes to those lists do not change the builder arrangement.
     */
    @Test
    void copiesSuppliedCheckerLists() {
        List<TestChecker> playing = new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR));
        List<TestChecker> bar = new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR));
        List<TestChecker> borneOff = new ArrayList<>(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR));

        builder.cell(0, playing);
        builder.bar(StandardCheckerColor.WHITE, bar);
        builder.borneOff(StandardCheckerColor.BLACK, borneOff);

        playing.clear();
        bar.set(0, TestChecker.BLACK_MAN);
        borneOff.add(TestChecker.WHITE_MAN);

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_EMIR), board.getCell(0).getState());
        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR), board.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), board.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that repeated builds create distinct boards and cells.
     * Changing either board does not affect the other or future builds.
     */
    @Test
    void repeatedBuildsCreateIndependentBoardsWithoutResettingBuilder() {
        builder.cell(0, List.of(TestChecker.WHITE_MAN, TestChecker.BLACK_EMIR));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));

        StandardBoard<TestChecker> first = builder.build();
        StandardBoard<TestChecker> second = builder.build();

        assertNotSame(first, second);

        for (int index = 0; index < first.getPlayingCellCount(); index++) {
            assertNotSame(first.getCell(index), second.getCell(index));
            assertEquals(first.getCell(index).getState(), second.getCell(index).getState());
        }

        assertNotSame(first.getBar(StandardCheckerColor.WHITE),
                second.getBar(StandardCheckerColor.WHITE));
        assertNotSame(first.getBar(StandardCheckerColor.BLACK),
                second.getBar(StandardCheckerColor.BLACK));
        assertNotSame(first.getBorneOff(StandardCheckerColor.WHITE),
                second.getBorneOff(StandardCheckerColor.WHITE));
        assertNotSame(first.getBorneOff(StandardCheckerColor.BLACK),
                second.getBorneOff(StandardCheckerColor.BLACK));

        first.clear();

        assertEquals(List.of(TestChecker.WHITE_MAN, TestChecker.BLACK_EMIR),
                second.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                second.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                second.getBorneOff(StandardCheckerColor.BLACK).getState());

        second.moveToBar(0);
        second.moveToBorneOff(0);

        assertTrue(first.isEmpty());

        StandardBoard<TestChecker> third = builder.build();

        assertEquals(List.of(TestChecker.WHITE_MAN, TestChecker.BLACK_EMIR),
                third.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                third.getBar(StandardCheckerColor.WHITE).getState());
        assertTrue(third.getBar(StandardCheckerColor.BLACK).isEmpty());
        assertTrue(third.getBorneOff(StandardCheckerColor.WHITE).isEmpty());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                third.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that reconfiguring the builder affects subsequent builds
     * without changing a previously built board.
     */
    @Test
    void builderChangesDoNotAffectPreviouslyBuiltBoard() {
        builder.cell(0, List.of(TestChecker.WHITE_MAN));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_MAN));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));

        StandardBoard<TestChecker> first = builder.build();

        builder.cell(0, List.of(TestChecker.BLACK_EMIR));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_EMIR));

        StandardBoard<TestChecker> second = builder.build();

        assertEquals(List.of(TestChecker.WHITE_MAN), first.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_MAN),
                first.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                first.getBorneOff(StandardCheckerColor.BLACK).getState());

        assertEquals(List.of(TestChecker.BLACK_EMIR), second.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                second.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_EMIR),
                second.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that configuration accepts lists whose checker type
     * is a subtype of the builder checker type.
     */
    @Test
    void acceptsListsOfCheckerSubtype() {
        StandardBoardBuilder<Checker<?, ?>> genericBuilder =
                new StandardBoardBuilder<>();
        List<TestChecker> checkers = List.of(TestChecker.WHITE_MAN);

        StandardBoard<Checker<?, ?>> board = genericBuilder
                .cell(0, checkers)
                .bar(StandardCheckerColor.WHITE, checkers)
                .borneOff(StandardCheckerColor.WHITE, checkers)
                .build();

        assertSame(TestChecker.WHITE_MAN, board.getCell(0).peek());
        assertSame(TestChecker.WHITE_MAN, board.getBar(StandardCheckerColor.WHITE).peek());
        assertSame(TestChecker.WHITE_MAN, board.getBorneOff(StandardCheckerColor.WHITE).peek());
    }

    /**
     * Verifies that null checker lists are rejected during configuration
     * without replacing previously configured contents.
     */
    @Test
    void rejectsNullListsWithoutChangingConfiguration() {
        builder.cell(0, List.of(TestChecker.WHITE_MAN));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_MAN));

        assertThrows(NullPointerException.class, () -> builder.cell(0, null));
        assertThrows(NullPointerException.class,
                () -> builder.bar(StandardCheckerColor.WHITE, null));
        assertThrows(NullPointerException.class,
                () -> builder.borneOff(StandardCheckerColor.BLACK, null));

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(TestChecker.WHITE_MAN), board.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                board.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_MAN),
                board.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Verifies that null checker elements are rejected at any position
     * without replacing previously configured contents.
     *
     * @param index the position of the null checker
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsNullElementsWithoutChangingConfiguration(int index) {
        builder.cell(0, List.of(TestChecker.WHITE_EMIR));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));
        builder.borneOff(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_EMIR));

        List<TestChecker> invalid = new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_MAN));
        invalid.set(index, null);

        assertThrows(NullPointerException.class, () -> builder.cell(0, invalid));
        assertThrows(NullPointerException.class,
                () -> builder.bar(StandardCheckerColor.WHITE, invalid));
        assertThrows(NullPointerException.class,
                () -> builder.borneOff(StandardCheckerColor.WHITE, invalid));

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(TestChecker.WHITE_EMIR), board.getCell(0).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                board.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.WHITE_EMIR),
                board.getBorneOff(StandardCheckerColor.WHITE).getState());
    }

    /**
     * Verifies that special cell configuration rejects null colors
     * without adding any configuration.
     */
    @Test
    void rejectsNullSpecialCellColors() {
        assertThrows(NullPointerException.class,
                () -> builder.bar(null, List.of()));
        assertThrows(NullPointerException.class,
                () -> builder.borneOff(null, List.of()));

        assertTrue(builder.build().isEmpty());
    }

    /**
     * Verifies that invalid playing cell indices are stored during
     * configuration and rejected when the board is built.
     * Empty checker lists do not bypass index validation.
     *
     * @param index the invalid playing cell index
     */
    @ParameterizedTest
    @ValueSource(ints = {-1, 24, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void rejectsInvalidPlayingCellIndicesDuringBuild(int index) {
        assertDoesNotThrow(() -> builder.cell(index, List.of()));

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     * Verifies that unsupported special cell colors are rejected during
     * board creation, even when they use a standard color code.
     * Empty checker lists do not bypass color validation.
     */
    @Test
    void rejectsUnsupportedSpecialCellColorsDuringBuild() {
        CheckerColor unsupported = () -> "W";

        StandardBoardBuilder<TestChecker> barBuilder =
                new StandardBoardBuilder<>();
        StandardBoardBuilder<TestChecker> borneOffBuilder =
                new StandardBoardBuilder<>();

        assertDoesNotThrow(() -> barBuilder.bar(unsupported, List.of()));
        assertDoesNotThrow(() -> borneOffBuilder.borneOff(unsupported, List.of()));

        assertThrows(IllegalArgumentException.class, barBuilder::build);
        assertThrows(IllegalArgumentException.class, borneOffBuilder::build);
    }

    /**
     * Verifies that a checker of the wrong color is rejected during
     * board creation at any position in either special cell stack.
     *
     * @param index the position of the checker with the wrong color
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsWrongSpecialCellCheckerColorDuringBuild(int index) {
        List<TestChecker> invalid = new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_MAN));
        invalid.set(index, TestChecker.BLACK_EMIR);

        StandardBoardBuilder<TestChecker> barBuilder =
                new StandardBoardBuilder<>();
        StandardBoardBuilder<TestChecker> borneOffBuilder =
                new StandardBoardBuilder<>();

        assertDoesNotThrow(() -> barBuilder.bar(StandardCheckerColor.WHITE, invalid));
        assertDoesNotThrow(() -> borneOffBuilder.borneOff(StandardCheckerColor.WHITE, invalid));

        IllegalArgumentException barException =
                assertThrows(IllegalArgumentException.class, barBuilder::build);
        IllegalArgumentException borneOffException =
                assertThrows(IllegalArgumentException.class, borneOffBuilder::build);

        String expected = "Checker at index %d has color B; expected W".formatted(index);

        assertEquals(expected, barException.getMessage());
        assertEquals(expected, borneOffException.getMessage());
    }

    /**
     * Verifies that a failed build preserves all configuration.
     * Replacing invalid contents allows a later build to succeed.
     */
    @Test
    void failedBuildPreservesConfigurationAndAllowsCorrection() {
        builder.cell(7, List.of(TestChecker.BLACK_EMIR));
        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.BLACK_MAN));
        builder.borneOff(StandardCheckerColor.BLACK, List.of(TestChecker.BLACK_EMIR));

        assertThrows(IllegalArgumentException.class, builder::build);
        assertThrows(IllegalArgumentException.class, builder::build);

        builder.bar(StandardCheckerColor.WHITE, List.of(TestChecker.WHITE_MAN));

        StandardBoard<TestChecker> board = builder.build();

        assertEquals(List.of(TestChecker.BLACK_EMIR), board.getCell(7).getState());
        assertEquals(List.of(TestChecker.WHITE_MAN),
                board.getBar(StandardCheckerColor.WHITE).getState());
        assertEquals(List.of(TestChecker.BLACK_EMIR),
                board.getBorneOff(StandardCheckerColor.BLACK).getState());
    }

    /**
     * Returns the ordinary test checker of the specified standard color.
     *
     * @param color the standard color
     * @return the corresponding ordinary checker
     */
    private static TestChecker man(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_MAN
                : TestChecker.BLACK_MAN;
    }

    /**
     * Returns the emir test checker of the specified standard color.
     *
     * @param color the standard color
     * @return the corresponding emir checker
     */
    private static TestChecker emir(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_EMIR
                : TestChecker.BLACK_EMIR;
    }
}
