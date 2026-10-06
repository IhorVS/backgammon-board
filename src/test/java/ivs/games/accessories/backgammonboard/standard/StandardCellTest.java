package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.BoardCellView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("DataFlowIssue")
class StandardCellTest {

    @Test
    void createsEmptyCell() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        assertTrue(cell.isEmpty());
        assertEquals(0, cell.size());
        assertEquals(List.of(), cell.getState());
        assertEquals(0, cell.count(StandardCheckerColor.WHITE));
        assertEquals(0, cell.count(StandardCheckerColor.BLACK));
    }

    @Test
    void acceptsMixedColorsAndTypesAndRemovesFromTop() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        cell.push(TestChecker.WHITE_MAN);
        cell.push(TestChecker.BLACK_MAN);
        cell.push(TestChecker.WHITE_EMIR);

        assertFalse(cell.isEmpty());
        assertEquals(3, cell.size());
        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR), cell.getState());

        assertSame(TestChecker.WHITE_EMIR, cell.peek());
        assertEquals(3, cell.size());
        assertSame(StandardCheckerColor.WHITE, cell.getTopCheckerColor());

        assertSame(TestChecker.WHITE_EMIR, cell.pop());
        assertSame(TestChecker.BLACK_MAN, cell.peek());
        assertSame(StandardCheckerColor.BLACK, cell.getTopCheckerColor());

        assertSame(TestChecker.BLACK_MAN, cell.pop());
        assertSame(TestChecker.WHITE_MAN, cell.pop());

        assertTrue(cell.isEmpty());
        assertEquals(0, cell.size());
    }

    @Test
    void permitsRepeatedCheckerInstances() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        cell.push(TestChecker.WHITE_MAN);
        cell.push(TestChecker.WHITE_MAN);

        assertEquals(2, cell.size());
        assertEquals(2, cell.count(StandardCheckerColor.WHITE));
        assertSame(TestChecker.WHITE_MAN, cell.pop());
        assertSame(TestChecker.WHITE_MAN, cell.pop());
    }

    @Test
    void rejectsTopOperationsOnEmptyCell() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        assertThrows(IllegalArgumentException.class, cell::peek);
        assertThrows(IllegalArgumentException.class, cell::pop);
        assertThrows(IllegalArgumentException.class, cell::getTopCheckerColor);

        assertTrue(cell.isEmpty());
    }

    @Test
    void rejectsNullCheckerWithoutChangingState() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);

        assertThrows(NullPointerException.class, () -> cell.push(null));

        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void returnsIndependentUnmodifiableSnapshot() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);

        List<TestChecker> snapshot = cell.getState();

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(TestChecker.BLACK_MAN));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.set(0, TestChecker.WHITE_EMIR));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        cell.pop();
        cell.push(TestChecker.BLACK_MAN);

        assertEquals(List.of(TestChecker.WHITE_MAN), snapshot);
        assertEquals(List.of(TestChecker.BLACK_MAN), cell.getState());
    }

    @Test
    void replacesStateAndCopiesSuppliedList() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_EMIR);

        List<TestChecker> supplied = new ArrayList<>(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN));

        cell.setState(supplied);
        supplied.clear();

        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN), cell.getState());
        assertEquals(2, cell.size());
        assertSame(TestChecker.BLACK_MAN, cell.peek());
        assertSame(StandardCheckerColor.BLACK, cell.getTopCheckerColor());
    }

    @Test
    void emptyStateClearsCell() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);

        cell.setState(List.of());

        assertTrue(cell.isEmpty());
        assertEquals(0, cell.count(StandardCheckerColor.WHITE));
    }

    @Test
    void rejectsNullStateWithoutChangingContents() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);

        assertThrows(NullPointerException.class, () -> cell.setState(null));

        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void rejectsNullElementWithoutChangingContents() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);

        List<TestChecker> invalidState = Arrays.asList(
                TestChecker.BLACK_MAN, null, TestChecker.WHITE_EMIR);

        assertThrows(IllegalArgumentException.class,
                () -> cell.setState(invalidState));

        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void copiesCellAndKeepsContentsIndependent() {
        StandardCell<TestChecker> source = new StandardCell<>();
        source.setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN));

        StandardCell<TestChecker> copy = new StandardCell<>(source);

        assertEquals(source.getState(), copy.getState());
        assertSame(source.peek(), copy.peek());

        source.pop();
        assertEquals(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN), copy.getState());

        copy.push(TestChecker.WHITE_EMIR);
        assertEquals(List.of(TestChecker.WHITE_MAN), source.getState());
    }

    @Test
    void copiesEmptyCell() {
        StandardCell<TestChecker> source = new StandardCell<>();
        StandardCell<TestChecker> copy = new StandardCell<>(source);

        assertTrue(copy.isEmpty());
        assertEquals(List.of(), copy.getState());
    }

    @Test
    void rejectsNullCopySource() {
        assertThrows(NullPointerException.class, () -> new StandardCell<>((BoardCellView<?>) null));
    }

    @Test
    void rejectsNullCheckerList() {
        assertThrows(NullPointerException.class, () -> new StandardCell<>((List<TestChecker>) null));
    }

    @Test
    void rejectsCheckerListWithNullElement() {
        List<TestChecker> supplied = new ArrayList<>();
        supplied.add(TestChecker.WHITE_MAN);
        supplied.add(TestChecker.WHITE_MAN);
        supplied.add(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new StandardCell<>(supplied));

        assertEquals("Checker at index 2 is null", exception.getMessage());
    }

    @Test
    void countsAllCheckersOfColorIncludingEmir() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_EMIR,
                TestChecker.WHITE_MAN));

        assertEquals(3, cell.count(StandardCheckerColor.WHITE));
        assertEquals(1, cell.count(StandardCheckerColor.BLACK));

        cell.pop();
        assertEquals(2, cell.count(StandardCheckerColor.WHITE));

        cell.setState(List.of(TestChecker.BLACK_MAN));
        assertEquals(0, cell.count(StandardCheckerColor.WHITE));
        assertEquals(1, cell.count(StandardCheckerColor.BLACK));
    }

    @Test
    void rejectsNullColor() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        assertThrows(NullPointerException.class, () -> cell.count(null));
    }

    @Test
    void traversesFromBottomToTopIncludingRepeatedCheckers() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        List<TestChecker> expected = List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN,
                TestChecker.WHITE_MAN,
                TestChecker.WHITE_EMIR);
        cell.setState(expected);

        List<TestChecker> visited = new ArrayList<>();
        cell.forEach(visited::add);

        assertEquals(expected, visited);
        assertEquals(expected, cell.getState());
    }

    @Test
    void doesNotInvokeActionForEmptyCell() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        cell.forEach(checker -> fail("Empty cell must not invoke the action"));
    }

    @Test
    void rejectsNullAction() {
        StandardCell<TestChecker> cell = new StandardCell<>();

        assertThrows(NullPointerException.class, () -> cell.forEach(null));
    }

    @Test
    void propagatesActionException() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.push(TestChecker.WHITE_MAN);
        IllegalStateException failure = new IllegalStateException("Action failed");

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> cell.forEach(checker -> {
                    throw failure;
                }));

        assertSame(failure, actual);
        assertEquals(List.of(TestChecker.WHITE_MAN), cell.getState());
    }

    @Test
    void clearsCellAndAllowsReuse() {
        StandardCell<TestChecker> cell = new StandardCell<>();
        cell.setState(List.of(
                TestChecker.WHITE_MAN,
                TestChecker.BLACK_MAN));

        cell.clear();
        cell.clear();

        assertTrue(cell.isEmpty());
        assertEquals(List.of(), cell.getState());
        assertEquals(0, cell.count(StandardCheckerColor.WHITE));
        assertEquals(0, cell.count(StandardCheckerColor.BLACK));

        cell.push(TestChecker.WHITE_EMIR);

        assertEquals(1, cell.size());
        assertSame(TestChecker.WHITE_EMIR, cell.peek());
    }
}
