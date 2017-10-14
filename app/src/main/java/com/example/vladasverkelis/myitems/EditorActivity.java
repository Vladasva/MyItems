package com.example.vladasverkelis.myitems;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.vladasverkelis.myitems.data.Item;
import com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry;
import com.example.vladasverkelis.myitems.data.ItemDbHelper;

/**
 * Created by vladasverkelis on 01/08/2017.
 */

/**
 * Allows user to crate a new item or edit existing one
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for selection of file
     */
    private static final int PICK_IMAGE_REQUEST = 0;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private ItemDbHelper dbHelper;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    Uri mCurrentItemUri;

    /**
     * EditText field to enter the item's name
     */
    private EditText mNameEditText;

    /**
     * EditText text field to enter the item's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EdiText field to enter item's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the item's supplier
     */
    private EditText mSupplierEditText;

    /**
     * EditText field to enter the supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * EditText field to enter the supplier's email
     */
    private EditText mEmailEditText;

    /**
     * EditText field to enter the item's image
     */
    ImageView mImageView;

    long currentItemId;

    /**
     * Button field for uploading an image
     */
    private Button mImageButton;

    /**Boolean flag that keeps track of weather the item has been edited (true) or not (false)*/
    private boolean mItemHasChanged = false;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity
        //in order to figure out if we' re creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //If the intent DOES NOT contain an item content URI, then we know that we are
        //creating a new item.

            //Find all relevant views that will need to read user input from
            mNameEditText= (EditText) findViewById(R.id.edit_product);
            mQuantityEditText =(EditText) findViewById(R.id.edit_quantity);
            mPriceEditText = (EditText) findViewById(R.id.edit_price);
            mSupplierEditText = (EditText) findViewById(R.id.edit_supplier);
            mPhoneEditText = (EditText) findViewById(R.id.edit_phone);
            mEmailEditText = (EditText) findViewById(R.id.edit_email);
            mImageButton = (Button) findViewById(R.id.upload_image);
            mImageView = (ImageView) findViewById(R.id.image_view);

            dbHelper = new ItemDbHelper(this);
            currentItemId = getIntent().getLongExtra("itemId", 0);

        if (currentItemId == 0) {
            setTitle(getString(R.string.editor_activity_title_new_item));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
        }

        mImageButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                tryToOpenImageSelector();
                mItemHasChanged = true;
            }
        });

    }

    /**
     * This method is called when the back button is pressed
     */

    @Override
    public void onBackPressed() {
        //If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
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
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()){
            //Respond to a click on the "Save" menu option
            case R.id.action_save:
                //Save item to database
                if(!addItem()){
                    return  true;
                };//
                //Exit Activity
                finish();
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
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                        //Show a dialog that notifies the user they have unsaved changes
                        showUnsavedChangesDialog(discardButtonClickListener);
                        return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean addItem() {
        boolean isOk = true;
        if (!checkIfValueSet(mNameEditText, "name")) {
            isOk = false;
        }
        if (!checkIfValueSet(mQuantityEditText, "quantity")) {
            isOk = false;
        }
        if (!checkIfValueSet(mPriceEditText, "price")) {
            isOk = false;
        }
        if (!checkIfValueSet(mSupplierEditText, "supplier")) {
            isOk = false;
        }
        if (!checkIfValueSet(mPhoneEditText, "phone")) {
            isOk = false;
        }
        if (!checkIfValueSet(mEmailEditText, "email")) {
            isOk = false;
        }

        if (mCurrentItemUri == null && currentItemId == 0) {
            isOk = false;
            mImageButton.setError("Missing image");
        }
        if (!isOk) {
            return false;
        }

        if (currentItemId == 0) {
            Item item = new Item(
                    // Read from input fields
                    // Use trim to eliminate leading or trailing white space
                    mNameEditText.getText().toString().trim(),
                    mPriceEditText.getText().toString().trim(),
                    Integer.parseInt(mQuantityEditText.getText().toString().trim()),
                    mSupplierEditText.getText().toString().trim(),
                    mPhoneEditText.getText().toString().trim(),
                    mEmailEditText.getText().toString().trim(),
                    mCurrentItemUri.toString());
            dbHelper.insertItem(item);
        } else {
            int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            dbHelper.updateItem(currentItemId, quantity);
        }
        return true;
    }

    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError("Missing product " + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
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
                null
        );                              //Default sort order
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
        mEmailEditText.setText("");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){


            //Proceed with nothing to the first row of the cursor and reading data from it
            //(This should be the only row in the cursor)
            cursor.moveToFirst();
                //Find the columns of item attributes that we're interested in
                int nameColumnIndex =  cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
                int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
                int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
                int supplierColumnIndex =  cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER);
                int phoneColumnIndex =  cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PHONE);
                int emailColumnIndex =  cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL);
                //Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                final int quantity = cursor.getInt(quantityColumnIndex);
                double price = cursor.getDouble(priceColumnIndex);
                String supplier = cursor.getString(supplierColumnIndex);
                String phone = cursor.getString(phoneColumnIndex);
                String email = cursor.getString(emailColumnIndex);

                //Update the views on the screen with values from the database
                mNameEditText.setText(name);
                mQuantityEditText.setText(Integer.toString(quantity));
                mPriceEditText.setText(Double.toString(price));
                mSupplierEditText.setText(supplier);
                mPhoneEditText.setText(phone);
                mEmailEditText.setText(email);
                mImageView.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE))));

    }

    /**
     * Show a dialog that warns  the user there are unsaved changes that will be lost
     * if they continue leaving the editor
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they wnt to discard their  changes
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

    /**
     * Promt the user to confirm that they want to delete this item
     */
    private void showDeleteConfirmationDialog(){
        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                //User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
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


    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mCurrentItemUri = resultData.getData();
                mImageView.setImageURI(mCurrentItemUri);
                mImageView.invalidate();
            }
        }
    }

}




