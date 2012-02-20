package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.core.AbstractGlobalItemScorer;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Score items based on the basket of items using an item-item CF model.
 * 
 * @author Shuo Chang <schang@cs.umn.edu>
 * 
 */
public class GlobalItemItemScorer extends AbstractGlobalItemScorer implements
		GlobalItemItemModelBackedScorer {
	protected final ItemItemModel model;
	protected final int neighborhoodSize;
	protected @Nonnull NeighborhoodScorer scorer;

	public GlobalItemItemScorer(DataAccessObject dao, ItemItemModel m,
			@NeighborhoodSize int nnbrs,
			NeighborhoodScorer scorer) {
		super(dao);
		model = m;
		neighborhoodSize = nnbrs;
		this.scorer = scorer;
	}

    @Override
    public ItemItemModel getModel() {
        return model;
    }
    
	@Override
	public SparseVector globalScore(Collection<Long> queryItems,
			Collection<Long> items) {
		// create the unary rating for the items
		double[] ratings = new double[queryItems.size()];
		for(int i = 0; i < ratings.length; i++)
			ratings[i] = 0.0;
		long[] ids = new long[queryItems.size()];
		int i = 0;
		for(Long id:queryItems)
			ids[i++] = id.longValue();
		// create a dummy user vector with user id = 0
		UserVector basket = new UserVector(0, ids, ratings, queryItems.size());
		
        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }
        
        MutableSparseVector preds = scoreItems(basket, iset);
		
		return preds.freeze();
	}
	
    /**
     * Compute item scores for a user. 
     * The same implementation with 
     * {@link ItemItemScorer#scoreItems(SparseVector, LongSortedSet)}
     * 
     * 
     * @param basket The user vector for which scores are to be computed.
     * @param items The items to score.
     * @return The scores for the items. The key domain contains all items; only
     *         those items with scores are set.
     */
    protected MutableSparseVector scoreItems(SparseVector basket,
                                             LongSortedSet items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        // We ran reuse accumulators
        ScoredItemAccumulator accum;
        if (neighborhoodSize > 0) {
            accum = new TopNScoredItemAccumulator(neighborhoodSize);
        } else {
            accum = new UnlimitedScoredItemAccumulator();
        }

        // FIXME Make sure the direction on similarities is right for asym.
        // for each item, compute its prediction
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();

            // find all potential neighbors
            // FIXME: Take advantage of the fact that the neighborhood is sorted
            ScoredLongList neighbors = model.getNeighbors(item);

            if (neighbors == null) {
                /* we cannot predict this item */
                continue;
            }

            // filter and truncate the neighborhood
            ScoredLongListIterator niter = neighbors.iterator();
            while (niter.hasNext()) {
                long oi = niter.nextLong();
                double score = niter.getScore();
                if (basket.containsKey(oi)) {
                    accum.put(oi, score);
                }
            }
            neighbors = accum.finish();

            // compute score & place in vector
            final double score = scorer.score(neighbors, basket);
            if (!Double.isNaN(score)) {
                scores.set(item, score);
            }
        }

        return scores;
    }


	@Override
	public LongSet getScoreableItems(Collection<Long> queryItems) {
        // FIXME This method incorrectly assumes the model is symmetric
        LongSet items = new LongOpenHashSet();
        Iterator<Long> iter = queryItems.iterator();
        while (iter.hasNext()) {
            final long item = iter.next().longValue();
            items.addAll(model.getNeighbors(item));
        }
        return items;
    }

}
