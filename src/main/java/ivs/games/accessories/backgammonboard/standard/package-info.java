/**
 * Standard backgammon checkers, stack cells, boards, and board builders.
 *
 * <p>The standard set contains white and black ordinary and emir checker
 * constants. Codes are case-sensitive: WM, WE, BM, and BE. Color and type
 * codes identify server-side values; user interfaces may choose their own visuals.</p>
 *
 * <p>Standard boards contain 24 playing cells indexed from 0 to 23 and separate
 * bar and borne-off cells for each standard color. Playing cells accept mixed
 * colors and types. Colored cells require every checker to match their assigned
 * color. The board does not enforce game rules or checker inventory limits.</p>
 *
 * <p>Board copying and restoration create independent cells and preserve stack
 * order. Immutable checker objects are shared. Cell references remain valid
 * when a board is cleared.</p>
 *
 * <p>The builder stores copied checker lists. Each build creates a new board
 * without resetting the builder. Null values are rejected during configuration;
 * playing indices and special cell color restrictions are validated during build.</p>
 *
 * <p>Mutable boards, cells, and builders are not thread-safe.</p>
 */
package ivs.games.accessories.backgammonboard.standard;
