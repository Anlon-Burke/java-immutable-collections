///###////////////////////////////////////////////////////////////////////////
//
// Burton Computer Corporation
// http://www.burton-computer.com
//
// Copyright (c) 2014, Burton Computer Corporation
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

package org.javimmutable.collections.hash;

import org.javimmutable.collections.Cursor;
import org.javimmutable.collections.Holder;
import org.javimmutable.collections.Holders;
import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.MapEntry;
import org.javimmutable.collections.array.trie32.EmptyTrieNode;
import org.javimmutable.collections.array.trie32.Transforms;
import org.javimmutable.collections.array.trie32.TrieNode;
import org.javimmutable.collections.common.AbstractJImmutableMap;
import org.javimmutable.collections.common.MutableDelta;

public class JImmutableHashMap<T, K, V>
        extends AbstractJImmutableMap<K, V>
{
    // we only new one instance of the transformations object
    static final HashValueListTransforms TRANSFORMS = new HashValueListTransforms();

    // we only new one instance of the transformations object
    static final HashValueTreeTransforms COMPARABLE_TRANSFORMS = new HashValueTreeTransforms();

    // this is safe since the transformations object works for any possible K and V
    @SuppressWarnings("unchecked")
    static final JImmutableHashMap EMPTY = new JImmutableHashMap(EmptyTrieNode.of(), 0, TRANSFORMS);

    // this is safe since the transformations object works for any possible K and V
    @SuppressWarnings("unchecked")
    static final JImmutableHashMap COMPARABLE_EMPTY = new JImmutableHashMap(EmptyTrieNode.of(), 0, COMPARABLE_TRANSFORMS);

    private final TrieNode<T> root;
    private final int size;
    private final Transforms<T, K, V> transforms;

    private JImmutableHashMap(TrieNode<T> root,
                              int size,
                              Transforms<T, K, V> transforms)
    {
        this.root = root;
        this.size = size;
        this.transforms = transforms;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> JImmutableMap<K, V> of()
    {
        return (JImmutableMap<K, V>)EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>, V> JImmutableMap<K, V> comparableOf()
    {
        return (JImmutableMap<K, V>)COMPARABLE_EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> JImmutableMap<K, V> of(Class<K> klass)
    {
        return klass.isAssignableFrom(Comparable.class) ? COMPARABLE_EMPTY : EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> JImmutableMap<K, V> forKey(K key)
    {
        return (key instanceof Comparable) ? COMPARABLE_EMPTY : EMPTY;
    }

    @Override
    public V getValueOr(K key,
                        V defaultValue)
    {
        return root.getValueOr(TrieNode.ROOT_SHIFT, key.hashCode(), key, transforms, defaultValue);
    }

    @Override
    public Holder<V> find(K key)
    {
        return root.find(TrieNode.ROOT_SHIFT, key.hashCode(), key, transforms);
    }

    @Override
    public Holder<Entry<K, V>> findEntry(K key)
    {
        Holder<V> value = find(key);
        if (value.isEmpty()) {
            return Holders.of();
        } else {
            return Holders.<Entry<K, V>>of(MapEntry.of(key, value.getValue()));
        }
    }

    @Override
    public JImmutableMap<K, V> assign(K key,
                                      V value)
    {
        MutableDelta sizeDelta = new MutableDelta();
        TrieNode<T> newRoot = root.assign(TrieNode.ROOT_SHIFT, key.hashCode(), key, value, transforms, sizeDelta);
        if (newRoot == root) {
            return this;
        } else {
            return new JImmutableHashMap<T, K, V>(newRoot, size + sizeDelta.getValue(), transforms);
        }
    }

    @Override
    public JImmutableMap<K, V> delete(K key)
    {
        MutableDelta sizeDelta = new MutableDelta();
        TrieNode<T> newRoot = root.delete(TrieNode.ROOT_SHIFT, key.hashCode(), key, transforms, sizeDelta);
        if (newRoot == root) {
            return this;
        } else if (newRoot.isEmpty()) {
            return EmptyHashMap.of();
        } else {
            return new JImmutableHashMap<T, K, V>(newRoot, size + sizeDelta.getValue(), transforms);
        }
    }

    @Override
    public int size()
    {
        return size;
    }

    @Override
    public JImmutableMap<K, V> deleteAll()
    {
        return EmptyHashMap.of();
    }

    @Override
    public Cursor<Entry<K, V>> cursor()
    {
        return root.anyOrderEntryCursor(transforms);
    }

    // for unit test to verify proper transforms selected
    Transforms getTransforms()
    {
        return transforms;
    }

}
