package org.eclipse.paho.android.sample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.eclipse.paho.android.sample.R;
import org.eclipse.paho.android.sample.components.MessageListItemAdapter;
import org.eclipse.paho.android.sample.internal.Connections;
import org.eclipse.paho.android.sample.internal.IReceivedMessageListener;
import org.eclipse.paho.android.sample.model.ReceivedMessage;

import java.util.ArrayList;
import java.util.Map;


public class HistoryFragment extends Fragment {

    ListView messageHistoryListView;
    MessageListItemAdapter messageListAdapter;
    Button clearButton;
    Connection connection;


    ArrayList<ReceivedMessage> messages;
    public HistoryFragment() {

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Map<String, Connection> connections = Connections.getInstance(this.getActivity())
                .getConnections();
        connection = connections.get(this.getArguments().getString(ActivityConstants.CONNECTION_KEY));
        System.out.println("History Fragment: " + connection.getId());
        setHasOptionsMenu(true);
        messages = connection.getMessages();
        connection.addReceivedMessageListner(new IReceivedMessageListener() {
            @Override
            public void onMessageReceived(ReceivedMessage message) {
                System.out.println("GOT A MESSAGE in history " + new String(message.getMessage().getPayload()));
                System.out.println("M: " + messages.size());
                messageListAdapter.notifyDataSetChanged();
            }
        });





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_connection_history, container, false);

        messageListAdapter = new MessageListItemAdapter(getActivity(), messages);
        messageHistoryListView = (ListView) rootView.findViewById(R.id.history_list_view);
        messageHistoryListView.setAdapter(messageListAdapter);

        messageHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
                ReceivedMessage msg = messageListAdapter.getItem(position);
                Log.d("HistoryList", String.valueOf(msg.getMessage()));

                Intent intent = new Intent(getActivity(), ViewImageActivity.class);
                intent.putExtra("topic", msg.getTopic());
                intent.putExtra("message", String.valueOf(msg.getMessage()));
                startActivity(intent);

            }
        });

        clearButton = (Button) rootView.findViewById(R.id.history_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                messages.clear();
                messageListAdapter.notifyDataSetChanged();
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



}

