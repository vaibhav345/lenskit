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
package org.lenskit.inject;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.grouplens.grapht.AbstractContext;
import org.grouplens.grapht.Binding;
import org.grouplens.grapht.Context;
import org.grouplens.grapht.reflect.Qualifiers;
import org.lenskit.LenskitBinding;
import org.lenskit.LenskitConfigContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.annotation.Annotation;

/**
 * Helper for implementing Lenskit config contexts.
 *
 * @since 1.0
 */
public abstract class AbstractConfigContext extends AbstractContext implements LenskitConfigContext {
    /**
     * Coerce a Grapht context to a LensKit context.
     *
     * @param ctx The context.
     * @return A LensKit context, as a wrapper if necessary.
     */
    protected static LenskitConfigContext wrapContext(Context ctx) {
        if (ctx instanceof LenskitConfigContext) {
            return (LenskitConfigContext) ctx;
        } else {
            return new ContextWrapper(ctx);
        }
    }

    @Override
    public <T> LenskitBinding<T> bind(Class<? extends Annotation> qual, Class<T> type) {
        return LenskitBindingImpl.wrap(super.bind(qual, type));
    }

    @Override
    public <T> LenskitBinding<T> bindAny(Class<T> type) {
        return LenskitBindingImpl.wrap(super.bindAny(type));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Binding set(@Nonnull Class<? extends Annotation> param) {
        Preconditions.checkNotNull(param);
        // Parameter annotation appears on the alias target
        Class<? extends Annotation> real = Qualifiers.resolveAliases(param);
        final Parameter annot = real.getAnnotation(Parameter.class);
        if (annot == null) {
            throw new IllegalArgumentException(param.toString() + "has no Parameter annotation");
        }
        Class<?> type = annot.value();
        Binding<?> binding;
        if (type.equals(File.class)) {
            binding = LenskitBindingImpl.wrap(bind(File.class),
                                              new StringToFileConversion());
        } else {
            binding = bind(annot.value());
        }
        return binding.withQualifier(param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addComponent(@Nonnull Object obj) {
        bind((Class) obj.getClass()).toInstance(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addComponent(@Nonnull Class<?> type) {
        bind((Class) type).to(type);
    }

    @Override @Deprecated
    public LenskitConfigContext in(Class<?> type) {
        return within(type);
    }
    
    @Override @Deprecated
    public LenskitConfigContext in(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return within(qualifier, type);
    }
    
    @Override @Deprecated
    public LenskitConfigContext in(@Nullable Annotation qualifier, Class<?> type) {
        return within(qualifier, type);
    }

    private static class StringToFileConversion implements Function<Object,Optional<File>> {
        @Nullable
        @Override
        public Optional<File> apply(@Nullable Object input) {
            if (input == null) {
                return null;
            } else if (input instanceof String) {
                return Optional.of(new File((String) input));
            } else {
                return Optional.absent();
            }
        }
    }
}
