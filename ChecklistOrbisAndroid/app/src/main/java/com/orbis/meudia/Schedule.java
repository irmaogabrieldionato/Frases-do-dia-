package com.orbis.meudia;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/** Occurrence rules. History is stored on each task, keyed by yyyy-MM-dd. */
final class Schedule {
    static Calendar date(String key) {
        try {
            String[] parts = key.split("-");
            if (parts.length != 3) throw new IllegalArgumentException();
            Calendar c = new GregorianCalendar(Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            c.setLenient(false);
            c.getTime();
            return c;
        } catch (Exception ignored) {
            return Calendar.getInstance();
        }
    }

    static String key(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1,
                c.get(Calendar.DAY_OF_MONTH));
    }

    static int dayDiff(Calendar start, Calendar end) {
        Calendar a=(Calendar)start.clone(), b=(Calendar)end.clone();
        a.set(Calendar.HOUR_OF_DAY,12); a.set(Calendar.MINUTE,0);
        a.set(Calendar.SECOND,0); a.set(Calendar.MILLISECOND,0);
        b.set(Calendar.HOUR_OF_DAY,12); b.set(Calendar.MINUTE,0);
        b.set(Calendar.SECOND,0); b.set(Calendar.MILLISECOND,0);
        if(a.after(b))return -dayDiff(end,start);
        int days=(int)((b.getTimeInMillis()-a.getTimeInMillis())/86400000L);
        a.add(Calendar.DAY_OF_YEAR, days);
        while(a.before(b)){a.add(Calendar.DAY_OF_YEAR,1);days++;}
        while(a.after(b)){a.add(Calendar.DAY_OF_YEAR,-1);days--;}
        return days;
    }

    static boolean occurs(JSONObject task, String key) {
        if(task==null)return false;
        String start=task.optString("start","");
        if(start.isEmpty() || key.compareTo(start)<0)return false;
        String until=task.optString("until","");
        if(!until.isEmpty() && key.compareTo(until)>0)return false;
        JSONArray excluded=task.optJSONArray("skips");
        if(excluded!=null)for(int i=0;i<excluded.length();i++)
            if(key.equals(excluded.optString(i)))return false;

        Calendar first=date(start), day=date(key);
        String mode=task.optString("mode","daily");
        int ordinal;
        switch(mode){
            case "once":
                if(!start.equals(key))return false;
                ordinal=1; break;
            case "daily":
                ordinal=dayDiff(first,day)+1; break;
            case "weekly":
                int mask=task.optInt("weekdays",0);
                if(mask==0)mask=1<<(first.get(Calendar.DAY_OF_WEEK)-1);
                if((mask & (1<<(day.get(Calendar.DAY_OF_WEEK)-1)))==0)return false;
                int delta=dayDiff(first,day);
                int full=delta/7;
                ordinal=full*Integer.bitCount(mask&127);
                Calendar probe=(Calendar)first.clone();
                for(int i=0;i<=delta%7;i++){
                    if((mask & (1<<(probe.get(Calendar.DAY_OF_WEEK)-1)))!=0)ordinal++;
                    probe.add(Calendar.DAY_OF_YEAR,1);
                }
                break;
            case "monthly":
                int months=(day.get(Calendar.YEAR)-first.get(Calendar.YEAR))*12+
                        day.get(Calendar.MONTH)-first.get(Calendar.MONTH);
                if(months<0 || day.get(Calendar.DAY_OF_MONTH)!=
                        Math.min(first.get(Calendar.DAY_OF_MONTH),
                            day.getActualMaximum(Calendar.DAY_OF_MONTH)))return false;
                ordinal=months+1; break;
            case "yearly":
                if(day.get(Calendar.YEAR)<first.get(Calendar.YEAR) ||
                    day.get(Calendar.MONTH)!=first.get(Calendar.MONTH) ||
                    day.get(Calendar.DAY_OF_MONTH)!=Math.min(first.get(Calendar.DAY_OF_MONTH),
                        day.getActualMaximum(Calendar.DAY_OF_MONTH)))return false;
                ordinal=day.get(Calendar.YEAR)-first.get(Calendar.YEAR)+1; break;
            default:return false;
        }
        int count=task.optInt("count",0);
        return ordinal>0 && (count<=0 || ordinal<=count);
    }

    static String description(JSONObject task, String once, String daily,
                              String weekly, String monthly, String yearly){
        switch(task.optString("mode","once")){
            case "daily":return daily;
            case "weekly":return weekly;
            case "monthly":return monthly;
            case "yearly":return yearly;
            default:return once;
        }
    }
}
