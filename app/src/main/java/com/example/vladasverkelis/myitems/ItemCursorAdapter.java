package com.example.vladasverkelis.myitems;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.vladasverkelis.myitems.data.ItemContract.ItemEntry;

import static com.example.vladasverkelis.myitems.R.id.quantity;

/**
 * Created by vladasverkelis on 01/08/2017.
 */

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c      The cursor from which to get the data
     */
    public ItemCursorAdapter(CatalogActivity context, Cursor c){
        super(context, c, 0 /*flags*/);
    }


        /**
         * Makes a new blank list item view. No data is set (or bound) to views yet.
         *
         * @param context app context
         * @param cursor The cursor from which to get data. The cursor is already
         *               moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created list item view.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //Inflate a list item view using the layout specified in the list_item.xml
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent,false);
        }

        /**
         * This method binds the item data (in the current row pointed to by cursor) to the given
         *list item layout. For example, the name for the current item can be set on the name TextView
         * int the list item layout.
         *
         * @param view Existing view, returned earlier by newView() method
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct row.
         */
        @Override
        public void bindView(View view, final Context context, Cursor cursor) {
            //Find individual views that we want modify in the list item layout
            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            TextView quantityTextView = (TextView) view.findViewById(quantity);
            TextView priceTextView = (TextView) view.findViewById(R.id.price);
            Button decreaseButton = (Button) view.findViewById(R.id.decrease_item);

            //Find the columns of the item attributes that we' re interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);

            //Read the item attributes from the Cursor for the current item
            String itemName = cursor.getString(nameColumnIndex);
            String itemQuantity = cursor.getString(quantityColumnIndex);
            String itemPrice = cursor.getString(priceColumnIndex);

            final long id = cursor.getLong(cursor.getColumnIndex(ItemEntry._ID));


            //If the item quantity string is empty or null, then use some default text
            //thats say "0", so the TextView isn't blank
            if (TextUtils.isEmpty(itemQuantity)) {
                itemQuantity = "0";
            }

            //If the item price string is empty or null, then use some default text
            //thats say "0", so the TextView isn't blank
            if (TextUtils.isEmpty(itemPrice)) {
                itemPrice = "0";
            }

            //Update the TextViews with attributes for the current item
            nameTextView.setText(itemName);
            quantityTextView.setText(itemQuantity);
            priceTextView.setText(itemPrice);

            final int itemId = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
            final int count = cursor.getInt(cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY));

            //Set a click listener on the "Decrease" button
            decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri itemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);
                    decreaseItemByOne(context, itemUri, count);
                }
            });
        }
            //Methods what decreases the quantity by one and updates database
            private void decreaseItemByOne(Context context, Uri itemUri, int currentCount) {
                int newCount = (currentCount >= 1) ? currentCount - 1 : 0;

                ContentValues values = new ContentValues();
                values.put(ItemEntry.COLUMN_ITEM_QUANTITY, newCount);
                int numRowsUpdated = context.getContentResolver().update(itemUri,values, null, null);
            }
        }








