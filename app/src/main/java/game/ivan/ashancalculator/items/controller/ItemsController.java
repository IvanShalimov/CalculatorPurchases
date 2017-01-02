package game.ivan.ashancalculator.items.controller;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hannesdorfmann.mosby.mvp.conductor.MvpController;
import com.mindorks.paracamera.Camera;
import com.mindorks.paracamera.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import game.ivan.ashancalculator.R;
import game.ivan.ashancalculator.database.models.Item;
import game.ivan.ashancalculator.database.models.Tags;
import game.ivan.ashancalculator.items.presenter.ItemsPresenter;
import game.ivan.ashancalculator.items.view.ItemsView;
import game.ivan.ashancalculator.tags.controller.TagsListAdapter;

/**
 * Created by ivan on 21.12.16.
 */

public class ItemsController extends MvpController<ItemsView,ItemsPresenter> implements ItemsView,
        MaterialDialog.SingleButtonCallback,ItemListAdapter.ItemsListAdapterCallback,
        PermissionListener {

    private Unbinder unbinder;
    @BindView(R.id.list)
    RecyclerView list;
    RecyclerView.LayoutManager layoutManager;
    ItemListAdapter adapter;
    View dialogAdd;
    ConductorCamera camera;
    String picturePath="";

    public ItemsController(){
        setHasOptionsMenu(true);
    }

    public ItemsController(Bundle args){
        setHasOptionsMenu(true);
    }

    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflateView(inflater, container);
        unbinder = ButterKnife.bind(this,view);
        camera = new ConductorCamera(this);
        onViewBound(view);
        return view;
    }

    protected void onViewBound(View view){
        layoutManager = new LinearLayoutManager(getApplicationContext());
        list.setLayoutManager(layoutManager);
        adapter = new ItemListAdapter();
        adapter.setCallback(this);
        list.setAdapter(adapter);

        // Build the camera
        camera.builder()
                .resetToCorrectOrientation(false)
                .setDirectory("pics")
                .setName("item_" + System.nanoTime())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(75);
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        //подгрузка пунктов
        presenter.loadItems();
    }

    @NonNull
    @Override
    public ItemsPresenter createPresenter() {
        return new ItemsPresenter();
    }

    protected  View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container){
        return inflater.inflate(R.layout.list_layout, container, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tags, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_item) {
            //
            new TedPermission(getApplicationContext())
                    .setPermissionListener(this)
                    .setDeniedMessage("If you reject permission,you can not use " +
                            "this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroyView(View view) {
        super.onDestroyView(view);
        unbinder.unbind();
        unbinder = null;
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        String name = ((EditText)dialogAdd.findViewById(R.id.name_item_edit_field))
                .getText().toString();
        long tagId = ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).getSelectedItemId();
        int count = Integer.valueOf(((EditText)dialogAdd.findViewById(R.id.count_item_picker))
                .getText().toString());
        double price = Double.valueOf(((EditText)dialogAdd.findViewById(R.id.price_item_field))
                .getText().toString());
        Log.d("Test","path before save - "+ picturePath);
        presenter.saveItem(new Item(name,picturePath,tagId,count,price));

    }

    @Override
    public void onListItemSelect(Item item) {
        MaterialDialog dialog;
        boolean wrapInScrollView = true;
     //   if(dialogAdd == null){
            dialog = new MaterialDialog.Builder(getActivity())
                    .title("Добавить товар")
                    .customView(R.layout.add_item_dialog_layout, wrapInScrollView)
                    .positiveText("Сохранить")
                    .negativeText("Удалить")
                    .onPositive((dialog12, which) -> {
                        item.name = ((EditText)dialogAdd.findViewById(R.id.name_item_edit_field))
                                .getText().toString();
                        item.tagId = ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).getSelectedItemId();
                        item.count = Integer.valueOf(((EditText)dialogAdd.findViewById(R.id.count_item_picker))
                                .getText().toString());
                        item.price = Double.valueOf(((EditText)dialogAdd.findViewById(R.id.price_item_field))
                                .getText().toString());

                        presenter.saveItem(item);
                    })
                    .onNegative((dialog1, which) -> {
                        presenter.deleteItem(item);
                    })
                    .build();

            dialogAdd = dialog.getCustomView();
        /*} else {
            dialog = new MaterialDialog.Builder(getActivity())
                    .title("Добавить товар")
                    .customView(dialogAdd, wrapInScrollView)
                    .positiveText("Сохранить")
                    .negativeText("Удалить")
                    .onPositive((dialog12, which) -> {
                        item.name = ((EditText)dialogAdd.findViewById(R.id.name_item_edit_field))
                                .getText().toString();
                        item.tagId = ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).getSelectedItemId();
                        item.count = Integer.valueOf(((EditText)dialogAdd.findViewById(R.id.count_item_picker))
                                .getText().toString());
                        item.price = Double.valueOf(((EditText)dialogAdd.findViewById(R.id.price_item_field))
                                .getText().toString());

                        presenter.saveItem(item);

                    })
                    .onNegative((dialog1, which) -> {
                        presenter.deleteItem(item);
                    })
                    .build();
        }
*/
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.spinner_item, presenter.getListTag());
        ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).setAdapter(spinnerAdapter);
        ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).setSelection((int)item.tagId);
        ((EditText)dialogAdd.findViewById(R.id.name_item_edit_field)).setText(String.valueOf(item.name));
        ((EditText)dialogAdd.findViewById(R.id.count_item_picker)).setText(String.valueOf(item.count));
        ((EditText)dialogAdd.findViewById(R.id.price_item_field)).setText(String.valueOf(item.price));

        dialog.show();
    }
    @Override
    public void onPermissionGranted() {
        try {
            picturePath =  camera.takePicture(true);
            Log.d("Test",""+picturePath);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
        Toast.makeText(getActivity(), "Permission Denied\n" + deniedPermissions.toString(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            Bitmap bitmap =(Bitmap) data.getExtras().get("data");  //camera.getCameraBitmap(picturePath);
            if(bitmap != null) {
                camera.saveBitmap(picturePath,bitmap);
                MaterialDialog dialog;
                boolean wrapInScrollView = true;

         //       if(dialogAdd == null){
                    dialog = new MaterialDialog.Builder(getActivity())
                            .title("Добавить товар")
                            .customView(R.layout.add_item_dialog_layout, wrapInScrollView)
                            .positiveText("Сохранить")
                            .negativeText("Отмена")
                            .onPositive(this)
                            .build();

                    dialogAdd = dialog.getCustomView();
                    /* } else {
                    dialog = new MaterialDialog.Builder(getActivity())
                            .title("Добавить товар")
                            .customView(dialogAdd, wrapInScrollView)
                            .positiveText("Сохранить")
                            .negativeText("Отмена")
                            .onPositive(this)
                            .build();
                }*/

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        R.layout.spinner_item, presenter.getListTag());
                ((Spinner)dialogAdd.findViewById(R.id.tag_spinner_list)).setAdapter(spinnerAdapter);


                dialog.show();
            }else{
                Toast.makeText(this.getApplicationContext(),"Picture not taken!",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void refreshView(List<Item> list) {
        adapter.setItems(list);
    }
}
