package shopflow.w04;

/**
 * Worked example for Week 4, session 1. Already works.
 * Notice: private final fields, validation in the constructor, no setters,
 * and a "change" method that returns a NEW object. That is an immutable class.
 */
public class Rectangle {

    private final int width;
    private final int height;

    public Rectangle(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int area() {
        return width * height;
    }

    public Rectangle scaledBy(int factor) {
        return new Rectangle(width * factor, height * factor);
    }

    @Override
    public String toString() {
        return "Rectangle[" + width + "x" + height + "]";
    }
}
