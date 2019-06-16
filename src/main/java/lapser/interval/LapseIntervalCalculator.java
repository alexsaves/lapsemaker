package lapser.interval;

import lapser.LapseImgSrc;

import java.util.*;

/**
 * Tries to figure out the real interval between the images.
 * Tries to handle irregularities and missing images
 */
public class LapseIntervalCalculator {
    /**
     * The list of source images
     */
    private ArrayList<LapseImgSrc> srcImgs = new ArrayList<>();

    /**
     * Set up a new instance
     *
     * @param srcImgs
     */
    public LapseIntervalCalculator(ArrayList<LapseImgSrc> srcImgs) {
        this.srcImgs = srcImgs;
    }

    /**
     * Guess the interval
     * @param thresholdSeconds
     * @return
     * @throws RuntimeException
     */
    public Long computeApproximateInterval(long thresholdSeconds) throws RuntimeException {
        Map<Long, Integer> intervalFreq = new HashMap<>();
        Date lastDate = null;

        // First build a frequency table of intervals
        for (LapseImgSrc img : srcImgs) {
            if (lastDate == null) {
                lastDate = img.takenAt;
                continue;
            }
            // Get the diff rounded to the nearest second
            Long tdiff = (img.takenAt.getTime() - lastDate.getTime()) / 1000;

            intervalFreq.putIfAbsent(tdiff, 0);
            intervalFreq.put(tdiff, intervalFreq.get(tdiff) + 1);

            lastDate = img.takenAt;
        }

        // Now do nearest neighbor grouping with threshold if there is more than one
        while (intervalFreq.size() > 1 && nearestNeighborGroupWithThreshold(intervalFreq, thresholdSeconds)) {
        }

        // Get the maximum frequency item
        Map.Entry<Long, Integer> maxFreqItem = null;
        for (Map.Entry<Long, Integer> entry : intervalFreq.entrySet()) {
            if (maxFreqItem == null || entry.getValue() > maxFreqItem.getValue()) {
                maxFreqItem = entry;
            }
        }

        // Return the result
        return maxFreqItem.getKey();
    }


    /**
     * Group the set by intervals that are close together
     * @param intervalFreq
     * @param thresholdSeconds
     * @return
     */
    private Boolean nearestNeighborGroupWithThreshold(Map<Long, Integer> intervalFreq, long thresholdSeconds) {
        // Sort in ascending order
        List<Object> intervalArr = Arrays.asList((intervalFreq.keySet().toArray()));
        Comparator<Object> timeComparator = new Comparator<>() {
            public int compare(Object s1, Object s2) {
                return (int) ((Long) s1 - (Long) s2);
            }
        };
        intervalArr.sort(timeComparator);

        for (int i = 0; i < intervalArr.size() - 1; i++) {
            Long intrvl1 = (Long)intervalArr.get(i);
            for (int j = i + 1; j < intervalArr.size(); j++) {
                Long intrvl2 = (Long)intervalArr.get(j);
                Long tdiff = Math.abs(intrvl2 - intrvl1);
                if (tdiff <= thresholdSeconds) {
                    // Merge them!
                    // Take the midpoint
                    Long midpoint = (intrvl2 + intrvl1) / 2l;
                    int count1 = intervalFreq.get(intrvl1);
                    int count2 = intervalFreq.get(intrvl2);
                    intervalFreq.remove(intrvl1);
                    intervalFreq.remove(intrvl2);
                    intervalFreq.putIfAbsent(midpoint, 0);
                    intervalFreq.put(midpoint, intervalFreq.get(midpoint) + count1 + count2);
                    return true;
                }
            }
        }

        return false;
    }
}
