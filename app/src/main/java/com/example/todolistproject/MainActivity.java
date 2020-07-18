package com.example.todolistproject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements RecyclerAdapter.ListItemListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private String toStore;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerAdapter adapter;
    private CoordinatorLayout coordinatorLayout;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);

        coordinatorLayout = findViewById(R.id.coordinator_main);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this,AddList.class);
//                startActivity(intent);
                showAlertDialog();

            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        setUpRecyclerView();

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout,
                toolbar_main,
                R.string.drawer_open,
                R.string.drawer_close);
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_main);
        navigationView.setCheckedItem(R.id.item_main);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_main:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.item_add_list:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        showAlertDialog();
                        return true;
                    case R.id.item_hint:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        showHintDialog();
                        return true;
                    case R.id.item_share:
                        Toast.makeText(MainActivity.this, "Function not available right now", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }

            }

        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showAlertDialog() {
        final EditText editText = new EditText(this);
        editText.setHint("Enter list name");
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Add New list")
                .setView(editText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        toStore = editText.getText().toString();
                        ListModel listModel = new ListModel(toStore);

                        if (!toStore.equals("")) {

                            db.collection("List").add(listModel)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(MainActivity.this, "New List Created", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(MainActivity.this, "Hint: Swipe Right To Delete", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure!!!!!!!!: " + e.toString());
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter list name", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void showHintDialog() {

        final TextView textView1 = new TextView(this);
        textView1.setText("  1. Press on + button to add new List\n  2. Swipe Right to delete list\n  3. Press on list to view task");
        textView1.setTextSize(20);
        textView1.setPadding(5,0,0,0);
        textView1.setTextColor(Color.BLACK);

      AlertDialog.Builder alertDialog  =  new AlertDialog.Builder(MainActivity.this)
                .setTitle("Hint")
                .setView(textView1)
                .setPositiveButton("Dimiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
      alertDialog.show();

                }


    public void setUpRecyclerView() {
        Query query = db.collection("List");
        FirestoreRecyclerOptions<ListModel> options = new FirestoreRecyclerOptions.Builder<ListModel>()
                .setQuery(query, ListModel.class)
                .build();

        adapter = new RecyclerAdapter(options, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        Log.d(TAG, "onStop: ");
    }


    @Override
    public void onItemClicked(DocumentSnapshot snapshot) {

        String id = snapshot.getId();
        Log.d(TAG, "onItemClicked: !!!!!!!!!!!!! and id:" + id);

        Intent intent = new Intent(getApplicationContext(), SubItemPage.class);
        intent.putExtra("id", id);
        startActivity(intent);

    }

    @Override
    public void handleDeleteItem(DocumentSnapshot snapshot) {
        final DocumentReference documentReference = snapshot.getReference();
        final ListModel listModel = snapshot.toObject(ListModel.class);

        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Data deleted");
            }
        });
        Snackbar.make(coordinatorLayout, "Are you sure", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        documentReference.set(listModel);
                    }
                }).setDuration(10000).show();
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            adapter.deleteItem(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                    .addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}