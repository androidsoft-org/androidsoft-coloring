package eu.quelltext.images;

public abstract class ColorComparator {

    public static ColorComparator unequal(final int color1, final int color2) {
        return new ColorComparator() {
            @Override
            boolean equals(int color) {
                return color != color1 && color != color2;
            }
        };
    }

    public static ColorComparator unequal(final int color) {
        return new ColorComparator() {
            @Override
            boolean equals(int c) {
                return color != c;
            }
        };
    }

    abstract boolean equals(int color);
}
