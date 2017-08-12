/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.data.entities;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import net.jcip.annotations.Immutable;
import java.io.*;
import java.util.*;

/**
 * A set of attributes.  Attributes are mapped to positions.  If {@link CommonAttributes#ENTITY_ID} is in the
 * set, it is always at position 0.  Iteration is in order.
 *
 * This class exists for two reasons:
 *
 * - To share space for storing attribute names - including the list of attribute names - between multiple
 * entities with the same set of attributes.
 * - To speed up lookups; microbenchmarks have found that linear search with object identity is faster than
 * hashtable lookups for small maps, and entities do not tend to have very many attributes.
 */
@Immutable
public class AttributeSet extends AbstractSet<TypedName<?>> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Interner<AttributeSet> setCache = Interners.newWeakInterner();

    // Typed names are always interned, so we can use == to compare them.
    private final TypedName<?>[] names;
    private transient Set<String> nameSet;

    AttributeSet(TypedName<?>[] ans) {
        names = ans;
    }

    /**
     * Create a new attribute set.
     * @param names The list of attributes.
     * @return The attribute set.
     */
    public static AttributeSet create(TypedName<?>... names) {
        return create(Arrays.asList(names));
    }

    /**
     * Create a new attribute set.
     * @param names The list of attributes.
     * @return The attribute set.
     */
    public static AttributeSet create(List<? extends TypedName<?>> names) {
        Preconditions.checkArgument(names.size() > 0, "no attribute names");
        TypedName[] arr = names.toArray(new TypedName[names.size()]);
        if (arr[0] != CommonAttributes.ENTITY_ID) {
            int iidx = names.indexOf(CommonAttributes.ENTITY_ID);
            if (iidx >= 0) {
                TypedName n = arr[0];
                arr[0] = arr[iidx];
                arr[iidx] = n;
                assert arr[0] == CommonAttributes.ENTITY_ID;
            }
        }
        return setCache.intern(new AttributeSet(arr));
    }

    /**
     * Look up an attribute.
     * @param name The attribute.
     * @return The attribute's index, or a negative value if it does not exist.  Nonexistence is further
     * differentiated with -1 for no attribute, -2 for type mismatch.
     */
    public int lookup(TypedName<?> name) {
        return lookup(name, false);
    }

    /**
     * Look up an attribute.
     * @param name The attribute.
     * @param matchSubclasses If true, then attributes whose types are subclasses of `name`'s type will also match.
     * @return The attribute's index, or a negative value if it does not exist.  Nonexistence is further
     * differentiated with -1 for no attribute, -2 for type mismatch.
     */
    public int lookup(TypedName<?> name, boolean matchSubclasses) {
        // Linear search with interned objects is faster for short lists
        for (int i = 0; i < names.length; i++) {
            TypedName<?> n = names[i];
            if (n == name) {
                return i;
            } else if (n.getName() == name.getName()) {
                if (matchSubclasses && n.getType().isSubtypeOf(name.getType())) {
                    return i;
                } else {
                    // FIXME Handle typecasting
                    return -2;
                }
            }
        }
        return -1;
    }

    /**
     * Look up an attribute.
     * @param name The attribute name.
     * @return The attribute's index, or a negative value if it does not exist.
     */
    public int lookup(String name) {
        name = name.intern();

        for (int i = 0; i < names.length; i++) {
            TypedName<?> n = names[i];
            if (n.getName() == name) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the type name for an attribute by index.
     * @param idx The attribute index.
     * @return The type name.
     */
    public TypedName<?> getAttribute(int idx) {
        return names[idx];
    }

    @Override
    public Iterator<TypedName<?>> iterator() {
        return new TNIter();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof TypedName && lookup((TypedName<?>) o) >= 0;
    }

    /**
     * Get the number of attributes in this set.
     * @return The number of attributes in this set.
     */
    @Override
    public int size() {
        return names.length;
    }

    /**
     * Get a set-of-strings view of this attribute set.
     * @return The A set-of-strings view of the attribute set.
     */
    public Set<String> nameSet() {
        Set<String> s = nameSet;
        if (s == null) {
            nameSet = s = new NameSet();
        }
        return s;
    }

    private class NameSet extends AbstractSet<String> {
        @Override
        public boolean contains(Object o) {
            return o instanceof String && lookup((String) o) >= 0;
        }

        @Override
        public Iterator<String> iterator() {
            return Iterators.transform(new TNIter(), TypedName::getName);
        }

        @Override
        public int size() {
            return names.length;
        }
    }

    private class TNIter implements Iterator<TypedName<?>> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < names.length;
        }

        @Override
        public TypedName<?> next() {
            if (pos >= names.length) {
                throw new NoSuchElementException();
            }
            return names[pos++];
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("typed names must use serialization proxy");
    }

    private void readObjectNoData() throws ObjectStreamException {
        throw new InvalidObjectException("typed names must use serialization proxy");
    }

    private Object writeReplace() {
        return new SerialProxy(names);
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 2L;

        private List<TypedName<?>> names;

        public SerialProxy(TypedName<?>[] names) {
            this.names = Lists.newArrayList(names);
        }

        private Object readResolve() throws ObjectStreamException {
            return create(names);
        }
    }
}
