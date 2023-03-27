package org.javimmutable.collections.deque;

import java.util.Iterator;
import org.javimmutable.collections.IDeque;
import org.javimmutable.collections.IDequeBuilder;
import org.javimmutable.collections.Indexed;
import org.javimmutable.collections.common.StandardBuilderTests;

public class BuilderTestAdapter<T>
    implements StandardBuilderTests.BuilderAdapter<T, IDeque<T>>
{
    private final IDequeBuilder<T> builder;

    public BuilderTestAdapter(IDequeBuilder<T> builder)
    {
        this.builder = builder;
    }

    @Override
    public IDeque<T> build()
    {
        return builder.build();
    }

    @Override
    public void clear()
    {
        builder.clear();
    }

    @Override
    public int size()
    {
        return builder.size();
    }

    @Override
    public void add(T value)
    {
        builder.add(value);
    }

    @Override
    public void add(Iterator<? extends T> source)
    {
        builder.add(source);
    }

    @Override
    public void add(Iterable<? extends T> source)
    {
        builder.add(source);
    }

    @Override
    public <K extends T> void add(K... source)
    {
        builder.add(source);
    }

    @Override
    public void add(Indexed<? extends T> source,
                    int offset,
                    int limit)
    {
        builder.add(source, offset, limit);
    }

    @Override
    public void add(Indexed<? extends T> source)
    {
        builder.add(source);
    }
}
