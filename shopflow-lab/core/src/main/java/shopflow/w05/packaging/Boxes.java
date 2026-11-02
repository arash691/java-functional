package shopflow.w05.packaging;

/** Factory for boxes. A cube is "just a special box", so it is modelled as a subclass. */
public final class Boxes {
    private Boxes() {}

    public static Box of(int width, int height, int depth) {
        return new Box(width, height, depth);
    }

    public static Box cube(int side) {
        return new Cube(side);
    }

    /** A cube keeps all sides equal, so every setter updates all three dimensions. */
    static final class Cube extends Box {
        Cube(int side) {
            super(side, side, side);
        }

        @Override
        public void setWidth(int side) {
            super.setWidth(side);
            super.setHeight(side);
            super.setDepth(side);
        }

        @Override
        public void setHeight(int side) {
            setWidth(side);
        }

        @Override
        public void setDepth(int side) {
            setWidth(side);
        }
    }
}
