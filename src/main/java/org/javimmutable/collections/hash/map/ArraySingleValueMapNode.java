///###////////////////////////////////////////////////////////////////////////
//
// Burton Computer Corporation
// http://www.burton-computer.com
//
// Copyright (c) 2020, Burton Computer Corporation
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

package org.javimmutable.collections.hash.map;

import org.javimmutable.collections.Holder;
import org.javimmutable.collections.Holders;
import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.MapEntry;
import org.javimmutable.collections.Proc2;
import org.javimmutable.collections.Proc2Throws;
import org.javimmutable.collections.common.CollisionMap;
import org.javimmutable.collections.iterators.GenericIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class ArraySingleValueMapNode<K, V>
    implements ArrayMapNode<K, V>
{
    private final K key;
    private final V value;

    public ArraySingleValueMapNode(K key,
                                   V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public int size(@Nonnull CollisionMap<K, V> collisionMap)
    {
        return 1;
    }

    @Override
    public V getValueOr(@Nonnull CollisionMap<K, V> collisionMap,
                        @Nonnull K key,
                        V defaultValue)
    {
        return key.equals(this.key) ? value : defaultValue;
    }

    @Nonnull
    @Override
    public Holder<V> find(@Nonnull CollisionMap<K, V> collisionMap,
                          @Nonnull K key)
    {
        return key.equals(this.key) ? Holders.of(value) : Holders.of();
    }

    @Nonnull
    @Override
    public ArrayMapNode<K, V> assign(@Nonnull CollisionMap<K, V> collisionMap,
                                     @Nonnull K key,
                                     V value)
    {
        final K thisKey = this.key;
        final V thisValue = this.value;
        if (!key.equals(thisKey)) {
            return new ArrayMultiValueMapNode<>(collisionMap.update(collisionMap.single(thisKey, thisValue), key, value));
        } else if (value == thisValue) {
            return this;
        } else {
            return new ArraySingleValueMapNode<>(thisKey, value);
        }
    }

    @Nullable
    @Override
    public ArrayMapNode<K, V> delete(@Nonnull CollisionMap<K, V> collisionMap,
                                     @Nonnull K key)
    {
        return key.equals(this.key) ? null : this;
    }

    @Nonnull
    @Override
    public GenericIterator.Iterable<K> keys(@Nonnull CollisionMap<K, V> collisionMap)
    {
        return GenericIterator.valueIterable(key);
    }

    @Nonnull
    @Override
    public GenericIterator.Iterable<V> values(@Nonnull CollisionMap<K, V> collisionMap)
    {
        return GenericIterator.valueIterable(value);
    }

    @Nonnull
    @Override
    public GenericIterator.Iterable<JImmutableMap.Entry<K, V>> entries(@Nonnull CollisionMap<K, V> collisionMap)
    {
        return GenericIterator.valueIterable(MapEntry.entry(key, value));
    }

    @Override
    public void forEach(@Nonnull CollisionMap<K, V> collisionMap,
                        @Nonnull Proc2<K, V> proc)
    {
        proc.apply(key, value);
    }

    @Override
    public <E extends Exception> void forEachThrows(@Nonnull CollisionMap<K, V> collisionMap,
                                                    @Nonnull Proc2Throws<K, V, E> proc)
        throws E
    {
        proc.apply(key, value);
    }
}