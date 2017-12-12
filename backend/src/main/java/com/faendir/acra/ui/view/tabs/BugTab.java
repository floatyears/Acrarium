package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.AppScoped;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.ReportList;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.TimeSpanRenderer;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

/**
 * @author Lukas
 * @since 17.05.2017
 */
public class BugTab extends VerticalLayout implements DataManager.Listener<AppScoped> {
    public static final String CAPTION = "Bugs";
    @NotNull private final String app;
    @NotNull private final NavigationManager navigationManager;
    @NotNull private final DataManager dataManager;
    @NotNull private final MyGrid<Bug> bugs;
    @NotNull private final CheckBox hideSolved;
    @Nullable private ReportList reportList;

    public BugTab(@NotNull String app, @NotNull NavigationManager navigationManager, @NotNull DataManager dataManager) {
        this.app = app;
        this.navigationManager = navigationManager;
        this.dataManager = dataManager;
        hideSolved = new CheckBox("Hide solved", true);
        hideSolved.addValueChangeListener(e -> setItems());
        addComponent(hideSolved);
        setComponentAlignment(hideSolved, Alignment.MIDDLE_RIGHT);
        bugs = new MyGrid<>(null, dataManager.getLazyBugs(app, false));
        bugs.setWidth(100, Unit.PERCENTAGE);
        bugs.addColumn(bug -> bug.getReportIds().size(), "Reports");
        bugs.sort(bugs.addColumn(Bug::getLastReport, new TimeSpanRenderer(), "lastReport","Latest Report"), SortDirection.DESCENDING);
        bugs.addColumn(Bug::getVersionCode, "versionCode","Version");
        bugs.addColumn(bug -> bug.getStacktrace().split("\n", 2)[0], "stacktrace","Stacktrace").setExpandRatio(1);
        bugs.addSelectionListener(this::handleBugSelection);
        bugs.addColumn(bug -> new MyCheckBox(bug.isSolved(), SecurityUtils.hasPermission(app, Permission.Level.EDIT), e -> dataManager.setBugSolved(bug, e.getValue())),
                       new ComponentRenderer(), "Solved");
        addComponent(bugs);
        Style.NO_PADDING.apply(this);
        setCaption(CAPTION);
        addAttachListener(e -> dataManager.addListener(this, AppScoped.class));
        addDetachListener(e -> dataManager.removeListener(this));
    }

    private void handleBugSelection(@NotNull SelectionEvent<Bug> e) {
        Optional<Bug> selection = e.getFirstSelectedItem();
        ReportList reportList = null;
        if (selection.isPresent()) {
            reportList = new ReportList(app, navigationManager, dataManager, dataManager.getLazyReportsForBug(selection.get()),
                                        reportInfo -> dataManager.matches(selection.get(), reportInfo));
            replaceComponent(this.reportList, reportList);
        } else if (this.reportList != null) {
            removeComponent(this.reportList);
        }
        this.reportList = reportList;
    }

    @Override
    public void onChange(@NotNull AppScoped appScoped) {
        if (appScoped.getApp().equals(app)) {
            setItems();
        }
    }

    private void setItems() {
        getUI().access(() -> {
            Set<Bug> selection = bugs.getSelectedItems();
            bugs.setDataProvider(dataManager.getLazyBugs(app, !hideSolved.getValue()));
            selection.forEach(bugs::select);
        });
    }
}
