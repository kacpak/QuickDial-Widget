package net.kacpak.quickdialwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

public class QuickDialWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE = "net.kacpak.quickdialwidget.action.UPDATE";

    /**
     * Dane kontaktu do pobrania
     */
    private static final String[] PROJECTION = {
            Data.DISPLAY_NAME,
            Data.PHOTO_URI,
            "data4"
    };

    /**
     * Zmieniono rozmiar
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        onUpdate(context, appWidgetManager, new int[] { appWidgetId });
    }

    /**
     * Zaktualizuj widgety
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int widgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.quickdial_layout);
            updateWidget(context, widgetId, views);
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    /**
     * Aktualizuje podany widget
     * @param context kontekst
     * @param widgetId id wybranego widgetu
     * @param views widoki widgetu
     */
    private void updateWidget(Context context, int widgetId, RemoteViews views) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.widget_preferences),
                Context.MODE_PRIVATE
        );
        String contactUriString = sharedPreferences.getString(String.valueOf(widgetId), null);
        if (contactUriString == null)
            return;

        // Pobierz dane o kontakcie
        Uri contactUri = Uri.parse(contactUriString);
        Cursor cursor = context.getContentResolver()
                .query(contactUri, PROJECTION, null, null, null);
        if (cursor == null) return;
        cursor.moveToFirst();

        // Zapamiętaj interesujące nas dane
        String displayName = cursor.getString(0);
        String photoUriString = cursor.getString(1);
        String phoneNumber = cursor.getString(2);
        cursor.close();

        // Zaktualizuj nazwę wyświetlaną
        views.setTextViewText(R.id.contactName, displayName);

        // Zaktualizuj zdjęcie
        if (photoUriString == null)
            views.setImageViewResource(R.id.contactPhoto, R.drawable.ic_contact_picture);
        else
            Glide
                .with(context)
                .load(Uri.parse(photoUriString))
                .asBitmap()
                .into(new AppWidgetTarget(context, views, R.id.contactPhoto, widgetId));

        // Zadzwoń po naciśnięciu widgetu
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        PendingIntent callPendingIntent = PendingIntent.getActivity(
                context,
                0,
                callIntent,
                0
        );
        views.setOnClickPendingIntent(R.id.widget, callPendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_UPDATE)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (widgetId > -1)
                onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
        }

        // TODO dodaj filtr do otrzymania zmiany danych kontaktu, wyszukaj odpowiedni widget i zaktualizuj go
    }
}
