package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

/**
 * Created by Manuel on 17/06/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter
{
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c)
    {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        Button btn_sale = (Button) view.findViewById(R.id.item_list_sale);
        btn_sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: UPLOAD DATABASE
                Log.v("InventoryCursorAdapter ", "item buttom clicked");
            }
        });

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_list_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_list_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_list_quantity);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_STOCK);

        // Read the pet attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        Double producPrice = cursor.getDouble(priceColumnIndex);
        Integer productQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText("Price: " + Double.toString(producPrice));
        quantityTextView.setText("Quantity: " + Integer.toString(productQuantity));
    }
}
