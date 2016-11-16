package org.eclipse.paho.android.sample.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.test.AndroidTestRunner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.content.Intent;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.eclipse.paho.android.sample.R;
import org.eclipse.paho.android.sample.internal.Connections;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static android.app.Activity.RESULT_OK;



public class PublishFragment extends Fragment {

    private static int RESULT_LOAD_IMAGE = 1;

    Connection connection;

    int selectedQos = 0;
    boolean retainValue = false;
    String hashtag = "", imagePath = "", imageUri = "";
    ArrayList<String> hashtagList;
    ProgressDialog publishDialog;

    public PublishFragment() {
        // Required empty public constructor
    }

    // handler for releasing publishDialog
    Handler dialogDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            publishDialog.dismiss();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<String, Connection> connections = Connections.getInstance(this.getActivity())
                .getConnections();
        connection = connections.get(this.getArguments().getString(ActivityConstants.CONNECTION_KEY));

        System.out.println("FRAGMENT CONNECTION: " + this.getArguments().getString(ActivityConstants.CONNECTION_KEY));
        System.out.println("NAME:" + connection.getId());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_publish, container, false);
        EditText hashtagText = (EditText) rootView.findViewById(R.id.hashtag);
        Button imageButton = (Button) rootView.findViewById(R.id.publish_imgBtn);
        Spinner qos = (Spinner) rootView.findViewById(R.id.qos_spinner);
        final Switch retain = (Switch) rootView.findViewById(R.id.retain_switch);


        hashtagText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hashtag = s.toString();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        qos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedQos = Integer.parseInt(getResources().getStringArray(R.array.qos_options)[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.qos_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qos.setAdapter(adapter);

        retain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                retainValue = isChecked;
            }
        });

        Button publishButton = (Button) rootView.findViewById(R.id.publish_button);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check whether the entered hashtag is valid
                if (hashtag.length() < 1) {
                    Toast.makeText(getActivity(), "Please enter the hashtag of your image", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // Hashtag validation
                    // Assume all hashtags start with '#'
                    System.out.println("Parsing hashtag[" + hashtag + "]...");
                    StringTokenizer tokenizer = new StringTokenizer(hashtag);
                    hashtagList = new ArrayList<>();
                    while (tokenizer.hasMoreElements()) {
                        String curr = tokenizer.nextToken();
                        if (curr.charAt(0) != '#') {
                            Toast.makeText(getActivity(), "Please start hashtag with '#'", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        hashtagList.add(curr.substring(1));
                    }
                }
                // Check there is an image selected
                if (imagePath.length() < 1) {
                    Toast.makeText(getActivity(), "Please select an image to publish", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Make thread which will do all publish jobs
                Thread threadPublish = new Thread() {
                    public void run() {
                        try {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    publishDialog = new ProgressDialog(getActivity());
                                    publishDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    publishDialog.setMessage("Publishing...");
                                    publishDialog.show();
                                }
                            });


                            // Upload selected image to cloudinary
                            System.out.println("Uploading image to cloudinary...");
                            Map config = new HashMap();
                            config.put("cloud_name", "cs543teamc");
                            config.put("api_key", "543136171475397");
                            config.put("api_secret", "VYhheb0vB7wqE-6U5JfUwVqP7qQ");
                            Cloudinary cloudinary = new Cloudinary(config);
                            Map uploadResult = cloudinary.uploader().upload(new File(imagePath), ObjectUtils.emptyMap());
                            imageUri = uploadResult.get("url").toString();
                            System.out.println("Upload Success!\nResult:\n" + imageUri);

                            // Send the uri of image to server
                            System.out.println("Publish Starts...");
                            for (String currHashtag : hashtagList) {
                                System.out.println("Publishing: [hashtag: " + currHashtag + ", imageUri: " + imageUri + ", QoS: " + selectedQos + ", Retain: " + retainValue + "]");
                                ((MainActivity) getActivity()).publish(connection, currHashtag, imageUri, selectedQos, retainValue);
                            }


                            // Now, we are done!
                            System.out.println("All done!");
                            dialogDismissHandler.sendEmptyMessage(0);
                            imagePath = "";
                            imageUri = "";

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                // Publish task start
                threadPublish.start();
                // Clear ImageView
                ImageView imageView = (ImageView)getActivity().findViewById(R.id.publish_imageView);
                imageView.setImageResource(android.R.color.transparent);

            }
        });


        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Executed whenever image is selected
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView)getActivity().findViewById(R.id.publish_imageView);
            imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        }


    }
}