package virtuoso.jdbc4;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.sql.*;
public class VirtuosoPoint
{
    public double x;
    public double y;
    public VirtuosoPoint (double _x, double _y)
    {
     this.x = _x;
     this.y = _y;
    }
    public VirtuosoPoint (String data) throws IllegalArgumentException
    {
        if (data == null)
            throw new IllegalArgumentException();
        StringTokenizer strtok = new StringTokenizer(data," ");
        if (strtok.hasMoreTokens()) {
            this.x = Double.parseDouble(strtok.nextToken());
            if (strtok.hasMoreTokens())
                this.y = Double.parseDouble(strtok.nextToken());
            else
                throw new IllegalArgumentException();
        } else {
            throw new IllegalArgumentException();
        }
    }
    public String toString ()
    {
     return "POINT("+x+" "+y+")";
    }
}
