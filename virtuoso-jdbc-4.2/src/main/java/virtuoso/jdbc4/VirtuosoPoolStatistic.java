package virtuoso.jdbc4;
import java.util.*;
public class VirtuosoPoolStatistic implements Cloneable {
  protected String name;
  protected volatile int conn_unUsed;
  protected volatile int connIn_Use;
  protected volatile int cacheSize = 0;
  protected volatile int _hits = 0;
  protected volatile int _misses = 0;
  protected volatile long _max_wtime = 0L;
  protected volatile long _min_wtime = 0L;
  protected volatile long _cum_wtime = 0L;
  protected VirtuosoPoolStatistic() {
  }
  protected void setCacheParam(String _name, int _cacheSize, int _conn_unUsed, int _connIn_Use) {
    name = _name;
    cacheSize = _cacheSize;
    conn_unUsed = _conn_unUsed;
    connIn_Use = _connIn_Use;
  }
  protected void setWaitingTime(long tm) {
    if(_min_wtime == 0L || tm < _min_wtime )
       _min_wtime = tm;
    if(tm > _max_wtime)
       _max_wtime = tm;
    _cum_wtime += tm;
  }
  protected synchronized Object clone() {
    try {
      VirtuosoPoolStatistic v = (VirtuosoPoolStatistic)super.clone();
      v._hits = _hits;
      v._misses = _misses;
      v._max_wtime = _max_wtime;
      v._min_wtime = _min_wtime;
      v._cum_wtime = _cum_wtime;
      return v;
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  public int getHits() {
    return _hits;
  }
  public int getMisses() {
    return _misses;
  }
  public long getMaxWaitTime() {
    return _max_wtime;
  }
  public long getMinWaitTime() {
    return _min_wtime;
  }
  public long getCumWaitTime() {
    return _cum_wtime;
  }
  public int getCacheSize() {
    return cacheSize;
  }
  public int getConnsInUse() {
    return connIn_Use;
  }
  public int getConnsUnUsed() {
    return conn_unUsed;
  }
  public String getName() {
    return name;
  }
  public String toString() {
    StringBuffer buf = new StringBuffer(128);
    buf.append("--------------------------------------\n");
    buf.append("  ** Cache Statistics for the ["+name+"] **\n");
    buf.append("--------------------------------------\n");
    buf.append(" connection's cacheSize= "); buf.append(cacheSize); buf.append('\n');
    buf.append("      used connections = "); buf.append(connIn_Use); buf.append('\n');
    buf.append("    unused connections = "); buf.append(conn_unUsed); buf.append('\n');
    buf.append("      total cache hits = "); buf.append(_hits); buf.append('\n');
    buf.append("    total cache misses = "); buf.append(_misses); buf.append('\n');
    buf.append(" min waiting time (millisec)= "); buf.append(_min_wtime); buf.append('\n');
    buf.append(" max waiting time (millisec)= "); buf.append(_max_wtime); buf.append('\n');
    buf.append(" avg waiting time (millisec)= "); buf.append((_misses==0 ? 0 : _cum_wtime/_misses)); buf.append('\n');
    return buf.toString();
  }
}
