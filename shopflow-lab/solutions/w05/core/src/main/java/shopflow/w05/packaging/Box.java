package shopflow.w05.packaging;

/** An immutable parcel box with dimensions in centimetres. */
public final class Box {
    private final int width;
    private final int height;
    private final int depth;

    Box(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public int volume() {
        return width * height * depth;
    }

    public boolean isCube() {
        return width == height && height == depth;
    }
}
