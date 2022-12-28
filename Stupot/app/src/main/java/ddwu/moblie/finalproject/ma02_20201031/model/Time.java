package ddwu.moblie.finalproject.ma02_20201031.model;

public class Time {
    public int hour;
    public int minute;
    public int second;

    public Time() { hour = minute = second = 0; }

    public Time(int time) {
        second = time % 100; time /= 100;
        minute = time % 100; time /= 100;
        hour = time;
    }

    public Time add(Time time) {
        Time result = new Time();

        int tmp = this.second + time.second;
        result.second = tmp % 60; tmp /= 60;

        tmp += this.minute + time.minute;
        result.minute = tmp % 60; tmp /= 60;

        tmp += this.hour + time.hour;
        result.hour = tmp;

        return result;
    }

    public int toInteger() {
        int result = 0;

        result += hour; result *= 100;
        result += minute; result *= 100;
        result += second;

        return result;
    }

    public String toString() {
        return String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second);
    }
}
