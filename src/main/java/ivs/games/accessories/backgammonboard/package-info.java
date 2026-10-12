/**
 * Backgammon accessory contracts, immutable state snapshots, and side-relative addressing.
 *
 * <p>{@link ivs.games.accessories.backgammonboard.Checker} describes immutable
 * checkers through their code, type, and color. Constants may represent multiple
 * identical physical checkers; individual checker identity is not required.</p>
 *
 * <p>Cells contain stacks ordered from bottom to top. The last element is the
 * top checker. Cell views expose read operations, while mutable cells also allow
 * changes. Board views provide access to playing, bar, and borne-off cells.</p>
 *
 * <p>Read-only interfaces expose live objects, not immutable snapshots.
 * {@link ivs.games.accessories.backgammonboard.BoardState} copies all collections
 * and provides an immutable snapshot, sharing immutable checker and color objects.
 * State subclasses must preserve immutability.</p>
 *
 * <p>{@link ivs.games.accessories.backgammonboard.BoardAddressing} translates
 * side-relative route points 1 to 24 into absolute playing cell indices 0 to 23
 * and back. Games supply two distinct immutable side identifiers and a complete
 * bidirectional mapping for each side. Mappings are copied into immutable maps;
 * arbitrary permutations and identical mappings for different sides are supported.
 * Bar and borne-off cells are excluded. Addressing does not change board contents
 * or define movement rules.</p>
 *
 * <p>Game engines define legal moves, directions, initial arrangements, and
 * victory conditions. Board indices are technical addresses rather than
 * player-relative point numbers. Implementations define board dimensions
 * and supported colors.</p>
 */
package ivs.games.accessories.backgammonboard;
