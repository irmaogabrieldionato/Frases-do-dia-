package com.orbis.meudia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure-Java, offline aggregation for future per-task timer and stopwatch history.
 * This does not persist sessions or schedule alarms. Times are milliseconds.
 */
public final class TaskTimeHistory {
    private TaskTimeHistory() {}

    public enum Kind { TIMER, STOPWATCH }

    public static final class Session {
        public final String id;
        public final String taskId;
        public final String occurrenceDate; // ISO yyyy-MM-dd, task occurrence date
        public final Kind kind;
        public final long elapsedMillis;
        public final long targetMillis; // timer target; zero for stopwatch

        public Session(String id, String taskId, String occurrenceDate, Kind kind,
                       long elapsedMillis, long targetMillis) {
            if (blank(id) || blank(taskId) || occurrenceDate == null
                    || !occurrenceDate.matches("\\d{4}-\\d{2}-\\d{2}")
                    || kind == null || elapsedMillis < 0 || targetMillis < 0
                    || (kind == Kind.STOPWATCH && targetMillis != 0)) {
                throw new IllegalArgumentException("Invalid time session");
            }
            try {
                int year = Integer.parseInt(occurrenceDate.substring(0, 4));
                if (year == 0) throw new IllegalArgumentException("Year zero is not an ISO calendar year");
                int month = Integer.parseInt(occurrenceDate.substring(5, 7));
                int day = Integer.parseInt(occurrenceDate.substring(8, 10));
                java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
                calendar.setLenient(false);
                // ISO dates use the proleptic Gregorian calendar, including historical dates.
                calendar.setGregorianChange(new java.util.Date(Long.MIN_VALUE));
                calendar.clear();
                calendar.set(year, month - 1, day);
                calendar.getTimeInMillis();
            } catch (IllegalArgumentException error) {
                throw new IllegalArgumentException("Invalid occurrence date", error);
            }
            this.id = id;
            this.taskId = taskId;
            this.occurrenceDate = occurrenceDate;
            this.kind = kind;
            this.elapsedMillis = elapsedMillis;
            this.targetMillis = targetMillis;
        }
    }

    public static final class Day {
        public final String date;
        public final long stopwatchMillis;
        public final long timerElapsedMillis;
        public final long timerTargetMillis;
        public final int stopwatchSessions;
        public final int timerSessions;

        private Day(String date, long stopwatchMillis, long timerElapsedMillis,
                    long timerTargetMillis, int stopwatchSessions, int timerSessions) {
            this.date = date;
            this.stopwatchMillis = stopwatchMillis;
            this.timerElapsedMillis = timerElapsedMillis;
            this.timerTargetMillis = timerTargetMillis;
            this.stopwatchSessions = stopwatchSessions;
            this.timerSessions = timerSessions;
        }

        /** Actual recorded work; timer targets are never counted as elapsed time. */
        public long workedMillis() {
            return Math.addExact(stopwatchMillis, timerElapsedMillis);
        }
    }

    /** Totals for one task, keeping timer target separate from actual elapsed time. */
    public static final class Total {
        public final int activeDays;
        public final long stopwatchMillis;
        public final long timerElapsedMillis;
        public final long timerTargetMillis;
        public final long stopwatchSessions;
        public final long timerSessions;

        private Total(int activeDays, long stopwatchMillis, long timerElapsedMillis,
                      long timerTargetMillis, long stopwatchSessions, long timerSessions) {
            this.activeDays = activeDays;
            this.stopwatchMillis = stopwatchMillis;
            this.timerElapsedMillis = timerElapsedMillis;
            this.timerTargetMillis = timerTargetMillis;
            this.stopwatchSessions = stopwatchSessions;
            this.timerSessions = timerSessions;
        }

        /** Actual recorded work across the selected dates, excluding timer targets. */
        public long workedMillis() {
            return Math.addExact(stopwatchMillis, timerElapsedMillis);
        }
    }

    /** Aggregate only recorded days. No timer target is counted as worked time. */
    public static Total totalForTask(List<Session> sessions, String taskId) {
        List<Day> days = byDate(sessions, taskId);
        long stopwatch = 0, timerElapsed = 0, timerTarget = 0;
        long stopwatchCount = 0, timerCount = 0;
        for (Day day : days) {
            stopwatch = Math.addExact(stopwatch, day.stopwatchMillis);
            timerElapsed = Math.addExact(timerElapsed, day.timerElapsedMillis);
            timerTarget = Math.addExact(timerTarget, day.timerTargetMillis);
            stopwatchCount = Math.addExact(stopwatchCount, day.stopwatchSessions);
            timerCount = Math.addExact(timerCount, day.timerSessions);
        }
        return new Total(days.size(), stopwatch, timerElapsed, timerTarget,
                stopwatchCount, timerCount);
    }

