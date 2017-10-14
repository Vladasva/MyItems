package com.example.vladasverkelis.myitems;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry;
import com.example.vladasverkelis.myitems.data.ItemDbHelper;


/**
 * Created by vladasverkelis on 01/08/2017.
 */
/**
 * Displays lit of items that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the item data loader
     */
    private static final int ITEM_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ItemCursorAdapter mCursorAdapter;

    ItemDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        dbHelper = new ItemDbHelper(this);

        // Setup FAB to open EditorActivity
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView which will be populated with item data
        ListView itemListView = (ListView) findViewById(R.id.list);

        //Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        mCursorAdapter = new ItemCursorAdapter(this, null);

        //Setup an Adapter to create a list item for each row of item data in the Cursor
        //There is no item data yet (until the loader finishes) so pass in null for the cursor.
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        //Setup the item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@link Editor activity}
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);

                //Form the content URI that represents the specific item that was clicked on,
                //by appending the "id" (passed as input to this method) onto the
                //{@link ItemEntry#CONTENT_URI}.
                //For example, the URI would be "content://com.example.android.items/items/2"
                //if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                //Launch the {@link EditorActivity} to display the data for the current item.
                startActivity(intent);
            }
        });

        //Kick of the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCursorAdapter.swapCursor(dbHelper.readItem());
    }

    /**
     * Helper method to delete all items in the database
     */
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from the item database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/menu_catalog.xml file.
        //This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemEntry.COLUMN_ITEM_PHONE,
                ItemEntry.COLUMN_ITEM_EMAIL};

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,      //Parent activity context
                ItemEntry.CONTENT_URI,      //Provider content URI to query
                projection,                 //Columns to include in the resulting Cursor
                null,                       //No selection close
                null,                       //No selection close
                null);                      //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update {@link ItemCursorAdapter} with this new cursor containing updated per data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}



