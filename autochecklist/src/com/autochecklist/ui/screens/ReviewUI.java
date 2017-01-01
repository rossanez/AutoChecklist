package com.autochecklist.ui.screens;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.utils.Utils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ReviewUI extends BaseUI {

	private OutputFormatter mOutputFormatter;

	private BorderPane mContent;
	private ScrollPane mFinalContents;
	private ProgressIndicator mProgressIndicator;

	public ReviewUI(OutputFormatter outputFormatter) {
		super();
		mOutputFormatter = outputFormatter;
		setDoWorkUponShowing(true);
	}

	@Override
	protected void initUI() {
		if (mOutputFormatter == null) {
			Utils.printError("No requirements available to review!");
			throw new RuntimeException("Need requirements to review their findings!");
		}

		mStage.setTitle("Auto Checklist - Review");
		mStage.setMinWidth(400);
		mStage.setMinHeight(500);

		VBox progressContent = new VBox(10);
		progressContent.setAlignment(Pos.CENTER);
		mProgressIndicator = new ProgressIndicator();
		mProgressIndicator.setProgress(0);
		progressContent.prefWidthProperty().bind(mStage.widthProperty());
		progressContent.prefHeightProperty().bind(mStage.heightProperty());
		Label loadingLabel = new Label("Loading...");
		progressContent.getChildren().addAll(mProgressIndicator, loadingLabel);

		mContent = new BorderPane();
        mContent.setCenter(progressContent);

		Scene scene = new Scene(mContent, 800, 600);
		mStage.setScene(scene);
	}

	@Override
	protected void beforeWork() {
		mProgressIndicator.setProgress(-1); // Indeterminate.
	}

	@Override
	protected void doWork() {
		mFinalContents = new ScrollPane();
		mFinalContents.setContent(buildContents());
		mFinalContents.setHbarPolicy(ScrollBarPolicy.NEVER);
		mFinalContents.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
	}

	@Override
	protected void workSucceeded() {
		mProgressIndicator.setProgress(1);

		mContent.setCenter(mFinalContents);
	}

	@Override
	protected void workFailed(boolean cancelled) {
		mProgressIndicator.setProgress(0);
		new AlertDialog("Error!", "Failure on generating the contents!", mStage).show();
	}

	@Override
	protected void onExternalCloseRequest(WindowEvent windowEvent) {
		// We don't want a confirmation dialog.
		// Calling close() for cleaning up unlikely running background works!
		close();
	}

	private Node buildContents() {
		VBox contents = new VBox(10);
		for (Requirement requirement : mOutputFormatter.getRequirements()) {
			Label label = new Label(requirement.getId() + " - " + requirement.getText());

			VBox tableRequirement = new VBox(10);
			tableRequirement.getChildren().addAll(label, createTable(requirement));
			contents.getChildren().add(tableRequirement);
		}

		contents.prefHeightProperty().bind(mStage.heightProperty());
		contents.prefWidthProperty().bind(mStage.widthProperty());

		return contents;
	}

	private ObservableList<Finding> getFindings(Requirement requirement) {
		ObservableList<Finding> findings = FXCollections.observableArrayList();
		findings.addAll(requirement.getAllFindings());

		return findings;
	}

	private TableView<Finding> createTable(Requirement requirement) {
		TableView<Finding> table = new TableView<Finding>();
		table.setEditable(true);

		TableColumn<Finding, Integer> questionId = new TableColumn<Finding, Integer>("Question");
		questionId.setCellValueFactory(new Callback<CellDataFeatures<Finding, Integer>, ObservableValue<Integer>>() {

			@Override
			public ObservableValue<Integer> call(CellDataFeatures<Finding, Integer> finding) {
				return new ReadOnlyObjectWrapper<Integer>(finding.getValue().getQuestionId());
			}
		});
		TableColumn<Finding, String> automatic = new TableColumn<Finding, String>("Automated");
		TableColumn<Finding, String> finding = new TableColumn<Finding, String>("Finding");
		finding.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(finding.getValue().getDetail());
			}
		});
		TableColumn<Finding, String> autAnswer = new TableColumn<Finding, String>("Answer");
		autAnswer.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(
						Question.getAnswerStringValue(finding.getValue().getAnswerType()));
			}
		});
		automatic.getColumns().add(finding);
		automatic.getColumns().add(autAnswer);

		TableColumn<Finding, String> manual = new TableColumn<Finding, String>("Manual");
		TableColumn<Finding, String> manAnswer = new TableColumn<Finding, String>("Reviewed answer");
		manAnswer.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(
						Question.getAnswerStringValue(finding.getValue().getReviewedAnswerType()));
			}
		});
		manAnswer.setCellFactory(ComboBoxTableCell.<Finding, String>forTableColumn("N/A", "Yes", "No"));
		manAnswer.setOnEditCommit(new EventHandler<CellEditEvent<Finding, String>>() {

			@Override
			public void handle(CellEditEvent<Finding, String> event) {
				String str = event.getNewValue();
				int answerType;
				if ("No".equals(str)) {
					answerType = Question.ANSWER_NO;
				} else if ("Yes".equals(str)) {
					answerType = Question.ANSWER_YES;
				} else {
					answerType = -1;
				}
				((Finding) event.getTableView().getItems().get(event.getTablePosition().getRow()))
						.setReviewedAnswerType(answerType);
			}
		});
		TableColumn<Finding, String> comments = new TableColumn<Finding, String>("Comments");
		comments.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(finding.getValue().getReviewerComments());
			}
		});
		comments.setOnEditCommit(new EventHandler<CellEditEvent<Finding, String>>() {

			@Override
			public void handle(CellEditEvent<Finding, String> event) {
				((Finding) event.getTableView().getItems().get(event.getTablePosition().getRow()))
						.setReviewerComments(event.getNewValue());
			}
		});
		manual.getColumns().add(manAnswer);
		manual.getColumns().add(comments);

		table.getColumns().add(questionId);
		table.getColumns().add(automatic);
		table.getColumns().add(manual);

		table.setItems(getFindings(requirement));

		table.setFixedCellSize(30);
		table.prefHeightProperty().bind(Bindings.size(table.getItems()).multiply(table.getFixedCellSize()).add(30));
		table.minHeightProperty().bind(table.prefHeightProperty());
		table.maxHeightProperty().bind(table.prefHeightProperty());
		table.prefWidthProperty().bind(mStage.widthProperty());

		return table;
	}
}
