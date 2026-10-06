package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BoardCellView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests color-restricted cell operations and constructors.
 */
@SuppressWarnings("DataFlowIssue")
class ColoredCellTest {

    @Test
    void createsEmptyCellWithSpecifiedColor() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);

        assertSame(StandardCheckerColor.WHITE, cell.getColor());
        assertTrue(cell.isEmpty());
    }

    @Test
    void rejectsNullColor() {
        assertThrows(NullPointerException.class,
                () -> new ColoredCell<TestChecker>(
                        (StandardCheckerColor) null));
    }

    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void acceptsOrdinaryAndEmirCheckersOfConfiguredColor(
            StandardCheckerColor color) {

        ColoredCell<TestChecker> cell = new ColoredCell<>(color);
        TestChecker man = man(color);
        TestChecker emir = emir(color);

        cell.push(man);
        cell.push(emir);

        assertEquals(List.of(man, emir), cell.getState());
        assertSame(emir, cell.peek());
        assertSame(color, cell.getTopCheckerColor());
        assertEquals(2, cell.count(color));
    }

    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void rejectsOppositeColorWithoutChangingContents(
            StandardCheckerColor color) {

        ColoredCell<TestChecker> cell = new ColoredCell<>(color);
        cell.push(man(color));

        StandardCheckerColor opposite =
                StandardCheckerColor.getOpposite(color);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cell.push(man(opposite)));

        assertEquals(
                "Checker color " + opposite.getCode()
                        + " does not match cell color " + color.getCode(),
                exception.getMessage());
        assertEquals(List.of(man(color)), cell.getState());
    }

    @Test
    void rejectsNullCheckerWithoutChangingContents() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);
        cell.push(TestChecker.WHITE_MAN);

        assertThrows(NullPointerException.class, () -> cell.push(null));

        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void replacesStateWithMatchingCheckersAndCopiesList() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.BLACK);
        cell.push(TestChecker.BLACK_EMIR);

        List<TestChecker> supplied = new ArrayList<>(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.BLACK_MAN));

        cell.setState(supplied);
        supplied.clear();

        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.BLACK_MAN), cell.getState());
        assertEquals(3, cell.count(StandardCheckerColor.BLACK));
        assertSame(TestChecker.BLACK_MAN, cell.peek());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsWrongColorAtAnyIndexWithoutChangingContents(int index) {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);
        List<TestChecker> original = List.of(TestChecker.WHITE_EMIR);
        cell.setState(original);

        List<TestChecker> invalid = whiteCheckers();
        invalid.set(index, TestChecker.BLACK_MAN);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cell.setState(invalid));

        assertEquals(
                "Checker at index " + index + " has color B; expected W",
                exception.getMessage());
        assertEquals(original, cell.getState());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void rejectsNullElementAtAnyIndexWithoutChangingContents(int index) {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);
        List<TestChecker> original = List.of(TestChecker.WHITE_EMIR);
        cell.setState(original);

        List<TestChecker> invalid = whiteCheckers();
        invalid.set(index, null);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> cell.setState(invalid));

        assertEquals("Checker at index " + index + " is null",
                exception.getMessage());
        assertEquals(original, cell.getState());
    }

    @Test
    void rejectsNullStateWithoutChangingContents() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);
        cell.push(TestChecker.WHITE_MAN);

        assertThrows(NullPointerException.class, () -> cell.setState(null));

        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void acceptsEmptyStateAndPreservesConfiguredColor() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.WHITE);
        cell.push(TestChecker.WHITE_MAN);

        cell.setState(List.of());

        assertTrue(cell.isEmpty());
        assertSame(StandardCheckerColor.WHITE, cell.getColor());

        cell.push(TestChecker.WHITE_EMIR);
        assertSame(TestChecker.WHITE_EMIR, cell.peek());
    }

    /**
     * Verifies that copying preserves color and checker order,
     * while subsequent changes to either cell remain independent.
     */
    @Test
    void constructorCopiesColorAndCheckersAndKeepsCellsIndependent() {
        ColoredCell<TestChecker> source =
                new ColoredCell<>(StandardCheckerColor.BLACK);
        source.setState(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR));

        ColoredCell<TestChecker> copy = new ColoredCell<>(source);

        assertSame(source.getColor(), copy.getColor());
        assertEquals(source.getState(), copy.getState());
        assertSame(TestChecker.BLACK_EMIR, copy.peek());

        source.pop();

        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR), copy.getState());

        copy.push(TestChecker.BLACK_MAN);

        assertEquals(List.of(TestChecker.BLACK_MAN), source.getState());
        assertEquals(List.of(
                TestChecker.BLACK_MAN,
                TestChecker.BLACK_EMIR,
                TestChecker.BLACK_MAN), copy.getState());

        assertThrows(IllegalArgumentException.class,
                () -> copy.push(TestChecker.WHITE_MAN));
    }

    @Test
    void constructorCopiesEmptyCell() {
        ColoredCell<TestChecker> source =
                new ColoredCell<>(StandardCheckerColor.WHITE);

        ColoredCell<TestChecker> copy = new ColoredCell<>(source);

        assertTrue(copy.isEmpty());
        assertSame(StandardCheckerColor.WHITE, copy.getColor());
    }

    @Test
    void rejectsNullSourceCell() {
        assertThrows(NullPointerException.class,
                () -> new ColoredCell<>(
                        (ColoredCell<TestChecker>) null));
    }

    /**
     * Verifies copying from a general cell view, preserving order
     * and sharing immutable checker objects without sharing cell contents.
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void constructorCopiesCellViewAndKeepsCellsIndependent(
            StandardCheckerColor color) {

        StandardCell<TestChecker> source = new StandardCell<>();
        source.setState(List.of(man(color), emir(color)));

        ColoredCell<TestChecker> copy =
                new ColoredCell<>(color, source);

        assertSame(color, copy.getColor());
        assertEquals(source.getState(), copy.getState());
        assertSame(emir(color), copy.peek());

        source.clear();

        assertEquals(List.of(man(color), emir(color)), copy.getState());

        copy.push(man(color));
        assertTrue(source.isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> copy.push(man(StandardCheckerColor.getOpposite(color))));
    }

    /**
     * Verifies that an empty source view creates an empty cell
     * with the explicitly supplied color.
     */
    @Test
    void constructorAcceptsEmptyCellView() {
        BoardCellView<TestChecker> source = new StandardCell<>();

        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.BLACK, source);

        assertTrue(cell.isEmpty());
        assertSame(StandardCheckerColor.BLACK, cell.getColor());
    }

    @Test
    void cellViewConstructorRejectsNullColor() {
        BoardCellView<TestChecker> source = new StandardCell<>();

        assertThrows(NullPointerException.class,
                () -> new ColoredCell<>(null, source));
    }

    @Test
    void cellViewConstructorRejectsNullSource() {
        assertThrows(NullPointerException.class,
                () -> new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        (BoardCellView<TestChecker>) null));
    }

    /**
     * Verifies that every source position is checked for color,
     * and that a rejected copy leaves the source unchanged.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void cellViewConstructorRejectsWrongColorAtAnyIndex(int index) {
        List<TestChecker> contents = whiteCheckers();
        contents.set(index, TestChecker.BLACK_MAN);

        StandardCell<TestChecker> source = new StandardCell<>();
        source.setState(contents);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        source));

        assertEquals(
                "Checker at index " + index + " has color B; expected W",
                exception.getMessage());
        assertEquals(contents, source.getState());
    }

    /**
     * Verifies that the list constructor preserves checker order
     * and copies the supplied mutable list.
     */
    @ParameterizedTest
    @EnumSource(StandardCheckerColor.class)
    void constructorCopiesCheckerList(StandardCheckerColor color) {
        List<TestChecker> supplied =
                new ArrayList<>(List.of(man(color), emir(color)));

        ColoredCell<TestChecker> cell = new ColoredCell<>(color, supplied);
        supplied.clear();

        assertSame(color, cell.getColor());
        assertEquals(List.of(man(color), emir(color)), cell.getState());
        assertSame(emir(color), cell.peek());

        cell.push(man(color));
        assertTrue(supplied.isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> cell.push(man(StandardCheckerColor.getOpposite(color))));
    }

    @Test
    void constructorAcceptsEmptyCheckerList() {
        ColoredCell<TestChecker> cell =
                new ColoredCell<>(StandardCheckerColor.BLACK, List.of());

        assertTrue(cell.isEmpty());
        assertSame(StandardCheckerColor.BLACK, cell.getColor());
    }

    @Test
    void checkerListConstructorRejectsNullColor() {
        assertThrows(NullPointerException.class,
                () -> new ColoredCell<>(
                        null, List.of(TestChecker.WHITE_MAN)));
    }

    @Test
    void checkerListConstructorRejectsNullList() {
        assertThrows(NullPointerException.class,
                () -> new ColoredCell<>(
                        StandardCheckerColor.WHITE,
                        (List<TestChecker>) null));
    }

    /**
     * Verifies that a checker of the wrong color is rejected
     * at any position in the supplied list.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void checkerListConstructorRejectsWrongColorAtAnyIndex(int index) {
        List<TestChecker> supplied = whiteCheckers();
        supplied.set(index, TestChecker.BLACK_MAN);
        List<TestChecker> before = List.copyOf(supplied);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ColoredCell<>(
                        StandardCheckerColor.WHITE, supplied));

        assertEquals(
                "Checker at index " + index + " has color B; expected W",
                exception.getMessage());
        assertEquals(before, supplied);
    }

    /**
     * Verifies that null checkers are rejected with their actual list index.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void checkerListConstructorRejectsNullElementAtAnyIndex(int index) {
        List<TestChecker> supplied = whiteCheckers();
        supplied.set(index, null);
        List<TestChecker> before = new ArrayList<>(supplied);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new ColoredCell<>(
                        StandardCheckerColor.WHITE, supplied));

        assertEquals("Checker at index " + index + " is null",
                exception.getMessage());
        assertEquals(before, supplied);
    }

    private static List<TestChecker> whiteCheckers() {
        return new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN));
    }

    private static TestChecker man(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_MAN
                : TestChecker.BLACK_MAN;
    }

    private static TestChecker emir(StandardCheckerColor color) {
        return color == StandardCheckerColor.WHITE
                ? TestChecker.WHITE_EMIR
                : TestChecker.BLACK_EMIR;
    }
}
