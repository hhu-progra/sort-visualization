public class Sort {
    private static void insertionSort(Bar[] bars) {
        for(int currentIndex = 0; currentIndex < bars.length; currentIndex++) {
            Bar currentObject = bars[currentIndex];
            int insertionPosition = currentIndex;
            while(insertionPosition > 0 && bars[insertionPosition - 1].getSortKey() > currentObject.getSortKey()) {
                bars[insertionPosition] = bars[insertionPosition - 1];
                insertionPosition--;
            }
            bars[insertionPosition] = currentObject;
        }
    }
    
    private static void merge(Bar[] bars, int startIndex, int middleIndex, int endIndex) {
        int indexA = startIndex;
        int indexB = middleIndex;
        Bar[] merged = new Bar[endIndex - startIndex];
        int mergePosition = 0;
        
        while(indexA < middleIndex || indexB < endIndex) {
            if(indexA == middleIndex) {
                merged[mergePosition] = bars[indexB];
                indexB++;
            } else if(indexB == endIndex) {
                merged[mergePosition] = bars[indexA];
                indexA++;
            } else if(bars[indexB].getSortKey() <= bars[indexA].getSortKey()) {
                merged[mergePosition] = bars[indexB];
                indexB++;
            } else if(bars[indexA].getSortKey() <= bars[indexB].getSortKey()) {
                merged[mergePosition] = bars[indexA];
                indexA++;
            }
            mergePosition++;
        }
        
        for(int i = 0; i < merged.length; i++) {
            bars[startIndex + i] = merged[i];
        }
    }
    
    private static void mergeSort(Bar[] bars, int startIndex, int endIndex) {
        if (endIndex - startIndex <= 1) {
            return;
        }

        int middleIndex = startIndex + (endIndex - startIndex) / 2;
    
        mergeSort(bars, startIndex, middleIndex);
        mergeSort(bars, middleIndex, endIndex);
        
        merge(bars, startIndex, middleIndex, endIndex);
    }
    
    private static void mergeSort(Bar[] bars) {
        mergeSort(bars, 0, bars.length);
    }

    public static void main(String[] args) {
        Bar[] bars;
        if(args.length != 0) {
            bars = barHeightsFromStrings(args);
        } else {
            bars = randomBars(16);
        }

        printBars(bars);

//        insertionSort(bars);
        mergeSort(bars);

        printBars(bars);
    }

    private static void printBars(Bar[] bars) {
        for(Bar bar : bars) {
            System.out.println(bar);
        }
        System.out.println();
    }

    private static Bar[] barHeightsFromStrings(String[] heights) {
        Bar[] bars = new Bar[heights.length];
        for(int i = 0; i < heights.length; i++) {
            int height = Integer.parseInt(heights[i]);
            bars[i] = new Bar(height);
        }
        return bars;
    }
    
    private static Bar[] randomBars(int number) {
        Bar[] bars = new Bar[number];
        for(int i = 0; i < number; i++) {
            bars[i] = new Bar((int)(Math.random() * 200));
        }
        return bars;
    }

}
