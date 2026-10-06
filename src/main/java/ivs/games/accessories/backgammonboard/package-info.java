/**
 * Backgammon accessory contracts and immutable board state snapshots.
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
 * <p>Game engines define legal moves, directions, initial arrangements, and
 * victory conditions. Board indices are technical addresses rather than
 * player-relative point numbers. Implementations define board dimensions
 * and supported colors.</p>
 */
package ivs.games.accessories.backgammonboard;
