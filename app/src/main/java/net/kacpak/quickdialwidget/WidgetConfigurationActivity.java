package net.kacpak.quickdialwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static android.appwidget.AppWidgetManager.*;
import static android.provider.ContactsContract.CommonDataKinds.*;

public class WidgetConfigurationActivity extends Activity {

    static final int PICK_CONTACT_REQUEST = 1;  // The request code

    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // By przy wyjściu z activity widget nie został dodany
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish();

        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            initializeAppWidget(contactUri);
        }
        finish();
    }

    /**
     * Inicjalizuje widget
     * @param contactUri
     */
    private void initializeAppWidget(Uri contactUri) {
        // Zapamiętaj dane dla widgetu
        SharedPreferences.Editor preferences = getSharedPreferences(
                getString(R.string.widget_preferences),
                MODE_PRIVATE
        ).edit();
        preferences.putString(String.valueOf(mAppWidgetId), contactUri.toString());
        preferences.apply();

        // Odśwież widget
        Intent refreshIntent = new Intent(this, QuickDialWidget.class);
        refreshIntent.setAction(QuickDialWidget.ACTION_UPDATE);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        sendBroadcast(refreshIntent);

        // Przekaż, że widget został pomyślnie utworzony
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}
