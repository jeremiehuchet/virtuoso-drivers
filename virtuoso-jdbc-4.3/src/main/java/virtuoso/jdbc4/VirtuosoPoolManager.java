package virtuoso.jdbc4;
import java.util.*;
import java.util.concurrent.atomic.*;
public class VirtuosoPoolManager {
  private static WeakHashMap<Object,Object> connPools = new WeakHashMap<Object,Object>(50);
  private static VirtuosoPoolManager poolMgr = null;
  private static Object lock = new Object();
  private static ThreadGroup thrGroup = null;
  private static Thread poolChecker = null;
  private static Thread propertyChecker = null;
  private static AtomicBoolean isRun = new AtomicBoolean(false);
  public static VirtuosoPoolManager getInstance() {
    synchronized(lock) {
      if (poolMgr == null) {
        isRun.set(true);
        poolMgr = new VirtuosoPoolManager();
        thrGroup = new ThreadGroup("Virtuoso Pool Manager");
        thrGroup.setDaemon(true);
        poolChecker = new Thread(thrGroup, "Virtuoso Pool Checker") {
          public void run() {
            Object[] poolTmp;
            VirtuosoConnectionPoolDataSource pds;
            while(true) {
              try {
                sleep(500L);
              } catch (InterruptedException e) { }
              if (isRun.get() != true)
                return;
              synchronized(lock) {
                  poolTmp = connPools.keySet().toArray();
              }
              for(int i = 0; i < poolTmp.length; i++) {
                  pds = (VirtuosoConnectionPoolDataSource)poolTmp[i];
                  if (pds != null)
                    pds.checkPool();
                  poolTmp[i] = null;
              }
              pds = null;
            }
          }
        };
        poolChecker.setDaemon(true);
        poolChecker.start();
        propertyChecker = new Thread(thrGroup, "Virtuoso Property Checker") {
          public void run() {
            Object[] poolTmp;
            VirtuosoConnectionPoolDataSource pds;
            while(true) {
              try {
                sleep(500L);
              } catch (InterruptedException e) { }
              if (isRun.get() != true)
                return;
              synchronized(lock) {
                  poolTmp = connPools.keySet().toArray();
              }
              for(int i = 0; i < poolTmp.length; i++) {
                  pds = (VirtuosoConnectionPoolDataSource)poolTmp[i];
                  if (pds != null)
                    pds.checkPropQueue();
                  poolTmp[i] = null;
              }
              pds = null;
            }
          }
        };
        propertyChecker.setDaemon(true);
        propertyChecker.start();
      }
    }
    return poolMgr;
  }
  protected void addPool(VirtuosoConnectionPoolDataSource pool) {
    if (isRun.get() != true)
      return;
    synchronized(lock) {
     connPools.put(pool, null);
    }
  }
  public void shutdown() {
    if (isRun.get() != true)
      return;
    synchronized(lock) {
      isRun.set(false);
      VirtuosoConnectionPoolDataSource pds;
      for(Iterator i = connPools.keySet().iterator(); i.hasNext(); ) {
        pds = (VirtuosoConnectionPoolDataSource)i.next();
        if (pds != null)
          try {
            pds.close();
          } catch(Exception e) { }
      }
      connPools.clear();
    }
  }
  public VirtuosoPoolStatistic[] getAll_statistics() {
   VirtuosoConnectionPoolDataSource[] poolTmp = (VirtuosoConnectionPoolDataSource[])(connPools.keySet().toArray(new VirtuosoConnectionPoolDataSource[0]));
   VirtuosoPoolStatistic[] retVal = new VirtuosoPoolStatistic[poolTmp.length];
   for(int i = 0; i < poolTmp.length; i++) {
      retVal[i] = poolTmp[i].get_statistics();
      poolTmp[i] = null;
    }
    return retVal;
  }
}
