package com.example.vladasverkelis.myitems.data;

/**
 * Created by vladasverkelis on 01/08/2017.
 */


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry;

import static com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry.TABLE_NAME;

/**
 * Database helper for MyItems app. Manages database creation and version management.
 */
public class ItemDbHelper extends SQLiteOpenHelper{

    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    /**Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper};
     *
     * @param context of the app
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        //CREATE TABLE items (id INTEGER PRIMARY KEY, name TEXT, image TEXT);
        //Create a String that contains the SQL statement to create items table
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + "("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_QUANTITY  + " INTEGER DEFAULT 0, "
                + ItemEntry.COLUMN_ITEM_PRICE + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_SUPPLIER + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PHONE + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_EMAIL + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_IMAGE + " TEXT NOT NULL);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Insert a hardcoded item data into the database.
     */
    public void insertItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, item.getName());
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, item.getQuantity());
        values.put(ItemEntry.COLUMN_ITEM_PRICE, item.getPrice());
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER, item.getSupplier());
        values.put(ItemEntry.COLUMN_ITEM_PHONE, item.getPhone());
        values.put(ItemEntry.COLUMN_ITEM_EMAIL, item.getEmail());
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, item.getImage());
        long id = db.insert(TABLE_NAME, null, values);
    }

    public Cursor readItem() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PHONE,
                ItemEntry.COLUMN_ITEM_EMAIL,
                ItemEntry.COLUMN_ITEM_IMAGE
        };

        Cursor cursor = db.query(
                ItemEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return cursor;
    }

    public Cursor readInventory(long itemId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PHONE,
                ItemEntry.COLUMN_ITEM_EMAIL,
                ItemEntry.COLUMN_ITEM_IMAGE
        };
        String selection = ItemEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(itemId) };

        Cursor cursor = db.query(
                ItemEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        return cursor;
    }

    public void updateItem(long currentItemId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        String selection = ItemEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(currentItemId) };
        db.update(TABLE_NAME,
                values, selection, selectionArgs);
    }
}
