package shopflow.w05.packaging;

/** A parcel box with dimensions in centimetres. */
public class Box {
    protected int width;
    protected int height;
    protected int depth;

    public Box(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int volume() {
        return width * height * depth;
    }

    public boolean isCube() {
        return width == height && height == depth;
    }
}
