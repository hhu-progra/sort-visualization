public final class Bar {

    private final int height;

    public Bar(int height) {
        this.height = height;
    }
    
    public String toString() {
        return "Height: " + height;
    }
    
    public double getSortKey() {
        return height;
    }

    public int getHeight() {
        return height;
    }
}
