package com.example.upiapp.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.upiapp.models.Transaction;

import java.io.OutputStream;
import java.util.List;

public class CsvExportUtil {

    public static void exportTransactions(Context context, List<Transaction> list) {

        try {
            String fileName = "transaction_history.csv";
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS);

                Uri uri = context.getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                outputStream = context.getContentResolver().openOutputStream(uri);

            } else {
                String path = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toString();

                outputStream = new java.io.FileOutputStream(path + "/" + fileName);
            }

            StringBuilder csv = new StringBuilder();
            csv.append("Transaction ID,Sender,Receiver,Amount,Status,Category\n");

            for (Transaction t : list) {
                csv.append(t.transactionId).append(",")
                        .append(t.fromUpi).append(",")
                        .append(t.toUpi).append(",")
                        .append(t.amount).append(",")
                        .append(t.status).append(",")
                        .append(t.category).append("\n");
                        //.append(t.message).append(",")
                        //.append(t.timestamp).append("\n");
            }

            outputStream.write(csv.toString().getBytes());
            outputStream.close();

            Toast.makeText(context, "CSV downloaded in Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "CSV export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
