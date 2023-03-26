///###////////////////////////////////////////////////////////////////////////
//
// Burton Computer Corporation
// http://www.burton-computer.com
//
// Copyright (c) 2021, Burton Computer Corporation
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//     Redistributions of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
//
//     Redistributions in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
//
//     Neither the name of the Burton Computer Corporation nor the names
//     of its contributors may be used to endorse or promote products
//     derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package org.javimmutable.collections.stress_test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.javimmutable.collections.IArrays;
import org.javimmutable.collections.ICollectors;
import org.javimmutable.collections.IList;
import org.javimmutable.collections.IListMap;
import org.javimmutable.collections.ILists;
import org.javimmutable.collections.IMapEntry;
import org.javimmutable.collections.IMaps;
import org.javimmutable.collections.IMultisets;
import org.javimmutable.collections.ISet;
import org.javimmutable.collections.ISetMap;
import org.javimmutable.collections.ISetMaps;
import org.javimmutable.collections.ISets;
import org.javimmutable.collections.hash.JImmutableHashMap;
import org.javimmutable.collections.setmap.JImmutableSetMapFactory;
import org.javimmutable.collections.stress_test.KeyFactory.BadHashKeyFactory;
import org.javimmutable.collections.stress_test.KeyFactory.ComparableBadHashKeyFactory;
import org.javimmutable.collections.stress_test.KeyFactory.ComparableRegularKeyFactory;
import org.javimmutable.collections.stress_test.KeyFactory.RegularKeyFactory;

/**
 * Test program to run an infinite loop feeding data to every implementation of every
 * JImmutable collection type, querying the data, and deleting the data to verify
 * the collection always contains what it should.
 */
public class RunStressTests
{
    private static final IList<StressTester> AllTesters = ILists.<StressTester>of()
        .insert(new JImmutableListStressTester(ILists.of(), ICollectors.toList()))

        .insert(new JImmutableSetStressTester(ISets.hashed(), HashSet.class, IterationOrder.UNORDERED))
        .insert(new JImmutableSetStressTester(ISets.ordered(), LinkedHashSet.class, IterationOrder.INSERT_ORDER))
        .insert(new JImmutableSetStressTester(ISets.sorted(), TreeSet.class, IterationOrder.ORDERED))
        .insert(new JImmutableSetStressTester(IMultisets.hashed(), HashSet.class, IterationOrder.UNORDERED))
        .insert(new JImmutableSetStressTester(IMultisets.ordered(), LinkedHashSet.class, IterationOrder.INSERT_ORDER))
        .insert(new JImmutableSetStressTester(IMultisets.sorted(), TreeSet.class, IterationOrder.ORDERED))

        .insert(new JImmutableMultisetStressTester(IMultisets.hashed()))
        .insert(new JImmutableMultisetStressTester(IMultisets.ordered()))
        .insert(new JImmutableMultisetStressTester(IMultisets.sorted()))

        .insert(new JImmutableMapStressTester<>(JImmutableHashMap.usingList(), HashMap.class, new RegularKeyFactory()))
        .insert(new JImmutableMapStressTester<>(JImmutableHashMap.usingTree(), HashMap.class, new ComparableRegularKeyFactory()))
        .insert(new JImmutableMapStressTester<>(JImmutableHashMap.usingList(), HashMap.class, new BadHashKeyFactory()))
        .insert(new JImmutableMapStressTester<>(JImmutableHashMap.usingTree(), HashMap.class, new ComparableBadHashKeyFactory()))

        .insert(new JImmutableMapStressTester<>(IMaps.ordered(), LinkedHashMap.class, new ComparableRegularKeyFactory()))
        .insert(new JImmutableMapStressTester<>(IMaps.sorted(), TreeMap.class, new ComparableRegularKeyFactory()))

        .insert(new JImmutableSetMapStressTester(ISetMaps.hashed(), HashMap.class))
        .insert(new JImmutableSetMapStressTester(ISetMaps.ordered(), LinkedHashMap.class))
        .insert(new JImmutableSetMapStressTester(ISetMaps.sorted(), TreeMap.class))
        .insert(new JImmutableSetMapStressTester(ISetMaps.factory(String.class, String.class).withMap(IMaps.sorted()).withSet(ISets.hashed()).create(), TreeMap.class))

