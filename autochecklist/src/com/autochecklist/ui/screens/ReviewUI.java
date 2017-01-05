package com.autochecklist.ui.screens;

import java.util.Comparator;

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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SeparatorMenuItem;
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

	private MenuBar mMenuBar;
	private MenuItem mMenuClose;

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
			Utils.printError("No findings available to review!");
			throw new RuntimeException("Unable to review findings!");
		}

		mStage.setTitle("Auto Checklist - Review findings");
		mStage.setMinWidth(400);
		mStage.setMinHeight(500);

		mMenuClose = new MenuItem("Close");
		mMenuClose.setOnAction(this);

		mMenuBar = new MenuBar();
		Menu menu = new Menu("Actions");
		menu.getItems().add(new SeparatorMenuItem());
		menu.getItems().add(mMenuClose);
		mMenuBar.getMenus().add(menu);
		mMenuBar.prefWidthProperty().bind(mStage.widthProperty());

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

        VBox rootGroup = new VBox();
		rootGroup.getChildren().addAll(mMenuBar, mContent);

		Scene scene = new Scene(rootGroup, 800, 600);
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
	public void handle(ActionEvent event) {
		if (event.getSource() == mMenuClose) {
			close();
		}
	}

	@Override
	protected void onExternalCloseRequest(WindowEvent windowEvent) {
		// We don't want a confirmation dialog.
		// Calling close() for cleaning up unlikely running background works!
		close();
	}

	private TableView<Finding> buildContents() {
		TableView<Finding> contents = createTable();
		contents.prefHeightProperty().bind(mStage.heightProperty());
		contents.prefWidthProperty().bind(mStage.widthProperty());

		return contents;
	}

	private ObservableList<Finding> getFindings(Requirement requirement) {
		ObservableList<Finding> findings = FXCollections.observableArrayList();
		findings.addAll(requirement.getAllFindings());
		findings.sort(new Comparator<Finding>() {

			@Override
			public int compare(Finding o1, Finding o2) {
			    return (o1.getQuestionId() < o2.getQuestionId() ? -1
						: (o1.getQuestionId() == o2.getQuestionId() ? 0 : 1));
			}
		});

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
		requirementId.setCellFactory(new Callback<TableColumn<Finding, String>, TableCell<Finding, String>>() {
			public TableCell<Finding, String> call(TableColumn<Finding, String> p) {
				TableCell<Finding, String> cell = new TableCell<Finding, String>() {

					@Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        setAlignment(Pos.CENTER);
                        if (!empty) {
                            Requirement req = mOutputFormatter.getRequirement(item);
						    if (req != null) {
						        setTooltip(new Tooltip(req.getId() + ": " + req.getText()));
						    }
                        }
                    }
				};

				return cell;
			}
		});

		TableColumn<Finding, Integer> questionId = new TableColumn<Finding, Integer>("Question");
		questionId.setCellValueFactory(new Callback<CellDataFeatures<Finding, Integer>, ObservableValue<Integer>>() {

			@Override
			public ObservableValue<Integer> call(CellDataFeatures<Finding, Integer> finding) {
				return new ReadOnlyObjectWrapper<Integer>(finding.getValue().getQuestionId());
			}
		});
		questionId.setCellFactory(new Callback<TableColumn<Finding, Integer>, TableCell<Finding, Integer>>() {
			public TableCell<Finding, Integer> call(TableColumn<Finding, Integer> param) {
				TableCell<Finding, Integer> cell = new TableCell<Finding, Integer>() {

					@Override
					protected void updateItem(Integer item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : item.toString());
						setAlignment(Pos.CENTER);
						if (!empty) {
						    Question question = mOutputFormatter.getQuestion(item);
						    if (question != null) {
							    setTooltip(new Tooltip(question.getId() + ": " + question.getText()));
						    }
						}
					}
				};

				return cell;
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
						Question.getAnswerStringValue(finding.getValue().getAutomatedAnswerType()));
			}
		});
		autAnswer.setCellFactory(new Callback<TableColumn<Finding, String>, TableCell<Finding, String>>() {
            public TableCell<Finding, String> call(TableColumn<Finding, String> p) {
                TableCell<Finding, String> cell = new TableCell<Finding, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        setStyle("-fx-background-color:" + getBackgroundColor(item));
                        setAlignment(Pos.CENTER);
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
				ComboBoxTableCell<Finding, String> cell = new ComboBoxTableCell<Finding, String>("None", "Yes", "No") {

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty ? null : item);
						setStyle("-fx-background-color:" + getBackgroundColor(item));
						setAlignment(Pos.CENTER);
						setTooltip(new Tooltip("'None' -> keeps automatic answer"));
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

		requirementId.setSortType(TableColumn.SortType.ASCENDING);
		requirementId.setSortable(true);

		table.prefHeightProperty().bind(mStage.heightProperty());
		table.prefWidthProperty().bind(mStage.widthProperty());

		return table;
	}

	private String getBackgroundColor(String answer) {
		if ("No".equals(answer)) {
			return "#FF0000"; // Red
		} else if ("Possible No".equals(answer)) {
			return "#FF4500"; // Orange red
		} else if ("Warning".equals(answer)) {
			return "#FFFF00"; // Yellow
		} else if ("Possible Yes".equals(answer)) {
			return "#00FF00"; // Light green
		} else if ("Yes".equals(answer)) {
			return "#008000"; // Green
		} else {
			return "#E0E0E0"; // Light gray
		}
	}
}
