import java.awt.*;
import java.util.*;
import java.util.List;

import static instrumentation.ArrayAccessAgent.log;

/**
 * Canvas for drawing one big array and several smaller arrays
 * Note: must be in default package, because student code (which includes the Bar class) is in default package
 * Why using StdDraw? Because our students know that from the lecture. An older version of this code exposed which drawing
 * library is used. Now it's completely hidden, so actually we could use something simpler now ...
 */
public final class DrawCanvas {
    private record BarArrayReferences(Bar[] bars, String name) {}

    private final List<BarArrayReferences> bars = new ArrayList<>();
    private boolean paused = false;

    private static final double MIN_WIDTH_PERCENT = 0.25;
    private static final int BIG_ARRAY_CANVAS_SIZE = 800;
    private static final int NUM_SMALL_ARRAYS_X = fromEnv("NUM_SMALL_ARRAYS_X", 2);
    private static final int NUM_SMALL_ARRAYS_Y = fromEnv("NUM_SMALL_ARRAYS_Y", 2);
    private static final int SMALL_ARRAY_CANVAS_SIZE = BIG_ARRAY_CANVAS_SIZE / NUM_SMALL_ARRAYS_Y;
    private static final int NUM_SMALL_ARRAYS = NUM_SMALL_ARRAYS_X * NUM_SMALL_ARRAYS_Y;
    private static final int CANVAS_WIDTH = BIG_ARRAY_CANVAS_SIZE + NUM_SMALL_ARRAYS_X * SMALL_ARRAY_CANVAS_SIZE;
    private static final int CANVAS_HEIGHT = Math.max(BIG_ARRAY_CANVAS_SIZE, NUM_SMALL_ARRAYS_Y * SMALL_ARRAY_CANVAS_SIZE);
    private static final int BIG_ARRAY_RW_LABEL_HEIGHT = 20;
    private static final int SMALL_ARRAY_RW_LABEL = 10;
    private static final int DRAW_PAUSE_LENGTH = fromEnv("DRAW_PAUSE_LENGTH", 500);
    private static final int CLEAR_TRANSPARENCY = 175;

