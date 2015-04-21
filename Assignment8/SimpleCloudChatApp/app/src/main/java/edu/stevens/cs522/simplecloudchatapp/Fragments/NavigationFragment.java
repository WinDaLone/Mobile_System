package edu.stevens.cs522.simplecloudchatapp.Fragments;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.simplecloudchatapp.Activities.MainChatActivity;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IEntityCreator;
import edu.stevens.cs522.simplecloudchatapp.Callbacks.IQueryListener;
import edu.stevens.cs522.simplecloudchatapp.Contracts.ChatroomContract;
import edu.stevens.cs522.simplecloudchatapp.Entities.Chatroom;
import edu.stevens.cs522.simplecloudchatapp.Managers.ChatroomManager;
import edu.stevens.cs522.simplecloudchatapp.Managers.TypedCursor;
import edu.stevens.cs522.simplecloudchatapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NavigationFragment extends Fragment {
    public static final String TAG = NavigationFragment.class.getCanonicalName();
    View mainFrame;
    ChatroomManager manager = null;
    ListView listView;
    SimpleCursorAdapter adapter = null;

    public static final int NAVIGATION_FRAGMENT_LOADER_ID = 4;



    public NavigationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainFrame = getActivity().findViewById(R.id.main_chatroom_container);
        final boolean MultiPane = mainFrame != null && mainFrame.getVisibility() == View.VISIBLE;

        manager = new ChatroomManager(getActivity(), new IEntityCreator<Chatroom>() {
            @Override
            public Chatroom create(Cursor cursor) {
                return new Chatroom(cursor);
            }
        }, NAVIGATION_FRAGMENT_LOADER_ID);
        listView = (ListView)view.findViewById(R.id.navigation_list);
        String[] from = new String[] {ChatroomContract.NAME};
        int[] to = new int[] {R.id.navigation_row};
        adapter = new SimpleCursorAdapter(getActivity(), R.layout.navigation_row, null, from, to, 0);
        manager.QueryAsync(ChatroomContract.CONTENT_URI, new IQueryListener<Chatroom>() {
            @Override
            public void handleResults(TypedCursor<Chatroom> cursor) {
                adapter.swapCursor(cursor.getCursor());
            }

            @Override
            public void closeResults() {
                adapter.swapCursor(null);
            }
        });

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = adapter.getCursor();
                cursor.moveToPosition(position);
                Chatroom chatroom = new Chatroom(cursor);
                if (!MultiPane) { // Portrait mode
                    Intent intent = new Intent(getActivity(), MainChatActivity.class);
                    intent.putExtra(MainChatActivity.TAG, chatroom);
                    startActivity(intent);
                } else {
                    Fragment fragment = new MainChatFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(MainChatFragment.TAG, chatroom);
                    fragment.setArguments(args);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.main_chatroom_container, fragment);
                    ft.commit();
                    getFragmentManager().executePendingTransactions();
                }
            }
        });

    }
}
