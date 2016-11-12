package org.eclipse.paho.android.sample.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

    public PublishFragment() {
        // Required empty public constructor
    }

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
                // Check there is an image selected
                if (imagePath.length() < 1) {
                    // TO DO
                    return;
                }

                // Make thread which will get the image uri
                Thread threadGetImageUri = new Thread() {
                    public void run() {
                        try {
                            // Upload selected image to cloudinary
                            System.out.println("Uploading image to cloudinary...");

                            Map config = new HashMap();
                            config.put("cloud_name", "teamc");
                            config.put("api_key", "552788372692362");
                            config.put("api_secret", "-BCX9WJ4FYICQQqkwoSsSIOnuaU");
                            Cloudinary cloudinary = new Cloudinary(config);
                            Map uploadResult = cloudinary.uploader().upload(new File(imagePath), ObjectUtils.emptyMap());
                            imageUri = uploadResult.get("url").toString();

                            System.out.println("Upload Success!\nResult:\n" + imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };


                try {


                    // Assume all hashtag start with '#'
                    System.out.println("Parsing hashtag[" + hashtag + "]...");
                    StringTokenizer tokenizer = new StringTokenizer(hashtag);
                    ArrayList<String> hashtagList = new ArrayList<>();
                    while (tokenizer.hasMoreElements()) {
                        hashtagList.add(tokenizer.nextToken().substring(1));
                    }

                    System.out.println("Getting shared URL of image...");
                    threadGetImageUri.start();
                    threadGetImageUri.join();

                    System.out.println("Publish Starts...");
                    for (String currHashtag : hashtagList) {
                        System.out.println("Publishing: [hashtag: " + currHashtag + ", imageUri: " + imageUri + ", QoS: " + selectedQos + ", Retain: " + retainValue + "]");
                        ((MainActivity) getActivity()).publish(connection, currHashtag, imageUri, selectedQos, retainValue);
                    }

                    System.out.println("All done!");


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


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