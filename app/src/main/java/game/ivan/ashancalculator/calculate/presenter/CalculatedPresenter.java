package game.ivan.ashancalculator.calculate.presenter;



import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import game.ivan.ashancalculator.AshanApplication;
import game.ivan.ashancalculator.calculate.presenter.dagger.CalculatorPresenterComponent;
import game.ivan.ashancalculator.calculate.view.CalculatorView;
import game.ivan.ashancalculator.database.DatabaseCalculateManager;
import game.ivan.ashancalculator.database.models.Tags;
import game.ivan.ashancalculator.service.Calculator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ivan on 03.01.17.
 */

public class CalculatedPresenter extends MvpBasePresenter<CalculatorView> {

    @Inject
    DatabaseCalculateManager databaseManager;
    @Inject
    Calculator calculator;

    int bufferPosition =0;
    CalculatorPresenterComponent component;

    public CalculatedPresenter() {
        component = AshanApplication.getComponent().createCalculatorPresenterComponent();
        component.injectCalculatedPresenter(this);
    }

    public void getTags() {
        databaseManager.readAllTags()
                .subscribeOn(Schedulers.io())

                .map(tags -> {
                    List<String> labels = new ArrayList<>();
                    for (Tags tag : tags) {
                        labels.add(tag.nameTags);
                    }
                    return labels;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    if (isViewAttached())
                        getView().setSpinnerData(strings);
                });


    }

    public void detachView(boolean retainPresenterInstance) {
        super.detachView(retainPresenterInstance);
    }


    public void getDateForScreen(int position) {
        bufferPosition = position;
        databaseManager.getItemForTag(++position)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (isViewAttached()) {
                        getView().refreshList(list);
                        getView().showOneManPrice(calculator.oneManSum(list, databaseManager.getDivider(++bufferPosition)));
                        getView().showSum(calculator.sum(list));
                    }
                });
    }
}
