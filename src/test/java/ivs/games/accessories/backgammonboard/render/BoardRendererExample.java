package ivs.games.accessories.backgammonboard.render;

import ivs.games.accessories.backgammonboard.standard.StandardBoard;
import ivs.games.accessories.backgammonboard.standard.StandardBoardBuilder;
import ivs.games.accessories.backgammonboard.standard.StandardChecker;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

import static ivs.games.accessories.backgammonboard.standard.StandardChecker.*;
import static ivs.games.accessories.backgammonboard.standard.StandardCheckerColor.*;

/**
 * Demonstrates rendering an arbitrary board arrangement.
 *
 * <p>The example includes mixed playing stacks, an expanded upper half,
 * occupied bars, and a two-digit borne-off checker count.
 * The arrangement does not represent a position validated by game rules.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoardRendererExample {

    /**
     * Creates and prints an example board.
     *
     * @param args unused command-line arguments
     */
    public static void main(String[] args) {
        StandardBoard<StandardChecker> board =
                new StandardBoardBuilder<StandardChecker>()
                        .cell(0, List.of(BLACK_MAN, BLACK_MAN))
                        .cell(5, List.of(WHITE_MAN, WHITE_EMIR))
                        .cell(7, List.of(BLACK_MAN, WHITE_MAN, BLACK_EMIR))
                        .cell(11, List.of(BLACK_MAN, BLACK_MAN, BLACK_MAN))
                        .cell(12, List.of(WHITE_MAN, WHITE_MAN))
                        .cell(18, List.of(BLACK_MAN, BLACK_EMIR))
                        .cell(23, Collections.nCopies(7, WHITE_MAN))
                        .bar(WHITE, List.of(WHITE_MAN, WHITE_EMIR))
                        .bar(BLACK, List.of(BLACK_MAN))
                        .borneOff(WHITE, List.of(WHITE_MAN, WHITE_MAN, WHITE_EMIR))
                        .borneOff(BLACK, Collections.nCopies(12, BLACK_MAN))
                        .build();

        StandardBoardRenderer renderer = new StandardBoardRenderer();

        System.out.println(renderer.render(board));
    }
}
