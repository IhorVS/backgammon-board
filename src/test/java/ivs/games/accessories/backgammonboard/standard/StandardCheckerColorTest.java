package ivs.games.accessories.backgammonboard.standard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardCheckerColorTest {

    @Test
    void returnsCodes() {
        assertEquals("W", StandardCheckerColor.WHITE.getCode());
        assertEquals("B", StandardCheckerColor.BLACK.getCode());
    }

    @Test
    void resolvesColorsByCode() {
        assertSame(StandardCheckerColor.WHITE, StandardCheckerColor.fromCode("W"));
        assertSame(StandardCheckerColor.BLACK, StandardCheckerColor.fromCode("B"));
    }

    @Test
    void rejectsUnknownCodes() {
        for (String code : new String[]{"w", "b", "", "X", " W"}) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> StandardCheckerColor.fromCode(code));

            assertEquals("Unknown checker color code: " + code,
                    exception.getMessage());
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void rejectsNullCode() {
        assertThrows(NullPointerException.class,
                () -> StandardCheckerColor.fromCode(null));
    }

    @Test
    void returnsOppositeColor() {
        assertSame(StandardCheckerColor.BLACK,
                StandardCheckerColor.getOpposite(StandardCheckerColor.WHITE));
        assertSame(StandardCheckerColor.WHITE,
                StandardCheckerColor.getOpposite(StandardCheckerColor.BLACK));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void rejectsNullColor() {
        assertThrows(NullPointerException.class,
                () -> StandardCheckerColor.getOpposite(null));
    }
}
