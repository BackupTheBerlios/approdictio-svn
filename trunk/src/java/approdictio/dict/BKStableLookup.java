package approdictio.dict;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import approdictio.dict.BKTree.LinkTable;

/**
 * is an iterator that returns lookup results in a {@link BKTree} according
 * to an order provided. This order is completely independent of the metric
 * used in the tree.
 * <p>
 * <strong>IMPORTANT:</strong>
 * </p>
 * <ol>
 * <li>The {@link BKTree} must have been filled with elements in the order
 * given, smallest element first. The the first result element will be the
 * smallest matching element.</li>
 * <li>While the iterator is being used, no new elements may be inserted in
 * to the tree, otherwise the result is undefined.</li>
 * </ol>
 * 
 * @param <W> is the type stored in the referenced {@link BKTree}.
 */
public final class BKStableLookup<W> implements Iterator<W> {
  private final int maxDist;
  private final W queryValue;
  private final Comparator<W> order;
  private final IntMetric<W> metric;
  private W prepared = null;

  /**
   * creates an iterator to extract values from the given {@link BKTree} that
   * have at most {@code maxDist} distance to the {@code queryValue}
   * according to the tree's metric.
   * 
   * @param bkTree is the tree to search
   * @param queryValue the value to search for
   * @param maxDist the maximum distance a result may have to
   *        {@code queryValue}
   * @param order according to which matches are returned. This
   *        requires that the tree was filled with values sorted according to
   *        this order, smallest first. Note: the {@code queryValue} is never
   *        compared against any of the tree's values with this comparator.
   *        The comparator is only ever used to compare tree elements with
   *        each other.
   */
  public BKStableLookup(BKTree<W> bkTree,
                            W queryValue, 
                            int maxDist,
                            Comparator<W> order) {
    this.queryValue = queryValue;
    this.order = order;
    this.maxDist = maxDist;
    BKNode<W> root = bkTree.getRoot();
    if( root!=null) {
      toInspect.add(root);
    }
    this.metric = bkTree.getMetric();
  }

  private final Comparator<BKNode<W>> 
  valueComparator = new Comparator<BKNode<W>>() {
    @Override
    public int compare(BKNode<W> arg0, BKNode<W> arg1) {
      return order.compare(arg0.getValue(), arg1.getValue());
    }
  };
  PriorityQueue<BKNode<W>> toInspect = 
    new PriorityQueue<BKNode<W>>(200, valueComparator);

  @Override
  public boolean hasNext() {
    return prepare()!=null;
  }

  @Override
  public W next() {
    W value = prepare();
    prepared = null;
    if (value==null) {
      throw new NoSuchElementException();
    }
    return value;
  }
  
  private W prepare() {
    if (prepared!=null) {
      return prepared;
    }
    
    while (!toInspect.isEmpty()) {
      BKNode<W> node = toInspect.poll();
      prepared = node.getValue();
      insertChildren(toInspect, node);
      //System.out.println("checking "+prepared);
      int d = metric.d(prepared, queryValue);
      if (d<=maxDist) {
        return prepared;
      }
    }
    return null;
  }
  
  private void 
  insertChildren(PriorityQueue<BKNode<W>> toInspect,  BKNode<W> node) {
    LinkTable<W> children = node.getChildren();
    int l = children.size();
    for(int i=0; i<l; i++) {
      BKNode<W> child = children.get(i);
      if (child==null) continue;
      toInspect.add(child);
    }
  }
  /**
   * is not supported.
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();      
  }
}