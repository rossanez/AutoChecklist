package com.autochecklist.ui.screens;

import com.autochecklist.base.Finding;
import com.autochecklist.base.questions.Question;
import com.autochecklist.base.requirements.Requirement;
import com.autochecklist.modules.output.OutputFormatter;
import com.autochecklist.ui.BaseUI;
import com.autochecklist.ui.widgets.AlertDialog;
import com.autochecklist.utils.Utils;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class ReviewUI extends BaseUI {

	private OutputFormatter mOutputFormatter;

	private BorderPane mContent;
	private Node mFinalContents;
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
		mFinalContents = buildContents();
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

		contents.getChildren().add(createTable());
		contents.prefHeightProperty().bind(mStage.heightProperty());
		contents.prefWidthProperty().bind(mStage.widthProperty());

		return contents;
	}

	private ObservableList<Finding> getFindings(Requirement requirement) {
		ObservableList<Finding> findings = FXCollections.observableArrayList();
		findings.addAll(requirement.getAllFindings());

		return findings;
	}

	private TableView<Finding> createTable() {
		TableView<Finding> table = new TableView<Finding>();
		table.setEditable(true);

		TableColumn<Finding, String> requirementId = new TableColumn<Finding, String>("Req. ID");
		requirementId.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(finding.getValue().getRequirementId());
			}
		});

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
		autAnswer.setCellFactory(new Callback<TableColumn<Finding, String>, TableCell<Finding, String>>() {
            public TableCell<Finding, String> call(TableColumn<Finding, String> p) {
                TableCell<Finding, String> cell = new TableCell<Finding, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : getString());
                        setStyle("-fx-background-color:" + getBackgroundColor(getString()));
                        setAlignment(Pos.CENTER);
                    }

                    private String getString() {
                        return getItem() == null ? "" : getItem().toString();
                    }
                };


                return cell;
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
		manAnswer.setCellFactory(new Callback<TableColumn<Finding, String>, TableCell<Finding, String>>() {
			public TableCell<Finding, String> call(TableColumn<Finding, String> p) {
				ComboBoxTableCell<Finding, String> cell = new ComboBoxTableCell<Finding, String>("No answer", "Yes", "No") {
					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : getString());
						setStyle("-fx-background-color:" + getBackgroundColor(getString()));
						setAlignment(Pos.CENTER);
						setTooltip(new Tooltip("'No answer' -> uses automatic answer!"));
					}

					private String getString() {
						return getItem() == null ? "" : getItem().toString();
					}
				};

				return cell;
			}
		});
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
		TableColumn<Finding, String> comments = new TableColumn<Finding, String>("Reviewer Comments (optional)");
		comments.setCellValueFactory(new Callback<CellDataFeatures<Finding, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Finding, String> finding) {
				return new ReadOnlyObjectWrapper<String>(finding.getValue().getReviewerComments());
			}
		});
		comments.setCellFactory(TextFieldTableCell.<Finding>forTableColumn());
		comments.setOnEditCommit(new EventHandler<CellEditEvent<Finding, String>>() {

			@Override
			public void handle(CellEditEvent<Finding, String> event) {
				((Finding) event.getTableView().getItems().get(event.getTablePosition().getRow()))
						.setReviewerComments(event.getNewValue());
			}
		});
		manual.getColumns().add(manAnswer);
		manual.getColumns().add(comments);

		table.getColumns().add(requirementId);
		table.getColumns().add(questionId);
		table.getColumns().add(automatic);
		table.getColumns().add(manual);

		ObservableList<Finding> allFindings = FXCollections.observableArrayList();
		for (Requirement requirement : mOutputFormatter.getRequirements()) {
			allFindings.addAll(getFindings(requirement));
		}
		table.setItems(allFindings);

		table.prefHeightProperty().bind(mStage.heightProperty());
		table.prefWidthProperty().bind(mStage.widthProperty());

		return table;
	}

	private String getBackgroundColor(String answer) {
		if ("No".equals(answer)) {
			return "#FF0000"; // Red
		} else if ("Possible No".equals(answer)) {
			return "#FF3333"; // Light red
		} else if ("Warning".equals(answer)) {
			return "#FFFF00"; // Yellow
		} else if ("Possible Yes".equals(answer)) {
			return "#33FF33"; // Light green
		} else if ("Yes".equals(answer)) {
			return "#00FF00"; // Green
		} else {
			return "#FFFFFF"; // White
		}
	}
}
