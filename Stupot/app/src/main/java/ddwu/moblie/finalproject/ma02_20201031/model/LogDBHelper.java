package ddwu.moblie.finalproject.ma02_20201031.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogDBHelper extends SQLiteOpenHelper {

    private final String TAG = "LogDBHelper";

    public final static String DB_NAME = "log_db";
    public final static String TABLE_NAME = "log_table";

    public final static String COL_PLACEID = "pid";
    public final static String COL_DATE = "log_date";
    public final static String COL_TIME = "log_time";

    public LogDBHelper(Context context) { super(context, DB_NAME, null, 12); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = " CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( _id integer primary key autoincrement, "
                + COL_PLACEID + " TEXT, " + COL_DATE + " DATE, " + COL_TIME + " integer);";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.disableWriteAheadLogging();
    }
}