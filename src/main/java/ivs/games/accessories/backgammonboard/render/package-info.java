/**
 * Plain-text rendering of standard backgammon boards for logs and consoles.
 *
 * <p>The renderer captures a board state and displays technical indices 23 to 12
 * across the upper row and 0 to 11 across the lower row, both from left to right.
 * Index 0 is at the lower-left corner. Indices increase counterclockwise along
 * the lower edge to the right, then along the upper edge to the left.
 * This display orientation does not determine checker movement directions.
 * The renderer displays absolute indices and does not apply
 * {@link ivs.games.accessories.backgammonboard.BoardAddressing} mappings.</p>
 *
 * <p>Stack bottoms are at the outer board edges; stack tops face the center.</p>
 *
 * <p>Each half has at least five checker rows and expands independently to fit
 * its tallest stack. One blank board row separates the halves. Empty positions
 * are shown as two dots. Special cells show complete bottom-to-top checker lists
 * and counts with a minimum field width of two characters.</p>
 *
 * <p>Checker codes must be non-empty and contain no whitespace. Alignment assumes
 * one console column per code character. Output uses LF line separators without
 * a trailing separator. The source board must remain unchanged during capture.</p>
 */
package ivs.games.accessories.backgammonboard.render;
