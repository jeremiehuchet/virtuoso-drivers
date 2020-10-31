package openlink.util;
import java.util.*;
public class Vector
{
   protected Object elementData[];
   protected int elementCount;
   protected int capacityIncrement;
   public Vector(int initialCapacity, int capacityIncrement)
   {
      this.elementData = new Object[initialCapacity];
      this.capacityIncrement = capacityIncrement;
   }
   public Vector(int initialCapacity)
   {
      this (initialCapacity,0);
   }
   public Vector()
   {
      this (10);
   }
   public Vector(Object[] array)
   {
      elementData = array;
      elementCount = array.length;
   }
   public void copyInto(Object anArray[])
   {
      System.arraycopy(elementData,0,anArray,0,elementCount);
   }
   public void trimToSize()
   {
      int oldCapacity = elementData.length;
      if(elementCount < oldCapacity)
      {
         Object oldData[] = elementData;
         elementData = new Object[elementCount];
         System.arraycopy(oldData,0,elementData,0,elementCount);
      }
   }
   public void ensureCapacity(int minCapacity)
   {
      if(minCapacity > elementData.length)
         ensureCapacityHelper(minCapacity);
   }
   private void ensureCapacityHelper(int minCapacity)
   {
      int oldCapacity = elementData.length;
      Object oldData[] = elementData;
      int newCapacity = (capacityIncrement > 0) ? (oldCapacity + capacityIncrement) : (oldCapacity * 2);
      if(newCapacity < minCapacity)
         newCapacity = minCapacity;
      elementData = new Object[newCapacity];
      System.arraycopy(oldData,0,elementData,0,elementCount);
   }
   public void setSize(int newSize)
   {
      if((newSize > elementCount) && (newSize > elementData.length))
         ensureCapacityHelper(newSize);
      else
         for(int i = newSize;i < elementCount;i++)
            elementData[i] = null;
      elementCount = newSize;
   }
   public int capacity()
   {
      return elementData.length;
   }
   public final int size()
   {
      return elementCount;
   }
   public boolean isEmpty()
   {
      return elementCount == 0;
   }
   public Enumeration elements()
   {
      return new VectorEnumerator(this);
   }
   public boolean contains(Object elem)
   {
      return indexOf(elem,0) >= 0;
   }
   public int indexOf(Object elem)
   {
      return indexOf(elem,0);
   }
   public int indexOf(Object elem, int index)
   {
      for(int i = index;i < elementCount;i++)
         if(elem.equals(elementData[i]))
            return i;
      return -1;
   }
   public int lastIndexOf(Object elem)
   {
      return lastIndexOf(elem,elementCount - 1);
   }
   public int lastIndexOf(Object elem, int index)
   {
      for(int i = index;i >= 0;i--)
         if(elem.equals(elementData[i]))
            return i;
      return -1;
   }
   public Object elementAt(int index)
   {
      return elementData[index];
   }
   public Object firstElement()
   {
      return elementData[0];
   }
   public Object lastElement()
   {
      return elementData[elementCount - 1];
   }
   public void setElementAt(Object obj, int index)
   {
      elementData[index] = obj;
      if(index >= elementCount)
         elementCount = index + 1;
   }
   public void removeElementAt(int index)
   {
      int j = elementCount - index - 1;
      if(j > 0)
         System.arraycopy(elementData,index + 1,elementData,index,j);
      if(elementCount > 0)
         elementCount--;
      elementData[elementCount] = null;
   }
   public void insertElementAt(Object obj, int index)
   {
      int newcount = elementCount + 1;
      if(newcount > elementData.length)
         ensureCapacityHelper(newcount);
      System.arraycopy(elementData,index,elementData,index + 1,elementCount - index);
      elementData[index] = obj;
      elementCount++;
   }
   public void addElement(Object obj)
   {
      int newcount = elementCount + 1;
      if(newcount > elementData.length)
         ensureCapacityHelper(newcount);
      elementData[elementCount++] = obj;
   }
   public boolean removeElement(Object obj)
   {
      int i = indexOf(obj);
      if(i >= 0)
      {
         removeElementAt(i);
         return true;
      }
      return false;
   }
   public void removeAllElements()
   {
      for(int i = 0;i < elementCount;i++)
         elementData[i] = null;
      elementCount = 0;
   }
   public String toString()
   {
      int max = size() - 1;
      StringBuffer buf = new StringBuffer();
      Enumeration e = elements();
      buf.append("[");
      for(int i = 0;i <= max;i++)
      {
         Object obj = e.nextElement();
         String s = (obj==null) ? "<null>" : obj.toString();
         buf.append(s);
         if(i < max)
            buf.append(", ");
      }
      buf.append("]");
      return buf.toString();
   }
   public Object clone()
   {
      Object[] _new = new Object[elementCount];
      System.arraycopy(elementData,0,_new,0,elementCount);
      return new Vector(_new);
   }
}
final class VectorEnumerator implements Enumeration
{
   openlink.util.Vector vector;
   int count;
   VectorEnumerator(openlink.util.Vector v)
   {
      vector = v;
      count = 0;
   }
   public boolean hasMoreElements()
   {
      return count < vector.elementCount;
   }
   public Object nextElement()
   {
      return vector.elementData[count++];
   }
}
