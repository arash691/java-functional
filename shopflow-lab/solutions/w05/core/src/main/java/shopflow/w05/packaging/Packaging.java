package shopflow.w05.packaging;

public final class Packaging {
    private Packaging() {}

    public static Box resized(Box box, int width, int height, int depth) {
        return Boxes.of(width, height, depth);
    }
}
