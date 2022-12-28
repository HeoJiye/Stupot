package ddwu.moblie.finalproject.ma02_20201031.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlaceDBHelper extends SQLiteOpenHelper {

    private final String TAG = "PlaceDBHelper";

    public final static String DB_NAME = "place_database";
    public final static String TABLE_NAME = "place__table";

    public final static String COL_PLACEID = "_id";
    public final static String COL_NAME = "place_name";
    public final static String COL_LATITUDE = "place_latitude";
    public final static String COL_LONGITUDE = "place_longitude";

    public PlaceDBHelper(Context context) { super(context, DB_NAME, null, 14); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table " + TABLE_NAME + "( " + COL_PLACEID + " TEXT primary key, "
                + COL_NAME + " TEXT, " + COL_LATITUDE + " REAL, " + COL_LONGITUDE + " REAL);";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
