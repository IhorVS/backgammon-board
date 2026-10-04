package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.CheckerType;

enum TestType implements CheckerType {
    MAN("M"),
    EMIR("E");

    private final String code;

    TestType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
