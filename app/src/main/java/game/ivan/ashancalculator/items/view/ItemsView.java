package game.ivan.ashancalculator.items.view;

import com.hannesdorfmann.mosby.mvp.MvpView;

import java.util.List;

import game.ivan.ashancalculator.database.models.Item;

/**
 * Created by ivan on 21.12.16.
 */

public interface ItemsView extends MvpView {

    public  void refreshView(List<Item> list);
}
