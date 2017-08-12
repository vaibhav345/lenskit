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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.grouplens.grapht.util.ClassLoaders;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Descriptor for the default characteristics of an entity type.
 */
public class EntityDefaults {
    private static final TypeToken<Long> LONG_TYPE = TypeToken.of(Long.class);
    private final EntityType entityType;
    private final Map<String, TypedName<?>> attributes;
    private final List<TypedName<?>> defaultColumns;
    private final Class<? extends EntityBuilder> defaultBuilder;
    private final List<EntityDerivation> defaultDerivations;

    /**
     * Construct a set of entity defaults where all common attributes are default columns.
     * @param type The entity type.
     * @param cols The default column layout.
     */
    public EntityDefaults(EntityType type, List<TypedName<?>> cols) {
        this(type, cols, cols, BasicEntityBuilder.class, Collections.<EntityDerivation>emptyList());
    }


    /**
     * Construct a set of entity defaults.
     * @param type The entity type.
     * @param attrs The well-known attributes.
     * @param cols The default column layout.
     */
    public EntityDefaults(EntityType type, Collection<TypedName<?>> attrs, List<TypedName<?>> cols,
                          Class<? extends EntityBuilder> bld, List<EntityDerivation> derivs) {
        entityType = type;
        ImmutableMap.Builder<String,TypedName<?>> abld = ImmutableMap.builder();
        for (TypedName<?> a: attrs) {
            abld.put(a.getName(), a);
        }
        attributes = abld.build();
        defaultColumns = ImmutableList.copyOf(cols);
        defaultBuilder = bld;
        defaultDerivations = ImmutableList.copyOf(derivs);
    }

    /**
     * Look up the defaults for a particular entity type.
     * @param type The entity type.
     * @return The defaults.
     */
    @Nullable
    public static EntityDefaults lookup(EntityType type) {
        // TODO Cache these defaults
        String name = type.getName();
        String path = String.format("META-INF/lenskit/entity-defaults/%s.yaml", name);
        try (InputStream stream = ClassLoaders.inferDefault(EntityDefaults.class)
                                              .getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            DefaultsBean bean = mapper.readValue(stream, DefaultsBean.class);
            return fromBean(type, bean);
        } catch (IOException e) {
            throw new UncheckedIOException("error defaults for " + type, e);
        }
    }

    /**
     * Get the entity type this set of defaults describes.
     * @return The entity type.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Look up the default attribute descriptor (typed name) for a particular attribute name.
     * @param name The attribute name.
     * @return The attribute's typed name, or `null` if the attribute is not predefined for the entity type.
     */
    public TypedName<?> getAttributeDefaults(String name) {
        return attributes.get(name);
    }

    /**
     * Get the set of attributes that can typically be associated with this entity type.
     * @return The set of common attributes for this type.
     */
    public Set<TypedName<?>> getCommonAttributes() {
        return ImmutableSet.copyOf(attributes.values());
    }

    /**
     * Get the default columns for an entity type.  These are used as defaults when reading entities
     * of this type from a columnar storage format.
     * @return The default columns for the entity type.
     */
    public List<TypedName<?>> getDefaultColumns() {
        return defaultColumns;
    }

    /**
     * Get the default entity builder for this entity type.
     * @return The default entity builder.
     */
    public Class<? extends EntityBuilder> getDefaultBuilder() {
        return defaultBuilder;
    }

    /**
     * Get the default entity derivations.
     * @return The default entity derivations.
     */
    public List<EntityDerivation> getDefaultDerivations() {
        return defaultDerivations;
    }

    /**
     * Create an entity defaults object from a bean.
     * @param type The entity type.
     * @param bean The bean.
     * @return The entity defaults.
     */
    private static EntityDefaults fromBean(EntityType type, DefaultsBean bean) {
        Map<String,TypedName<?>> attrs = new HashMap<>();
        for (Map.Entry<String,String> ab: bean.getAttributes().entrySet()) {
            attrs.put(ab.getKey(), TypedName.create(ab.getKey(), ab.getValue()));
        }

        List<TypedName<?>> cols = new ArrayList<>();
        for (String col: bean.getColumns()) {
            cols.add(attrs.get(col));
        }

        List<EntityDerivation> derivs = new ArrayList<>();
        for (Map.Entry<String,String> d: bean.getDerivations().entrySet()) {
            EntityType et = EntityType.forName(d.getKey());
            TypedName<?> attr = attrs.get(d.getValue());
            Verify.verify(attr.getType().equals(LONG_TYPE),
                          "derived entity source column has non-Long type %s",
                          attr.getType());
            derivs.add(EntityDerivation.create(et, type, (TypedName<Long>) attr));
        }

        Class<?> builder;
        String builderName = bean.getBuilder();
        if (builderName != null) {
            try {
                builder = ClassUtils.getClass(bean.getBuilder());
            } catch (ClassNotFoundException ex) {
                throw new VerifyException("bean defaults reference non-existent class " + builderName, ex);
            }
        } else {
            builder = BasicEntityBuilder.class;
        }

        return new EntityDefaults(type, attrs.values(), cols, builder.asSubclass(EntityBuilder.class),
                                  derivs);
    }

    private static class DefaultsBean {
        private Map<String, String> attributes = new HashMap<>();
        private Map<String, String> derivations = new HashMap<>();
        private List<String> columns = new ArrayList<>();
        private String builder;

        public Map<String, String> getAttributes() {
            if (attributes != null) {
                return attributes;
            } else {
                return ImmutableMap.of();
            }
        }

        public void setAttributes(Map<String,String> attributes) {
            this.attributes = attributes;
        }

        public Map<String, String> getDerivations() {
            if (derivations != null) {
                return derivations;
            } else {
                return ImmutableMap.of();
            }
        }

        public void setDerivations(Map<String,String> deriv) {
            this.derivations = deriv;
        }

        public List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public String getBuilder() {
            return builder;
        }

        public void setBuilder(String builder) {
            this.builder = builder;
        }
    }
}
