package il.ac.tau.cs.sw1.flags;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class FlagsGUI {

	private static final int MAX_ERRORS = 3;
	private Shell shell;
	private Label scoreLabel;
	private Composite questionPanel;
	private Label startupMessageLabel;
	private Font boldFont;

	// Currently visible UI elements.
	Label instructionLabel;
	Label flagImageLabel;
	Text countryGuessText;

	private Button passButton;
	private Button getALetterButton;

	private List<Map.Entry<String, String>> flags = new ArrayList<>();
	private int score;
	private int fails;
	private int successes;
	private int attempts;
	private int iterator;
	private boolean firstPass;
	private boolean firstGetALetter;	
	private int getALetterButtonCounter;


	public void open() {
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	private void createShell() {
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Capture the Flags");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(
				new Point(monitor_bounds.width / 3, monitor_bounds.height / 2));
		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);

		// create window panels
		createFileLoadingPanel();
		createScorePanel();
		createQuestionPanel();
	}

	/**
	 * Creates the widgets of the form for flags file selection
	 */
	private void createFileLoadingPanel() {
		final Composite fileSelection = new Composite(shell, SWT.NULL);
		fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
		fileSelection.setLayout(new GridLayout(4, false));

		final Label label = new Label(fileSelection, SWT.NONE);
		label.setText("Enter flags file path: ");

		// text field to enter the file path
		final Text filePathField = new Text(fileSelection,
				SWT.SINGLE | SWT.BORDER);
		filePathField.setLayoutData(GUIUtils.createFillGridData(1));

		// "Browse" button
		final Button browseButton = new Button(fileSelection, SWT.PUSH);
		browseButton.setText("Browse");
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String path = GUIUtils.getFilePathFromFileDialog(shell);
				filePathField.setText(path);
			}
		});

		// "Play!" button
		final Button playButton = new Button(fileSelection, SWT.PUSH);
		playButton.setText("Play!");
		playButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				File file = new File(filePathField.getText());
				String parentDir = file.getParent();
				try {
					BufferedReader bufferedReader = new BufferedReader(
							new FileReader(file));
					int n = Integer.parseInt(bufferedReader.readLine());
					for (int i = 0; i < n; i++) {
						String[] line = bufferedReader.readLine().split(",");
						flags.add(new AbstractMap.SimpleEntry<>(
								parentDir + "\\" + line[0], line[1]));
					}
					bufferedReader.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Collections.shuffle(flags);
				score = 0;
				fails = 0;
				successes = 0;
				attempts = 0;
				iterator = 0;
				firstPass = true;
				firstGetALetter = true;
				nextQuestion();
			}
		});
	}

	private void nextQuestion() {
		if (iterator == flags.size()) {
			GUIUtils.showInfoDialog(shell, "Success!", "Your final score is "
					+ score + " after " + attempts + " questions.");
			return;
		}
		attempts++;
		Map.Entry<String, String> flag = flags.get(iterator++);
		updateQuestionPanel(flag.getKey(), flag.getValue());
	}

	/**
	 * Creates the panel that displays the current score
	 */
	private void createScorePanel() {
		Composite scorePanel = new Composite(shell, SWT.BORDER);
		scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
		scorePanel.setLayout(new GridLayout(2, false));

		final Label label = new Label(scorePanel, SWT.NONE);
		label.setText("Total score: ");

		// The label which displays the score; initially empty
		scoreLabel = new Label(scorePanel, SWT.NONE);
		scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
	}

	/**
	 * Creates the panel that displays the flag image, as soon as the game
	 * starts. See the updateQuestionPanel for creating the flag image and input
	 * box for answer
	 */
	private void createQuestionPanel() {
		questionPanel = new Composite(shell, SWT.BORDER);
		questionPanel.setLayoutData(
				new GridData(GridData.FILL, GridData.FILL, true, true));
		questionPanel.setLayout(new GridLayout(2, true));

		// Initially, only displays a message
		startupMessageLabel = new Label(questionPanel, SWT.NONE);
		startupMessageLabel.setText("No flag to display, yet.");
		startupMessageLabel.setLayoutData(GUIUtils.createFillGridData(2));
	}

	/**
	 * Serves to display the flag image and input box for answer
	 */
	private void updateQuestionPanel(String flagImageFullPath,
			String countryName) {
		// clear the panel
		Control[] children = questionPanel.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		// create the instruction label
		instructionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		instructionLabel.setText("Guess the country for the following flag:");
		instructionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the flag image:
		Image flagImage = new Image(Display.getCurrent(), flagImageFullPath);
		flagImageLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		flagImageLabel.setImage(flagImage);
		flagImageLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the answer buttons
		Text answerGuessText = new Text(questionPanel, SWT.SINGLE | SWT.BORDER);
		answerGuessText.setText("Guess...");
		answerGuessText.setLayoutData(GUIUtils.createFillGridData(1));

		Button checkAnswerButton = new Button(questionPanel,
				SWT.PUSH | SWT.WRAP);
		checkAnswerButton.setText("Check");
		GridData answerLayoutData = GUIUtils.createFillGridData(1);
		answerLayoutData.verticalAlignment = SWT.FILL;
		checkAnswerButton.setLayoutData(answerLayoutData);
		checkAnswerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!answerGuessText.getText().equalsIgnoreCase(countryName)) {
					score -= 2;
					if (++fails == MAX_ERRORS) {
						GUIUtils.showInfoDialog(shell, "Game over!",
								"Your final score is " + score + " after "
										+ attempts + " questions.");
						return;
					}
				} else {
					score += 3;
					successes++;
					fails = 0;
					nextQuestion();
				}
			}
		});
		// create the "Pass" button to skip a question
		passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.END, GridData.CENTER, true,
				false);
		data.horizontalSpan = 1;
		passButton.setLayoutData(data);
		if (score <= 0 && !firstPass) {
			passButton.setEnabled(false);
		}
		passButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (firstPass) {
					firstPass = false;
				} else {
					score -= 1;
				}
				attempts--;
				nextQuestion();
			}
		});
		// create the "Get-a-Letter" button to show a letter
		getALetterButton = new Button(questionPanel, SWT.PUSH);
		getALetterButton.setText("Get-a-Letter");
		data = new GridData(GridData.BEGINNING, GridData.CENTER, true, false);
		data.horizontalSpan = 1;
		getALetterButton.setLayoutData(data);
		if (score <= 0 && !firstGetALetter) {
			getALetterButton.setEnabled(false);
		}
		getALetterButtonCounter = 0;
		getALetterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Random random = new Random();
				int index = random.nextInt(countryName.length());
				char c = countryName.charAt(index++);
				GUIUtils.showInfoDialog(shell, "Get-a-Letter",
						"The " + index + "th letter is " + c);
				if (firstGetALetter) {
					firstGetALetter = false;

				} else {
					score -= 1;
				}
				if(++getALetterButtonCounter == 2 || score <= 0){
					getALetterButton.setEnabled(false);
				}
			}
		});
		// two operations to make the new widgets display properly
		questionPanel.pack();
		questionPanel.getParent().layout();

	}

	/**
	 * Opens the main window and executes the event loop of the application
	 */
	private void runApplication() {
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
	}

}
