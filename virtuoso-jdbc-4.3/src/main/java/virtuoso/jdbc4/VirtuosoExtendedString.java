package virtuoso.jdbc4;
import java.lang.*;
import java.io.*;
import virtuoso.sql.ExtendedString;
public class VirtuosoExtendedString implements ExtendedString
{
    public String str;
    public int strType;
    public int iriType;
    public static final int IRI = ExtendedString.IRI;
    public static final int BNODE = ExtendedString.BNODE;
    public VirtuosoExtendedString (String str, int type)
    {
 this.str = str;
 this.strType = type;
 if (str.indexOf ("nodeID://") == 0)
     this.iriType = ExtendedString.BNODE;
 else
     this.iriType = ExtendedString.IRI;
    }
    public VirtuosoExtendedString (int type)
    {
 this.str = new String ();
 this.strType = type;
 this.iriType = ExtendedString.IRI;
    }
    public String toString ()
    {
 return this.str;
    }
    public int getIriType ()
    {
        return this.iriType;
    }
    public int getStrType ()
    {
        return this.strType;
    }
}
