package ddwu.moblie.finalproject.ma02_20201031.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogDBHelper extends SQLiteOpenHelper {

    private final String TAG = "LogDBHelper";

    private final static String DB_NAME = "log_db";
    public final static String TABLE_NAME = "log_table";

    public final static String COL_PLACEID = "pid";
    public final static String COL_DATE = "date";
    public final static String COL_TIME = "time";

    public LogDBHelper(Context context) { super(context, DB_NAME, null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table " + TABLE_NAME + "( _id INTEGER primary key autoincrement,"
                + COL_PLACEID + " INTEGER, " + COL_DATE + " DATE, " + COL_TIME + " INTEGER);";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}