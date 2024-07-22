package net.skykingdoms.backend.base.util.random;

import com.google.common.collect.Lists;
import lombok.NonNull;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RandomUtil {

    public static final Random R = new Random();

    private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(
            Collectors.toCollection(ArrayList::new),

            list -> {
                Collections.shuffle(list);

                return list;
            }
    );

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> toShuffledList() {
        return (Collector<T, ?, List<T>>) SHUFFLER;
    }

    private static <T> Collection<T> getRandomElements(@NonNull List<T> from, int n, boolean unique) {
        List<T> list = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            int elementIndex = R.nextInt(from.size());
            T element = unique ? from.remove(elementIndex) : from.get(elementIndex);

            list.add(element);
        }

        return list;
    }

    public static <T> Collection<T> getRandomElements(@NonNull T[] array, int n, boolean unique) {
        return getRandomElements(Lists.newArrayList(array), n, unique);
    }

    public static <T> Collection<T> getRandomElements(@NonNull Collection<T> collection, int n, boolean unique) {
        return getRandomElements(new ArrayList<>(collection), n, unique);
    }

    public static <T> T getRandomElement(@NonNull List<T> list) {
        return list.isEmpty() ? null : list.get(R.nextInt(list.size()));
    }

    public static <T> T removeRandomElement(@NonNull List<T> list) {
        return list.isEmpty() ? null : list.remove(R.nextInt(list.size()));
    }

    @SafeVarargs
    public static <T> T getRandomElement(@NonNull T... array) {
        if (array.length == 0) {
            return null;
        }

        return array[R.nextInt(array.length)];
    }

    public static <T> T weightedRandom(@NonNull Collection<T> items, @NonNull ToDoubleFunction<T> weightCalculator) {
        double sum = 0.0;

        for (T item : items) {
            sum += weightCalculator.applyAsDouble(item);
        }

        double rand = R.nextDouble() * sum;
        double from = 0.0;

        T last = null;

        for (T element : items) {
            double d2 = weightCalculator.applyAsDouble(element);

            if (rand >= from && rand < from + d2) {
                return element;
            }

            last = element;
            from += d2;
        }

        return last;
    }

    public static <T> T weightedRandom(@NonNull Map<T, Double> weights) {
        return weightedRandom(weights.keySet(), weights::get);
    }
}
