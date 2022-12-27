package ddwu.moblie.finalproject.ma02_20201031.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaceDBHelper extends SQLiteOpenHelper {

    private final String TAG = "PlaceDBHelper";

    private final static String DB_NAME = "place_db";
    public final static String TABLE_NAME = "place_table";

    public final static String COL_NAME = "name";
    public final static String COL_TYPE = "type";
    public final static String COL_ADDRESS = "address";
    public final static String COL_LATITUDE = "latitude";
    public final static String COL_LONGITUDE = "longitude";

    public PlaceDBHelper(Context context) { super(context, DB_NAME, null, 1); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table " + TABLE_NAME + "( _id integer primary key autoincrement,"
                + COL_NAME + " TEXT, " + COL_TYPE + " TEXT, " + COL_ADDRESS + " TEXT, "
                + COL_LATITUDE + "FLOAT, " + COL_LONGITUDE + "FLOAT);";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
