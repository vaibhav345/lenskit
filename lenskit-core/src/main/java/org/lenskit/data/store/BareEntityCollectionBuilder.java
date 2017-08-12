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
package org.lenskit.data.store;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.collections.LongUtils;

/**
 * Builder for bare entity collections.
 */
class BareEntityCollectionBuilder extends EntityCollectionBuilder {
    private final EntityType entityType;
    private LongSet ids = new LongOpenHashSet();

    BareEntityCollectionBuilder(EntityType et) {
        entityType = et;
    }

    @Override
    public <T> EntityCollectionBuilder addIndex(TypedName<T> attribute) {
        return this;
    }

    @Override
    public EntityCollectionBuilder addIndex(String attrName) {
        return this;
    }

    @Override
    public EntityCollectionBuilder add(Entity e, boolean replace) {
        ids.add(e.getId());
        return this;
    }

    @Override
    public Iterable<Entity> entities() {
        return () -> new BareEntityCollection.EntityIterator(entityType, ids.iterator());
    }

    @Override
    public EntityCollection build() {
        return new BareEntityCollection(entityType, LongUtils.packedSet(ids));
    }
}
