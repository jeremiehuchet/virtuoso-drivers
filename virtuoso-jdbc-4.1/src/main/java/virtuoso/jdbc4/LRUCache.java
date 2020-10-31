package virtuoso.jdbc4;
import java.util.*;
class LRUCache<K,V> extends LinkedHashMap<K,V>
{
  int maxSize;
  public LRUCache(int size) {
    super(size);
    maxSize = size;
  }
  protected boolean removeEldestEntry(Map.Entry eldest) {
    return (size() > this.maxSize);
  }
}
