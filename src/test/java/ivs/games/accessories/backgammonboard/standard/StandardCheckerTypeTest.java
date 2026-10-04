package ivs.games.accessories.backgammonboard.standard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StandardCheckerTypeTest {

    @Test
    void returnsCodes() {
        assertEquals("M", StandardCheckerType.MAN.getCode());
        assertEquals("E", StandardCheckerType.EMIR.getCode());
    }

    @Test
    void resolvesTypesByCode() {
        assertSame(StandardCheckerType.MAN, StandardCheckerType.fromCode("M"));
        assertSame(StandardCheckerType.EMIR, StandardCheckerType.fromCode("E"));
    }

    @Test
    void rejectsUnknownCodes() {
        for (String code : new String[]{"m", "e", "", "X", " M"}) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> StandardCheckerType.fromCode(code));

            assertEquals("Unknown checker type code: " + code,
                    exception.getMessage());
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void rejectsNullCode() {
        assertThrows(NullPointerException.class,
                () -> StandardCheckerType.fromCode(null));
    }
}
