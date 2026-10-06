/**
 * Plain-text rendering of standard backgammon boards for logs and consoles.
 *
 * <p>The renderer captures a board state and displays technical indices 12 to 23
 * across the upper row and 11 to 0 across the lower row. Stack bottoms are at the
 * outer board edges; stack tops face the center.</p>
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
