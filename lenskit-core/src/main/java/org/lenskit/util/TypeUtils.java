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
package org.lenskit.util;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.joda.convert.FromStringConverter;
import org.joda.convert.StringConvert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Various type utilities used in LensKit.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TypeUtils {
    private TypeUtils() {
    }

    /**
     * Return the supertype closure of a type (the type and all its transitive
     * supertypes).
     *
     * @param type The type.
     * @return All supertypes of the type, including the type itself.
     */
    public static Set<Class<?>> typeClosure(Class<?> type) {
        if (type == null) {
            return Collections.emptySet();
        }

        Set<Class<?>> supertypes = new HashSet<>();
        supertypes.add(type);
        supertypes.addAll(typeClosure(type.getSuperclass()));
        for (Class<?> iface : type.getInterfaces()) {
            supertypes.addAll(typeClosure(iface));
        }

        return supertypes;
    }

    /**
     * Make a type token for a list of a particular element type.
     * @param element The element type.
     * @param <T> The element type.
     * @return A type token representing {@code List<T>}.
     */
    public static <T> TypeToken<List<T>> makeListType(TypeToken<T> element) {
        return new TypeToken<List<T>>() {}
                .where(new TypeParameter<T>() {}, element);
    }

    /**
     * Extract the element type from a type token representing a list.
     * @param token The type token.
     * @param <T> The list element type.
     * @return The type token for the list's element type.
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeToken<T> listElementType(TypeToken<? extends List<T>> token) {
        Type t = token.getType();
        Preconditions.checkArgument(t instanceof ParameterizedType, "list type not resolved");
        ParameterizedType pt = (ParameterizedType) t;
        Type[] args = pt.getActualTypeArguments();
        assert args.length == 1;
        return (TypeToken<T>) TypeToken.of(args[0]);
    }

    /**
     * Resolve a type name into a type.  This is like class lookup with a few additions:
     *
     * - Aliases for common types (`string`, `int`, `long`, `double`)
     * - Lists are handled with an array syntax (`string[]` becomes `List<String>`)
     *
     * @param type The type name to resolve.
     * @return The type.
     */
    public static TypeToken<?> resolveTypeName(String type) {
        Preconditions.checkArgument(type.length() > 0, "type name is empty");
        if (type.endsWith("[]")) {
            String nt = type.substring(0, type.length() - 2);
            TypeToken<?> inner = resolveTypeName(nt);
            return makeListType(inner);
        }
        switch (type) {
        case "string":
        case "String":
            return TypeToken.of(String.class);
        case "int":
        case "Integer":
            return TypeToken.of(Integer.class);
        case "long":
        case "Long":
            return TypeToken.of(Long.class);
        case "double":
        case "real":
        case "Double":
            return TypeToken.of(Double.class);
        case "text":
        case "Text":
            return TypeToken.of(Text.class);
        default:
            try {
                return TypeToken.of(ClassUtils.getClass(type));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot load type name ", e);
            }
        }
    }

    /**
     * Turn a type token into a parsable type name.
     * @param type The type token.
     * @param <T> The type.
     * @return A string that can be parsed by {@link #resolveTypeName(String)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> String makeTypeName(TypeToken<T> type) {
        Class<?> raw = type.getRawType();
        if (raw.equals(List.class)) {
            return makeTypeName(listElementType((TypeToken) type)) + "[]";
        } else if (raw.equals(String.class)) {
            return "string";
        } else if (raw.equals(Double.class)) {
            return "double";
        } else if (raw.equals(Integer.class)) {
            return "int";
        } else if (raw.equals(Long.class)) {
            return "long";
        } else {
            return raw.getName();
        }
    }

    /**
     * Look up a converter to convert strings to the specified type.  List types are converted from comma-separated
     * values.
     *
     * @param type The type.
     * @param <T> The type.
     * @return A converter to parse objects of type {@code type} from strings.
     */
    @SuppressWarnings("unchecked")
    public static <T> FromStringConverter<T> lookupFromStringConverter(TypeToken<T> type) {
        Class<? super T> rt = type.getRawType();
        if (rt.equals(List.class)) {
            TypeToken elt = listElementType((TypeToken) type);
            FromStringConverter inner = lookupFromStringConverter(elt);
            return new ListParser(elt.getRawType(), inner);
        } else {
            return (FromStringConverter) StringConvert.INSTANCE.findConverter(rt);
        }
    }

    private static class ListParser<T> implements FromStringConverter<List<T>> {
        private final Class<T> elementType;
        private final FromStringConverter<T> elementConverter;

        public ListParser(Class<T> et, FromStringConverter<T> ec) {
            elementType = et;
            elementConverter = ec;
        }

        @Override
        public List<T> convertFromString(Class<? extends List<T>> cls, String str) {
            assert cls != null && cls.isAssignableFrom(List.class);
            List<T> list = new ArrayList<>();
            StrTokenizer tok = StrTokenizer.getCSVInstance(str);
            while (tok.hasNext()) {
                String next = tok.next();
                list.add(elementConverter.convertFromString(elementType, next));
            }

            return list;
        }
    }
}
