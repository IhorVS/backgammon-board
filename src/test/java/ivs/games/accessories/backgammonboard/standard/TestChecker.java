package ivs.games.accessories.backgammonboard.standard;

import ivs.games.accessories.backgammonboard.Checker;

enum TestChecker implements Checker<TestType, StandardCheckerColor> {
    WHITE_MAN(TestType.MAN, StandardCheckerColor.WHITE),
    WHITE_EMIR(TestType.EMIR, StandardCheckerColor.WHITE),
    BLACK_MAN(TestType.MAN, StandardCheckerColor.BLACK),
    BLACK_EMIR(TestType.EMIR, StandardCheckerColor.BLACK);

    private final TestType type;
    private final StandardCheckerColor color;

    TestChecker(TestType type, StandardCheckerColor color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public String getCode() {
        return color.getCode() + type.getCode();
    }

    @Override
    public TestType getType() {
        return type;
    }

    @Override
    public StandardCheckerColor getColor() {
        return color;
    }
}
