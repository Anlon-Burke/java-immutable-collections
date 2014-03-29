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

package org.javimmutable.collections.inorder;

import org.javimmutable.collections.JImmutableMap;
import org.javimmutable.collections.JImmutableSet;
import org.javimmutable.collections.common.AbstractJImmutableSet;

/**
 * JImmutableSet implementation built on top of a JImmutableInsertOrderMap.  During iteration
 * elements are returned in the same order they were inserted into the set.  Performance is
 * slower than hash or tree sets but should be sufficient or most algorithms where insert
 * order matters.
 *
 * @param <T>
 */
public class JImmutableInsertOrderSet<T>
        extends AbstractJImmutableSet<T>
{
    @SuppressWarnings("unchecked")
    private static final JImmutableInsertOrderSet EMPTY = new JImmutableInsertOrderSet(JImmutableInsertOrderMap.of());

    private JImmutableInsertOrderSet(JImmutableMap<T, Boolean> map)
    {
        super(map);
    }

    @SuppressWarnings("unchecked")
    public static <T> JImmutableInsertOrderSet<T> of()
    {
        return (JImmutableInsertOrderSet<T>)EMPTY;
    }

    @Override
    public JImmutableSet<T> deleteAll()
    {
        return of();
    }

    @Override
    protected JImmutableSet<T> create(JImmutableMap<T, Boolean> map)
    {
        return new JImmutableInsertOrderSet<T>(map);
    }

    @Override
    protected JImmutableMap<T, Boolean> emptyMap()
    {
        return JImmutableInsertOrderMap.of();
    }
}