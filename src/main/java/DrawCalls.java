import static instrumentation.ArrayAccessAgent.log;
import static instrumentation.ArrayAccessMethodVisitor.getLocalVariableName;

// Note: must be in default package, because student code (which includes the Bar class) is in default package
public class DrawCalls {
    private static final DrawCanvas canvas = new DrawCanvas();

    /**
     * If the given object is an array of Bars, add it to the canvas.
     */
    static void newBarsArray(Object o, String methodAndVariableIndex) {
        String qualifiedVariableName = getLocalVariableName(methodAndVariableIndex);
        log("candidate for announcement: " + qualifiedVariableName);
        if(!(o instanceof Bar[] bars)) {
            log("not announcing " + o.getClass());
            return;
        }
        log("announcing bars " + bars.getClass() + " of length " + bars.length + " with name " + qualifiedVariableName + " @" + System.identityHashCode(bars));
        canvas.addArray(bars, qualifiedVariableName);
    }

    /**
     * If the given object is an array of Bars, redraw the canvas and indicate a read access at the given index.
     */
    static void drawBarsRead(Object o, int readIndex) {
        drawBars(o, readIndex, -1);
    }

    /**
     * If the given object is an array of Bars, redraw the canvas and indicate a write access at the given index.
     */
    static void drawBarsWrite(Object o, int writeIndex) {
        drawBars(o, -1, writeIndex);
    }

    private static void drawBars(Object o, int readIndex, int writeIndex) {
        if(!(o instanceof Bar[] bars)) {
            log("not drawing " + o.getClass());
            return;
        }
        log("drawing bars " + bars.getClass() + " of length " + bars.length + " @" + System.identityHashCode(bars));
        canvas.draw(bars, readIndex, writeIndex);
    }
}
