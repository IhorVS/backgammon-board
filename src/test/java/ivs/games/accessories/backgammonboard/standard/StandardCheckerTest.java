package ivs.games.accessories.backgammonboard.standard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests standard checker properties and lookup by code.
 */
@SuppressWarnings("DataFlowIssue")
class StandardCheckerTest {

    /**
     * Verifies the code, type, and color of each standard checker.
     */
    @Test
    void exposesCodeTypeAndColor() {
        assertChecker(StandardChecker.WHITE_MAN, "WM", StandardCheckerType.MAN, StandardCheckerColor.WHITE);
        assertChecker(StandardChecker.WHITE_EMIR, "WE", StandardCheckerType.EMIR, StandardCheckerColor.WHITE);
        assertChecker(StandardChecker.BLACK_MAN, "BM", StandardCheckerType.MAN, StandardCheckerColor.BLACK);
        assertChecker(StandardChecker.BLACK_EMIR, "BE", StandardCheckerType.EMIR, StandardCheckerColor.BLACK);
    }

    /**
     * Verifies that each supported code resolves to its checker constant.
     */
    @Test
    void resolvesCheckersByCode() {
        assertSame(StandardChecker.WHITE_MAN, StandardChecker.fromCode("WM"));
        assertSame(StandardChecker.WHITE_EMIR, StandardChecker.fromCode("WE"));
        assertSame(StandardChecker.BLACK_MAN, StandardChecker.fromCode("BM"));
        assertSame(StandardChecker.BLACK_EMIR, StandardChecker.fromCode("BE"));
    }

    /**
     * Verifies that unknown, incomplete, lowercase, and whitespace-padded
     * codes are rejected without normalization.
     *
     * @param code the invalid checker code
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "W", "M", "WX", "XM", "wm", "we", "bm", "be", "Wm", "wM", " WM", "WM "})
    void rejectsInvalidCodes(String code) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> StandardChecker.fromCode(code));

        assertEquals("Unknown checker code: " + code, exception.getMessage());
    }

    /**
     * Verifies that lookup rejects a null code.
     */
    @Test
    void rejectsNullCode() {
        assertThrows(NullPointerException.class, () -> StandardChecker.fromCode(null));
    }

    /**
     * Verifies the expected properties of a checker.
     *
     * @param checker the checker to inspect
     * @param code    the expected code
     * @param type    the expected type
     * @param color   the expected color
     */
    private static void assertChecker(StandardChecker checker, String code, StandardCheckerType type, StandardCheckerColor color) {

        assertAll(
                () -> assertEquals(code, checker.getCode()),
                () -> assertSame(type, checker.getType()),
                () -> assertSame(color, checker.getColor())
        );
    }
}
