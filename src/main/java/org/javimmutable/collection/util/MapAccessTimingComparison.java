package org.javimmutable.collection.util;

import org.javimmutable.collection.IBuilders;
import org.javimmutable.collection.IMap;
import org.javimmutable.collection.IMapBuilder;
import org.javimmutable.collection.Temp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * More realistic map timing comparisons.  Builds the map and then
 * accesses it rather than performing a lot of mutations.
 */
public class MapAccessTimingComparison
{
    private MapAccessTimingComparison()
    {
    }

    public static void main(String[] argv)
        throws Exception
    {
        if (argv.length != 2) {
            System.err.printf("usage: %s seed loops", MapAccessTimingComparison.class.getSimpleName());
            System.exit(1);
        }

        final int seed = Integer.parseInt(argv[0]);
        final int loops = Integer.parseInt(argv[1]);

        List<Integer> javaHashTimes = new ArrayList<>();
        List<Integer> javaTreeTimes = new ArrayList<>();
        List<Integer> hashTimes = new ArrayList<>();
        List<Integer> treeTimes = new ArrayList<>();
        List<Integer> arrayTimes = new ArrayList<>();
        List<Integer> inOrderTimes = new ArrayList<>();
        System.out.println("warm up runs");

        final int maxValue = 10 * loops;
        final int maxKey = 100000000;
        final int maxCommand = 10;
        runLoop(seed, loops, maxValue, maxKey, maxCommand, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        runLoop(seed, loops, maxValue, maxKey, maxCommand, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        runLoop(seed, loops, maxValue, maxKey, maxCommand, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        runLoop(seed, loops, maxValue, maxKey, maxCommand, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        System.out.println();

        System.out.println("real runs");
        for (int i = 0; i < 25; ++i) {
            runLoop(seed + i, loops, maxValue, maxKey, maxCommand, javaHashTimes, javaTreeTimes, hashTimes, treeTimes, arrayTimes, inOrderTimes);
            System.out.println();
        }
        System.out.printf("jhash avg: %.1f  jtree avg: %.1f  tree avg: %.1f  hash avg: %.1f  array avg: %.1f  order avg: %.1f%n",
                          avg(javaHashTimes), avg(javaTreeTimes), avg(treeTimes), avg(hashTimes), avg(arrayTimes), avg(inOrderTimes));
    }

    private static double avg(List<Integer> times)
    {
        times.sort(Comparator.naturalOrder());
        int tenPercent = (times.size() + 9) / 10;
        int limit = times.size() - tenPercent;
        double total = 0.0;
        for (int i = 0; i < limit; ++i) {
            total += times.get(i);
        }
        return total / (double)limit;
    }

    @SuppressWarnings("UnusedAssignment")
    private static void runLoop(int seed,
                                int loops,
                                int maxValue,
                                int maxKey,
                                int maxCommand,
                                List<Integer> javaHashElapsed,
                                List<Integer> javaTreeElapsed,
                                List<Integer> hashElapsed,
                                List<Integer> treeElapsed,
                                List<Integer> arrayElapsed,
                                List<Integer> inOrderElapsed)
        throws Exception
    {
        javaMapLoop(seed, loops, maxValue, maxKey, "jhash", javaHashElapsed, java.util.HashMap::new);
        javaMapLoop(seed, loops, maxValue, maxKey, "jtree", javaTreeElapsed, java.util.TreeMap::new);
        iMapLoop(seed, loops, maxValue, maxKey, "hash", hashElapsed, IBuilders::map);
        iMapLoop(seed, loops, maxValue, maxKey, "tree", treeElapsed, IBuilders::sortedMap);
        iMapLoop(seed, loops, maxValue, maxKey, "order", inOrderElapsed, IBuilders::orderedMap);
    }

    private static void javaMapLoop(int seed,
                                    int loops,
                                    int maxValue,
                                    int maxKey,
                                    String label,
                                    List<Integer> elapsed,
                                    Supplier<Map<Integer, Integer>> factory)
        throws Exception
    {
        Random random = new Random(seed);
        long startMillis = System.currentTimeMillis();

        // build phase
        Map<Integer, Integer> map = factory.get();
        for (int i = 1; i <= loops; ++i) {
            Integer key = random.nextInt(maxKey);
            Integer value = random.nextInt(maxValue);
            map.put(key, value);
        }

        Temp.Int1 gets = Temp.intVar(0);

        // query phase
        for (int i = 1; i <= loops; ++i) {
            Integer key = random.nextInt(maxKey);
            map.get(key);
            gets.a += 1;
        }

        // stream phase
//        map.entrySet().parallelStream().forEach(e -> gets.a += 1);

        long endMillis = System.currentTimeMillis();
        int elapsedMillis = (int)(endMillis - startMillis);
        elapsed.add(elapsedMillis);
        System.out.printf("%s adds %d gets %d size %d elapsed %d%n", label, loops, gets.a, map.size(), elapsedMillis);
        System.gc();
        Thread.sleep(500);
    }

    private static void iMapLoop(int seed,
                                 int loops,
                                 int maxValue,
                                 int maxKey,
                                 String label,
                                 List<Integer> elapsed,
                                 Supplier<IMapBuilder<Integer, Integer>> factory)
        throws Exception
    {
        Random random = new Random(seed);
        long startMillis = System.currentTimeMillis();

        // build phase
        IMapBuilder<Integer, Integer> builder = factory.get();
        for (int i = 1; i <= loops; ++i) {
            Integer key = random.nextInt(maxKey);
            Integer value = random.nextInt(maxValue);
            builder.add(key, value);
        }
        IMap<Integer, Integer> map = builder.build();

        Temp.Int1 gets = Temp.intVar(0);

        // query phase
        for (int i = 1; i <= loops; ++i) {
            Integer key = random.nextInt(maxKey);
            map.get(key);
            gets.a += 1;
        }

        // stream phase
//        map.parallelStream().forEach(e -> gets.a += 1);

        long endMillis = System.currentTimeMillis();
        int elapsedMillis = (int)(endMillis - startMillis);
        elapsed.add(elapsedMillis);
        System.out.printf("%s adds %d gets %d size %d elapsed %d%n", label, loops, gets.a, map.size(), elapsedMillis);
        System.gc();
        Thread.sleep(500);
    }
}
