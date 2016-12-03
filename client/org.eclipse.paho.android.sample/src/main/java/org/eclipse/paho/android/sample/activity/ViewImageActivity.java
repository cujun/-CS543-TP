package org.eclipse.paho.android.sample.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.paho.android.sample.R;
import org.eclipse.paho.android.sample.model.NetworkManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ViewImageActivity extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Intent intent = getIntent();
        String topic = intent.getStringExtra("topic");

        String message = intent.getStringExtra("message");

        Toast.makeText(this, topic + " " + message, Toast.LENGTH_LONG).show();

        mImageView = (ImageView) findViewById(R.id.viewimage_image_imageview);
        new DownloadImageTask(message).execute();
    }

    class DownloadImageTask extends AsyncTask<Void, Void, Boolean> {

        String url;
        HttpResponse response = null;
        Bitmap bmp = null;

        public DownloadImageTask(String url) {
            this.url = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            HttpGet get = new HttpGet(url);

            try {
                response = NetworkManager.getInstance(ViewImageActivity.this).execute(get);

                if(response == null) {
                    return false;
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = null;
                    try {
                        instream = entity.getContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    try {
                        while ((len = instream.read(buffer)) != -1) {
                            baos.write(buffer, 0, len);
                        }
                        baos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] b = baos.toByteArray();
                    bmp = BitmapFactory.decodeByteArray(b, 0, b.length);

                }

            } catch (IOException e) {
                e.printStackTrace();
                return Boolean.FALSE;
            }


            return response != null;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                    mImageView.setImageBitmap(bmp);
                } else {
                    Toast.makeText(ViewImageActivity.this, "Sorry, fail to download image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

