package shopflow.w05.packaging;

public final class Packaging {
    private Packaging() {}

    /** Returns the box resized to the requested dimensions (used to price shipping by volume). */
    public static Box resized(Box box, int width, int height, int depth) {
        box.setWidth(width);
        box.setHeight(height);
        box.setDepth(depth);
        return box;
    }
}
