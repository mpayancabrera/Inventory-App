package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.CursorLoader;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import com.example.android.inventoryapp.data.InventoryContract;

import org.w3c.dom.Text;

import static android.R.attr.name;
import static android.R.id.message;

/**
 * Created by Manuel on 17/06/2017.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing product */
    private Uri mCurrentProductUri;

    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView mImageView;
    private TextView mDescView;
    private TextView mStockView;
    private TextView mPriceView;
    private ImageButton mButtonLess;
    private ImageButton mButtonMore;
    private Button mButtonOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("");

        mImageView = (ImageView) findViewById(R.id.backdrop);
        mDescView = (TextView) findViewById(R.id.info_desc);
        mStockView = (TextView) findViewById(R.id.info_stock);
        mPriceView = (TextView) findViewById(R.id.info_price);
        mButtonLess = (ImageButton) findViewById(R.id.bt_less);
        mButtonMore = (ImageButton) findViewById(R.id.bt_more);
        mButtonOrder = (Button) findViewById(R.id.bt_order);

        // Initialize a loader to read the product data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.detail_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        mButtonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use an intent to launch an email app.
                // Send the order summary in the email body.
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.order_summary_email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mButtonLess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });

        mButtonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the detail view shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_STOCK,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int descColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int stockColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_STOCK);
            int priceColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            String desc = data.getString(descColumnIndex);
            int stock = data.getInt(stockColumnIndex);
            Double price = data.getDouble(priceColumnIndex);
            String base64Image = data.getString(imageColumnIndex);
            byte[] imgdata = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bm;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            bm = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length, opt);

            // Update the views on the screen with the values from the database
            collapsingToolbar.setTitle(name);
            mDescView.setText(desc);
            mStockView.setText(Integer.toString(stock));
            mPriceView.setText(Double.toString(price));
            mImageView.setImageBitmap(bm);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageView = null;
        mDescView.setText("");
        mStockView.setText("");
        mPriceView.setText("");
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue in the detail view.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
