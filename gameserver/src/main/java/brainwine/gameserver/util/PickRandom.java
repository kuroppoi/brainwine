package brainwine.gameserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PickRandom {

    /**Draw sampleSize random items from the given array.
     * 
     * @param <T> type of elements
     * @param arr the array
     * @param sampleSize the number of items to return
     * @returns a new array
     */
    public static <T> List<T> sampleWithoutReplacement(List<T> arr, int sampleSize) {
        return sampleWithoutReplacement(ThreadLocalRandom.current(), arr, sampleSize);
    }

    /**Draw sampleSize random items from the given array and the random number generator.
     * 
     * @param <T> type of elements
     * @param random Random instance to roll dice with
     * @param arr the array
     * @param sampleSize the number of items to return
     * @returns a new array
     */
    public static <T> List<T> sampleWithoutReplacement(Random random, List<T> arr, int sampleSize) {
        if(arr.size() <= sampleSize) {
            return new ArrayList<>(arr);
        }

        int selected = 0;
        int dealtWith = 0;

        List<T> result = new ArrayList<>(sampleSize);

        while(selected < sampleSize)
        {
            double uniform = random.nextDouble();

            if(uniform * (arr.size() - dealtWith) >= sampleSize - selected) {
                dealtWith++;
            } else {
                result.add(arr.get(dealtWith));
                selected++;
                dealtWith++;
            }
        }

        return result;
    }

}
