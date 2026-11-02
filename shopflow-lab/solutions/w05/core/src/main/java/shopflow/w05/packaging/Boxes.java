package shopflow.w05.packaging;

public final class Boxes {
    private Boxes() {}

    public static Box of(int width, int height, int depth) {
        return new Box(width, height, depth);
    }

    /** A cube is just a box whose sides happen to be equal; no subclass needed. */
    public static Box cube(int side) {
        return of(side, side, side);
    }
}
