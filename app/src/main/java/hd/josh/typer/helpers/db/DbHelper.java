package hd.josh.typer.helpers.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import hd.josh.typer.R;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";
    private static final String DATABASE_NAME = "typer.db";
    private static final int DATABASE_VERSION = 4;

    public static final String POSTS_TABLE = "posts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COLOR = "color";

    private static DbHelper singleton = null;
    private static Context mContext;

    public static DbHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new DbHelper(context.getApplicationContext());
        }
        return singleton;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +POSTS_TABLE+ " ("
                +COLUMN_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +COLUMN_TITLE+ " TEXT, "
                +COLUMN_TEXT+  " TEXT, "
                +COLUMN_DATE+  " TEXT, "
                +COLUMN_COLOR+  " TEXT)"
        );

        initIntroPosts(db);
    }

    public void initIntroPosts(SQLiteDatabase db) {
        final String[] introTitles = mContext.getResources().getStringArray(R.array.intro_titles);
        final String[] introBodies = mContext.getResources().getStringArray(R.array.intro_bodies);
        final String[] introColors = mContext.getResources().getStringArray(R.array.intro_colors);
        final String introVersion = mContext.getResources().getStringArray(R.array.update_versions)[0];

        ContentValues values = new ContentValues();
        for (int i = introTitles.length-1; i >= 0; i--) {
            values.put(DbHelper.COLUMN_TITLE, introTitles[i]);
            values.put(DbHelper.COLUMN_TEXT, introBodies[i]);
            values.put(DbHelper.COLUMN_COLOR, introColors[i]);
            values.put(DbHelper.COLUMN_DATE, introVersion);
            db.insert(DbHelper.POSTS_TABLE, null, values);
            values.clear();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " +POSTS_TABLE);
//        onCreate(db);

        final String title = "New Updates!";
        final String latestVersion = mContext.getResources().getStringArray(R.array.update_versions)[0];
        final String latestUpdate = mContext.getResources().getStringArray(R.array.update_text)[0];
        ContentValues values = new ContentValues();

        values.put(DbHelper.COLUMN_TITLE, title);
        values.put(DbHelper.COLUMN_TEXT, latestUpdate);
        values.put(DbHelper.COLUMN_COLOR, "red");
        values.put(DbHelper.COLUMN_DATE, latestVersion);

        db.insert(DbHelper.POSTS_TABLE, null, values);
        values.clear();

        switch(oldVersion) {
            case 1: //0.4b
            case 2: //0.5b
            case 3: //0.5.1b
            case 4: //0.5.2b
            case 5: //
                break;
            default:
                throw new IllegalStateException(
                        "Upgrading to unknown version: " + newVersion);
        }
    }
}
