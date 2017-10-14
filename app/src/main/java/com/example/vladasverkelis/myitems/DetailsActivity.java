package com.example.vladasverkelis.myitems;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry;

import static com.example.vladasverkelis.myitems.R.id.quantity;

/**
 * Created by vladasverkelis on 29/08/2017.
 */

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the item data loader
     */
    private static final int ITEM_LOADER = 0;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentItemUri;

    /**
     * TextView field for the item's name
     */
    private TextView mNameTextView;

    /**
     * TextView field for the item's quantity
     */
    private TextView mQuantityTextView;

    /**
     * TextView field for the item's price
     */
    private TextView mPriceTextView;

    /**
     * TextView field for the item's supplier name
     */
    private TextView mSupplierTextView;

    /**
     * TextView field for the supplier's phone number
     */
    private TextView mPhoneTextView;

    /**
     * TextView field for the supplier's email
     */
    private TextView mEmailTextView;

    /**
     * ImageView field for the item's image
     */
    ImageView mImageView;

    /**Boolean flag that keeps track of weather the item has been edited (true) or not (false)*/
    private boolean mItemHasChanged = false;

    /**
     * Button field for the decreasing the quantity
     */
    Button mDecreaseButton;

    /**
     * Button field for the increasing the quantity
     */
    Button mIncreaseButton;

    /**
     * Button field for the sending an email to supplier
     */
    Button mEmailOrder;

    /**
     * Button field for the calling to supplier
     */
    Button mCallSupplier;

    /**
     *OnTouchListener that listens for any user touches on a View, implying that they are modified
     * the view, and we changed the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_view);

        //Find all relevant views that will need to read user input from
        mNameTextView = (TextView) findViewById(R.id.name);
        mQuantityTextView = (TextView) findViewById(quantity);
        mPriceTextView = (TextView) findViewById(R.id.price);
        mSupplierTextView = (TextView) findViewById(R.id.supplier_name);
        mPhoneTextView = (TextView) findViewById(R.id.supplier_phone);
        mEmailTextView = (TextView) findViewById(R.id.supplier_email);
        mDecreaseButton = (Button) findViewById(R.id.decrease_item);
        mIncreaseButton = (Button) findViewById(R.id.increase_item);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mEmailOrder = (Button) findViewById(R.id.email_order);
        mCallSupplier = (Button) findViewById(R.id.call_order);

        //Examine the intent that was used to launch this activity
        //in order to figure out if we' re creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri != null) {
            setTitle(R.string.item_details_view);
            //Kick of the loader
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }
    }

    /**
     * Updates the quantity in database by +1 and -1
     */
    private int adjustAvailability(Uri itemUri, int newCount){
        if(newCount < 0) return 0;

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, newCount);
        int numRowsUpdated = getContentResolver().update(itemUri,values, null, null);

        return numRowsUpdated;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){

        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                   //Parent activity context
                mCurrentItemUri,        //Query the content URI for the current item
                null,                   //Columns to include in the resulting Cursor
                null,                   //No selection clause
                null,                   //No selection arguments
                null                    //Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Proceed with nothing to the first row of the cursor and reading data from it
        //(This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            //Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int supplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PHONE);
            int emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);

            //Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final String phone = cursor.getString(phoneColumnIndex);
            final String email = cursor.getString(emailColumnIndex);

            //Update the views on the screen with values from the database
            mNameTextView.setText(name);
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceTextView.setText(Double.toString(price));
            mSupplierTextView.setText(supplier);
            mPhoneTextView.setText(phone);
            mEmailTextView.setText(email);
            mImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE))));

            //Decreases quantity by one
            mDecreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustAvailability(mCurrentItemUri, quantity - 1);
                }
            });

            //Increases quantity by one
            mIncreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustAvailability(mCurrentItemUri, quantity + 1);
                }
            });

            //Intent to call to supplier
            mCallSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            //Intent to send an email to supplier
            mEmailOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + email)); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, email);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "New Order");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        mNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        mSupplierTextView.setText("");
        mPhoneTextView.setText("");
        mEmailTextView.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they wnt to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener){
        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard",  discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                //User clicked the "Keep editing" button, so dismiss the dialog
                //and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/menu_editor.xml file.
        //This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     * @param  menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //If this is a new item, hide the "Delete" menu item.
        if(mCurrentItemUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()){
            //Respond to a click on the "Delete" menu option
            case R.id. action_delete:
                //Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //If the item hasn't changed, continue with navigating up to parent activity
                //which is the {@link CatalogActivity}.
                if(!mItemHasChanged){
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                //Otherwise if there are unsaved changes, setup a dialog to warn the user.
                //Create a click listener to handle the user confirming that
                //changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i){
                                //User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Promt the user to confirm that they want to delete this item
     */
    public void showDeleteConfirmationDialog(){
        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                //User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                //User clicked the "Cancel" button, so dismiss the dialog
                //and continue editing the item.
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem(){
        //Only perform the delete if this is an existing item.
        if(mCurrentItemUri != null){
            //Call the ContentResolver to delete the item at the given content URI.
            //Pass in null for the selection and selection args because the mCurrentItemUri
            //content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            //Show a toast message depending on weather or not the delete was successful.
            if(rowsDeleted == 0){
                //If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Error with deleting item",
                        Toast.LENGTH_SHORT).show();
            }else {
                //Otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, "Item deleted",
                        Toast.LENGTH_LONG).show();
            }
        }
        //Close the activity
        finish();
    }
}