    /** Aggregate a task only inside an inclusive date range, without counting timer targets as worked time. */
    public static Total totalBetweenDates(List<Session> sessions, String taskId,
                                          String startDate, String endDate) {
        List<Day> days = betweenDates(sessions, taskId, startDate, endDate);
        long stopwatch = 0, elapsed = 0, target = 0, stopwatchCount = 0, timerCount = 0;
        for (Day day : days) {
            stopwatch = Math.addExact(stopwatch, day.stopwatchMillis);
            elapsed = Math.addExact(elapsed, day.timerElapsedMillis);
            target = Math.addExact(target, day.timerTargetMillis);
            stopwatchCount = Math.addExact(stopwatchCount, day.stopwatchSessions);
            timerCount = Math.addExact(timerCount, day.timerSessions);
        }
        return new Total(days.size(), stopwatch, elapsed, target, stopwatchCount, timerCount);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean validOccurrenceDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            int year = Integer.parseInt(date.substring(0, 4));
            if (year == 0) return false;
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
            calendar.setLenient(false);
            // ISO dates use the proleptic Gregorian calendar, including historical dates.
            calendar.setGregorianChange(new java.util.Date(Long.MIN_VALUE));
            calendar.clear();
            calendar.set(year, month - 1, day);
            calendar.getTimeInMillis();
            return true;
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    /** Returns individual sessions for exactly one task occurrence, in original order. */
    public static List<Session> forOccurrence(List<Session> sessions,
                                               String taskId, String occurrenceDate) {
        if (sessions == null || blank(taskId) || !validOccurrenceDate(occurrenceDate)) {
            throw new IllegalArgumentException("Missing input");
        }
        List<Session> result = new ArrayList<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Session session : sessions) {
            if (session == null) throw new IllegalArgumentException("Null session");
            if (taskId.equals(session.taskId)) {
                if (!seen.add(session.id)) throw new IllegalArgumentException("Duplicate session id");
                if (occurrenceDate.equals(session.occurrenceDate)) result.add(session);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** Inclusive date-range view for the future weekly/monthly history screen. */
    public static List<Day> betweenDates(List<Session> sessions, String taskId,
                                          String startDate, String endDate) {
        if (!validOccurrenceDate(startDate) || !validOccurrenceDate(endDate)
                || startDate.compareTo(endDate) > 0) {
            throw new IllegalArgumentException("Invalid date range");
        }
        List<Day> result = new ArrayList<>();
        for (Day day : byDate(sessions, taskId)) {
            if (day.date.compareTo(startDate) >= 0 && day.date.compareTo(endDate) <= 0) {
                result.add(day);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** Remove exactly one recorded session by ID, without changing any other day or task. */
    public static List<Session> withoutSession(List<Session> sessions, String sessionId) {
        if (sessions == null || blank(sessionId)) throw new IllegalArgumentException("Missing input");
        List<Session> result = new ArrayList<>();
        boolean found = false;
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Session session : sessions) {
            if (session == null) throw new IllegalArgumentException("Null session");
            if (!seen.add(session.id)) throw new IllegalArgumentException("Duplicate session id");
            if (sessionId.equals(session.id)) {
                if (found) throw new IllegalArgumentException("Duplicate session id");
                found = true;
            } else {
                result.add(session);
            }
        }
        if (!found) throw new IllegalArgumentException("Session not found");
        return Collections.unmodifiableList(result);
    }

    /** Groups a task's sessions by occurrence date; does not mix other tasks. */
    public static List<Day> byDate(List<Session> sessions, String taskId) {
        if (sessions == null || blank(taskId)) throw new IllegalArgumentException("Missing input");
        Map<String, long[]> grouped = new java.util.TreeMap<>();
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (Session session : sessions) {
            if (session == null) throw new IllegalArgumentException("Null session");
            if (!taskId.equals(session.taskId)) continue;
            if (!seen.add(session.id)) throw new IllegalArgumentException("Duplicate session id");
            long[] values = grouped.get(session.occurrenceDate);
            if (values == null) {
                values = new long[5];
                grouped.put(session.occurrenceDate, values);
            }
            if (session.kind == Kind.STOPWATCH) {
                values[0] = Math.addExact(values[0], session.elapsedMillis);
                values[3] = Math.addExact(values[3], 1);
            } else {
                values[1] = Math.addExact(values[1], session.elapsedMillis);
                values[2] = Math.addExact(values[2], session.targetMillis);
                values[4] = Math.addExact(values[4], 1);
            }
        }
        List<Day> result = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : grouped.entrySet()) {
            long[] v = entry.getValue();
            result.add(new Day(entry.getKey(), v[0], v[1], v[2],
                    Math.toIntExact(v[3]), Math.toIntExact(v[4])));
        }
        return Collections.unmodifiableList(result);
    }
}