    private static int fromEnv(String name, int defaultValue) {
        String envValue = System.getenv(name);
        if (envValue != null) {
            try {
                return Integer.parseInt(envValue);
            } catch (NumberFormatException e) {
                log("Invalid value for " + name + ": " + envValue + ", using default value " + defaultValue);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public DrawCanvas() {
        StdDraw.setCanvasSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        StdDraw.setXscale(0, CANVAS_WIDTH);
        StdDraw.setYscale(0, CANVAS_HEIGHT);
    }

    public void addArray(Bar[] bars, String name) {
        if(!barIsKnow(bars)) {
            this.bars.add(new BarArrayReferences(bars, name));
        }
    }

    public void draw(Bar[] bars, int readIndex, int writeIndex) {
        if(!barIsKnow(bars)) {
            this.bars.add(new BarArrayReferences(bars, "<unkown>"));
        }

        if(StdDraw.hasNextKeyTyped()) {
            if (StdDraw.nextKeyTyped() == 'p') {
                paused = !paused;
                log("paused: " + paused);
            }
        }

        if(paused) {
            pause();
        }

        redraw(bars, readIndex, writeIndex);

        StdDraw.show(DRAW_PAUSE_LENGTH);
    }

    private void pause() {
        while(!StdDraw.hasNextKeyTyped()) {
            StdDraw.show(50);
        }

        switch (StdDraw.nextKeyTyped()) {
            case 'p' -> {
                paused = false;
                log("unpaused");
            }
            case 's' -> log("step");
            default -> pause();
        }
    }

    private boolean barIsKnow(Bar[] otherBars) {
        return bars.stream().anyMatch(barRef -> barRef.bars == otherBars);
    }

    private BarArrayReferences biggestBarArray() {
        return bars.stream().max(Comparator.comparingInt(a -> a.bars.length)).orElseThrow();
    }

    private List<BarArrayReferences> smallerBarArrays() {
        return bars.stream().filter(a -> a != biggestBarArray()).toList();
    }

    public void redraw(Bar[] arrayWithOperation, int readIndex, int writeIndex) {
        if(readIndex > -1 && writeIndex > -1) {
            throw new IllegalArgumentException("readIndex and writeIndex cannot be set at the same time");
        }

        clearCanvas();

        BarArrayReferences biggestArray = biggestBarArray();
        drawSingleArray(biggestArray, arrayWithOperation, readIndex, writeIndex, BIG_ARRAY_CANVAS_SIZE, BIG_ARRAY_RW_LABEL_HEIGHT, 0, 0);

        for(int i = Math.max(0, smallerBarArrays().size() - NUM_SMALL_ARRAYS); i < smallerBarArrays().size(); i++) {
            BarArrayReferences currentArray = smallerBarArrays().get(i);
            int x = i % NUM_SMALL_ARRAYS_X;
            int y = (i % NUM_SMALL_ARRAYS) / NUM_SMALL_ARRAYS_X;
            drawSingleArray(currentArray, arrayWithOperation, readIndex, writeIndex, SMALL_ARRAY_CANVAS_SIZE, SMALL_ARRAY_RW_LABEL, BIG_ARRAY_CANVAS_SIZE + x * SMALL_ARRAY_CANVAS_SIZE, y * SMALL_ARRAY_CANVAS_SIZE);
        }
    }

    private static void clearCanvas() {
        StdDraw.setPenColor(new Color(255,255,255, CLEAR_TRANSPARENCY)); // we want to see the old state shining through
        StdDraw.filledRectangle(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2,CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2);
    }

    private void drawSingleArray(BarArrayReferences arrayRef, Bar[] arrayWithOperation, int readIndex, int writeIndex, int canvasSize, double rwLabelHeight, int xOffset, int yOffset) {
        drawBorder(canvasSize, xOffset, yOffset);
        drawName(arrayRef, canvasSize, rwLabelHeight, xOffset, yOffset);

        Bar[] array = arrayRef.bars;
        double widthPerBar = (double) canvasSize / getMaxLengthOfAllArrays();
        double maxHeight = getMaxHeightInAllArrays();
        for (int i = 0; i < array.length; i++) {
            Bar currentBar = array[i];
            if(currentBar == null) {
                continue;
            }
            double xPosition = i * widthPerBar + widthPerBar / 2 + xOffset;
            double drawnHeight = currentBar.getHeight() / maxHeight * (canvasSize - rwLabelHeight);
            drawBar(rwLabelHeight, yOffset, currentBar, xPosition, drawnHeight, maxHeight, widthPerBar);

            if(array == arrayWithOperation && i == readIndex) {
                drawReadIndicator(rwLabelHeight, yOffset, xPosition, widthPerBar);
            }
            if(array == arrayWithOperation && i == writeIndex) {
                drawWriteIndicator(rwLabelHeight, yOffset, xPosition, widthPerBar);
            }
        }
    }

    private static void drawBar(double rwLabelHeight, int yOffset, Bar currentBar, double xPosition, double drawnHeight, double maxHeight, double widthPerBar) {
        StdDraw.setPenColor(rainbowColor(currentBar.getHeight()));
        StdDraw.filledRectangle(xPosition, drawnHeight / 2 + rwLabelHeight + yOffset, widthFactor(maxHeight, currentBar.getHeight()) * widthPerBar / 2., drawnHeight / 2);
    }

    private static void drawWriteIndicator(double rwLabelHeight, int yOffset, double xPosition, double widthPerBar) {
        StdDraw.setPenColor(new Color(246, 93, 93,175));
        StdDraw.filledRectangle(xPosition, rwLabelHeight / 2 + yOffset, widthPerBar / 2, rwLabelHeight / 2);
        StdDraw.setPenColor(new Color(255, 255, 255));
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, (int)(0.8 * rwLabelHeight)));
        StdDraw.text(xPosition, rwLabelHeight / 2 + yOffset, "WRITE");
    }

    private static void drawReadIndicator(double rwLabelHeight, int yOffset, double xPosition, double widthPerBar) {
        StdDraw.setPenColor(new Color(48, 114, 229,175));
        StdDraw.filledRectangle(xPosition, rwLabelHeight / 2 + yOffset, widthPerBar / 2, rwLabelHeight / 2);
        StdDraw.setPenColor(new Color(255, 255, 255));
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, (int)(0.8 * rwLabelHeight)));
        StdDraw.text(xPosition, rwLabelHeight / 2 + yOffset, "READ");
    }

    private static void drawName(BarArrayReferences arrayRef, int canvasSize, double rwLabelHeight, int xOffset, int yOffset) {
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, (int)(1.8 * rwLabelHeight)));
        StdDraw.text(xOffset + canvasSize / 2, canvasSize - 2 * rwLabelHeight + yOffset, arrayRef.name);
    }

    private static void drawBorder(int canvasSize, int xOffset, int yOffset) {
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.rectangle(canvasSize / 2 + xOffset, canvasSize / 2 + yOffset, canvasSize / 2, canvasSize / 2);
    }

    private int getMaxLengthOfAllArrays() {
        return bars.stream().map(BarArrayReferences::bars).mapToInt(b -> b.length).max().orElse(1);
    }

    private int getMaxHeightInAllArrays() {
        return bars.stream()
                .map(BarArrayReferences::bars)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .mapToInt(Bar::getHeight)
                .max()
                .orElse(1);
    }

    private static java.awt.Color rainbowColor(int value) {
        float hue = ((2 * value) % 100) / 100f;
        float saturation = 0.8f / (value / 50 + 1) + 0.2f;
        return java.awt.Color.getHSBColor(hue * 0.8f, saturation, 0.8f);
    }

    private static double widthFactor(double maxHeight, int height) {
        // we want to make small bars wider than big bars so that you can see when a smaller is bar is overwritten
        return 1 - (1 - MIN_WIDTH_PERCENT) * height / maxHeight;
    }

}
