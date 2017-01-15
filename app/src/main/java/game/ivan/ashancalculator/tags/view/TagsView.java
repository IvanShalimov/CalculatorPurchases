package game.ivan.ashancalculator.tags.view;

import android.content.Context;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import game.ivan.ashancalculator.database.models.Tags;

/**
 * Created by ivan on 20.12.16.
 */

public interface TagsView extends MvpView {

    void refreshList(List<Tags> list);

    public void showEditDialog(Tags tag);

    void showCreateDialog();
}
