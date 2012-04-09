package approdictio.dict;

import java.util.Formatter;

import approdictio.dict.BKTree.LinkTable;

class BKNode<V> {
  private final V value;

  private final LinkTable<V> links;

  public BKNode(V value) {
    this.value = value;
    this.links = new BKTree.ArrayLinkTable<V>();
  }
  public V getValue() {
    return value;
  }
  public void set(int d, BKNode<V> node) {
    links.set(d, node);
  }
  public BKNode<V> get(int d) {
    return links.get(d);
  }
  public void dump(Appendable out, int d, String indent) {
    Formatter f = new Formatter(out);
    f.format("%s%d: %s%n", indent, d, value);
    int l = links.size();
    for(int i = 0; i < l; i++) {
      BKNode<V> node = links.get(i);
      if( node == null ) continue;
      node.dump(out, i, "  " + indent);
    }
  }
  public LinkTable<V> getChildren() {
    return links;
  }
}