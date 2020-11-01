package virtuoso.jdbc4;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.*;
public class VirtuosoDate extends java.sql.Date
{
    int timezone = 0;
    boolean with_timezone = false;
    public VirtuosoDate(long date) {
 super(date);
    }
    public VirtuosoDate(long date, int tz) {
 super(date);
 this.timezone = tz;
 this.with_timezone = true;
    }
    public VirtuosoDate(long date, int tz, boolean with_tz) {
 super(date);
 this.timezone = tz;
 this.with_timezone = with_tz;
    }
    public boolean withTimezone()
    {
     return this.with_timezone;
    }
    public int getTimezone()
    {
        return this.timezone;
    }
    public VirtuosoDate clone()
    {
        return new VirtuosoDate(getTime(), timezone, with_timezone);
    }
    public String toXSD_String ()
    {
        StringBuilder sb = new StringBuilder();
        DateFormat formatter;
        String timeZoneString = null;
        java.util.Calendar cal = new java.util.GregorianCalendar ();
        cal.setTime(this);
        if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
            sb.append('-');
            formatter = new SimpleDateFormat("yyy-MM-dd");
        }
        else
            formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (with_timezone)
        {
            StringBuilder s = new StringBuilder();
            s.append(timezone>0?'+':'-');
            int tz = Math.abs(timezone);
            int tzh = tz/60;
            int tzm = tz%60;
            if (tzh < 10)
                s.append('0');
            s.append(tzh);
            s.append(':');
            if (tzm < 10)
                s.append('0');
            s.append(tzm);
            timeZoneString = s.toString();
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"+timeZoneString));
        }
        sb.append(formatter.format(this));
        if (with_timezone && timezone!=0)
            sb.append(timeZoneString);
        return sb.toString();
    }
}
