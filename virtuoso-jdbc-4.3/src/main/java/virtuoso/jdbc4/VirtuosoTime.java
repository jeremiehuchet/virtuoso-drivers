package virtuoso.jdbc4;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.*;
public class VirtuosoTime extends java.sql.Time
{
    int timezone = 0;
    boolean with_timezone = false;
    public VirtuosoTime(long time) {
 super(time);
    }
    public VirtuosoTime(long time, int tz) {
 super(time);
 this.timezone = tz;
 this.with_timezone = true;
    }
    public VirtuosoTime(long time, int tz, boolean with_tz) {
 super(time);
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
    public VirtuosoTime clone()
    {
        return new VirtuosoTime(getTime(), timezone, with_timezone);
    }
    public String toXSD_String ()
    {
        StringBuilder sb = new StringBuilder();
        DateFormat formatter= new SimpleDateFormat("HH:mm:ss.SSS");
        String timeZoneString = null;
        if (with_timezone)
        {
            StringBuffer s = new StringBuffer();
            if (timezone == 0) {
              timeZoneString = "Z";
              formatter.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
            } else {
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
        }
        sb.append(formatter.format(this));
        if (timeZoneString!=null)
            sb.append(timeZoneString);
        return sb.toString();
    }
}