        .insert(new JImmutableListMapStressTester(IListMap.listMap(), HashMap.class))
        .insert(new JImmutableListMapStressTester(IListMap.insertOrderListMap(), LinkedHashMap.class))
        .insert(new JImmutableListMapStressTester(IListMap.sortedListMap(), TreeMap.class))

        .insert(new JImmutableArrayStressTester(IArrays.of(), ArrayIndexRange.INTEGER));

    public static void main(String[] argv)
        throws Exception
    {
        final OptionParser parser = new OptionParser();
        parser.accepts("help", "prints available options");
        final OptionSpec<String> fileSpec = parser.accepts("file", "specifies tokens file").withOptionalArg().ofType(String.class).defaultsTo("src/site/markdown/index.md");
        final OptionSpec<Long> seedSpec = parser.accepts("seed", "specifies random number seed").withOptionalArg().ofType(Long.class).defaultsTo(System.currentTimeMillis());
        final OptionSpec<String> testSpec = parser.accepts("test", "specifies tests to run").withRequiredArg().ofType(String.class);
        final OptionSet options;
        try {
            options = parser.parse(argv);
        } catch (OptionException ex) {
            System.out.printf("ERROR: %s%n%n", ex.getMessage());
            printHelpMessage(parser, ISets.hashed(), ILists.of());
            return;
        }
        final ISet<String> filters = ISets.sorted(testSpec.values(options));
        final IList<StressTester> selectedTests = filters.isEmpty() ? AllTesters : AllTesters.select(tester -> filters.containsAny(tester.getOptions()));
        if (options.has("help") || selectedTests.isEmpty()) {
            printHelpMessage(parser, filters, selectedTests);
            return;
        }

        final IList<String> filenames = ILists.allOf(options.valuesOf(fileSpec));
        final IList<String> tokens = StressTestUtil.loadTokens(filenames);
        System.out.printf("%nLoaded %d tokens from %d files%n", tokens.size(), filenames.size());

        long seed = options.valueOf(seedSpec);
        final Random random = new Random(seed);

        System.out.printf("%nRunning %d testers: %s%n", selectedTests.size(), valuesString(selectedTests.transform(StressTester::getTestName)));
        //noinspection InfiniteLoopStatement
        while (true) {
            for (StressTester tester : selectedTests) {
                System.out.printf("%nStarting %s with seed %d%n", tester.getTestName(), seed);
                tester.execute(random, tokens);
                seed = System.currentTimeMillis();
                random.setSeed(seed);
                System.out.println("sleeping before next test");
                //noinspection BusyWait
                Thread.sleep(5000);
            }
        }
    }

    private static void printHelpMessage(OptionParser parser,
                                         ISet<String> selectedTestArgs,
                                         IList<StressTester> selectedTests)
        throws IOException
    {
        if (selectedTests.size() > 0) {
            System.out.println("Selected Tests: " + valuesString(selectedTests.transform(StressTester::getTestName)));
            System.out.println();
        } else if (selectedTestArgs.size() > 0) {
            System.out.println("ERROR: invalid test args: " + valuesString(selectedTestArgs));
            System.out.println();
        }

        System.out.println("Available Options:");
        System.out.println();
        parser.printHelpOn(System.out);

        final JImmutableSetMapFactory<String, String> filterMapFactory = ISetMaps.factory(String.class, String.class)
            .withMap(IMaps.sorted())
            .withSet(ISets.sorted());
        final ISetMap<String, String> filterMap = AllTesters.stream()
            .flatMap(tester -> tester.getOptions()
                .stream()
                .map(option -> IMapEntry.entry(tester.getTestName(), option)))
            .collect(filterMapFactory.collector());
        System.out.println();
        System.out.println("Available Filters By Class:");
        System.out.printf("%-40s  %s%n", "Tester Class", "Filters");
        filterMap.stream()
            .forEach(e -> System.out.printf("%-40s  %s%n", e.getKey(), valuesString(e.getValue())));

        System.out.println();
        System.out.println("Available Filters");
        System.out.printf("%-20s  %s%n", "Filter", "Tester Classes");
        filterMap.stream()
            .flatMap(e -> e.getValue().stream().map(s -> IMapEntry.entry(s, e.getKey())))
            .collect(filterMapFactory.collector())
            .forEach(e -> System.out.printf("%-20s  %s%n", e.getKey(), valuesString(e.getValue())));
    }

    private static String valuesString(Iterable<?> objects)
    {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(object);
        }
        return sb.toString();
    }
}
