package ivs.games.accessories.backgammonboard.standard;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void acceptsOrdinaryAndEmirCheckersOfConfiguredColor() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
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
    }

    @Test
    void rejectsOppositeColorWithoutChangingContents() {
        for (StandardCheckerColor color : StandardCheckerColor.values()) {
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

    @Test
    void rejectsWrongColorAtAnyIndexWithoutChangingContents() {
        for (int index = 0; index < 3; index++) {
            ColoredCell<TestChecker> cell =
                    new ColoredCell<>(StandardCheckerColor.WHITE);
            List<TestChecker> original = List.of(TestChecker.WHITE_EMIR);
            cell.setState(original);

            List<TestChecker> invalid = new ArrayList<>(List.of(
                    TestChecker.WHITE_MAN,
                    TestChecker.WHITE_MAN,
                    TestChecker.WHITE_MAN));
            invalid.set(index, TestChecker.BLACK_MAN);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> cell.setState(invalid));

            assertEquals(
                    "Checker at index " + index + " has color B; expected W",
                    exception.getMessage());
            assertEquals(original, cell.getState());
        }
    }

    @Test
    void rejectsNullElementAtAnyIndexWithoutChangingContents() {
        for (int index = 0; index < 3; index++) {
            ColoredCell<TestChecker> cell =
                    new ColoredCell<>(StandardCheckerColor.WHITE);
            List<TestChecker> original = List.of(TestChecker.WHITE_EMIR);
            cell.setState(original);

            List<TestChecker> invalid = new ArrayList<>(List.of(
                    TestChecker.WHITE_MAN,
                    TestChecker.WHITE_MAN,
                    TestChecker.WHITE_MAN));
            invalid.set(index, null);

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> cell.setState(invalid));

            assertEquals("Checker at index " + index + " is null",
                    exception.getMessage());
            assertEquals(original, cell.getState());
        }
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
