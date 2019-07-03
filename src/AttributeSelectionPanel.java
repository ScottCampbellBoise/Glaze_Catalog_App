import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AttributeSelectionPanel extends JPanel {
	private GlazeSearchPanel masterSearchPanel;
	private GlazeEditPanel masterEditPanel;

	String[] colorArray = { "Black", "White", "Tan", "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Other" };
	String[] lowerConeArray = { "010-", "09", "08", "07", "06", "05", "04", "03", "02", "01", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "10", "11" };
	String[] upperConeArray = { "09", "08", "07", "06", "05", "04", "03", "02", "01", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "10", "11", "12+" };
	String[] firingArray = { "Unknown", "Ox.", "Red.", "Salt", "Soda", "Wood", "Other" };
	String[] finishArray = { "Unknown", "Matte", "Glossy", "Other" };
	String[] reliabilityArray = { "Unknown", "1 - Unreliable", "2", "3", "4", "5", "6", "7", "8", "9",
			"10 - Very Reliable" };
	String[] functionalityArray = { "Unknown", "Decorative", "Food Safe", "General", "Toxic", "Textured", "Other" };
	String[] stabilityArray = { "Unknown", "1 - Unstable", "2", "3", "4", "5", "6", "7", "8", "9", "10 - Very Stable" };
	String[] combinationArray = { "Unknown", "Good", "Ok", "Dependent", "Bad", "Other" };

	JLabel colorLabel, coneLabel, firingLabel, finishLabel, reliabilityLabel, functionalityLabel, stabilityLabel,
			combinationLabel;
	JComboBox<String> colorComboBox, lowerConeComboBox, upperConeComboBox, firingComboBox, finishComboBox,
			reliabilityComboBox, functionalityComboBox, stabilityComboBox, combinationComboBox;
	JButton colorAddButton, coneAddButton, firingAddButton, finishAddButton, reliabilityAddButton,
			functionalityAddButton, stabilityAddButton, combinationAddButton;

	JPanel coneSelectionPanel;

	private final int BUTTON_WIDTH = 70;

	public AttributeSelectionPanel(GlazeSearchPanel masterPanel) {
		masterSearchPanel = masterPanel;
		masterEditPanel = null;
		createPanel();
	}

	public AttributeSelectionPanel(GlazeEditPanel masterPanel) {
		masterEditPanel = masterPanel;
		masterSearchPanel = null;
		createPanel();
	}

	private void createPanel() {
		colorLabel = new JLabel("Glaze Color: ");
		colorComboBox = new JComboBox<String>(colorArray);
		colorComboBox.setSelectedIndex(0);
		colorAddButton = new JButton("Add");
		colorAddButton.addActionListener(new AttributeListener());
		colorAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new BorderLayout());
		JPanel colorSubPanel = new JPanel();
		colorSubPanel.setLayout(new BorderLayout());
		colorSubPanel.add(colorComboBox, BorderLayout.WEST);
		colorSubPanel.add(colorAddButton, BorderLayout.EAST);
		colorPanel.add(colorLabel, BorderLayout.WEST);
		colorPanel.add(colorSubPanel, BorderLayout.CENTER);

		lowerConeComboBox = new JComboBox<String>(lowerConeArray);
		lowerConeComboBox.setSelectedIndex(15);

		upperConeComboBox = new JComboBox<String>(upperConeArray);
		upperConeComboBox.setSelectedIndex(20);

		firingLabel = new JLabel("Firing Type: ");
		firingComboBox = new JComboBox<String>(firingArray);
		firingComboBox.setSelectedIndex(0);
		firingAddButton = new JButton("Add");
		firingAddButton.addActionListener(new AttributeListener());
		firingAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel firingPanel = new JPanel();
		firingPanel.setLayout(new BorderLayout());
		JPanel firingSubPanel = new JPanel();
		firingSubPanel.setLayout(new BorderLayout());
		firingSubPanel.add(firingComboBox, BorderLayout.WEST);
		firingSubPanel.add(firingAddButton, BorderLayout.EAST);
		firingPanel.add(firingLabel, BorderLayout.WEST);
		firingPanel.add(firingSubPanel, BorderLayout.CENTER);

		finishLabel = new JLabel("Glaze Finish: ");
		finishComboBox = new JComboBox<String>(finishArray);
		finishComboBox.setSelectedIndex(0);
		finishAddButton = new JButton("Add");
		finishAddButton.addActionListener(new AttributeListener());
		finishAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel finishPanel = new JPanel();
		finishPanel.setLayout(new BorderLayout());
		JPanel finishSubPanel = new JPanel();
		finishSubPanel.setLayout(new BorderLayout());
		finishSubPanel.add(finishComboBox, BorderLayout.WEST);
		finishSubPanel.add(finishAddButton, BorderLayout.EAST);
		finishPanel.add(finishLabel, BorderLayout.WEST);
		finishPanel.add(finishSubPanel, BorderLayout.CENTER);

		reliabilityLabel = new JLabel("Reliability: ");
		reliabilityComboBox = new JComboBox<String>(reliabilityArray);
		reliabilityComboBox.setSelectedIndex(0);
		reliabilityAddButton = new JButton("Add");
		reliabilityAddButton.addActionListener(new AttributeListener());
		reliabilityAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel reliabilityPanel = new JPanel();
		reliabilityPanel.setLayout(new BorderLayout());
		JPanel reliabilitySubPanel = new JPanel();
		reliabilitySubPanel.setLayout(new BorderLayout());
		reliabilitySubPanel.add(reliabilityComboBox, BorderLayout.WEST);
		reliabilitySubPanel.add(reliabilityAddButton, BorderLayout.EAST);
		reliabilityPanel.add(reliabilityLabel, BorderLayout.WEST);
		reliabilityPanel.add(reliabilitySubPanel, BorderLayout.CENTER);

		functionalityLabel = new JLabel("Functionality: ");
		functionalityComboBox = new JComboBox<String>(functionalityArray);
		functionalityComboBox.setSelectedIndex(0);
		functionalityAddButton = new JButton("Add");
		functionalityAddButton.addActionListener(new AttributeListener());
		functionalityAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel functionalityPanel = new JPanel();
		functionalityPanel.setLayout(new BorderLayout());
		JPanel functionalitySubPanel = new JPanel();
		functionalitySubPanel.setLayout(new BorderLayout());
		functionalitySubPanel.add(functionalityComboBox, BorderLayout.WEST);
		functionalitySubPanel.add(functionalityAddButton, BorderLayout.EAST);
		functionalityPanel.add(functionalityLabel, BorderLayout.WEST);
		functionalityPanel.add(functionalitySubPanel, BorderLayout.CENTER);

		stabilityLabel = new JLabel("Stability: ");
		stabilityComboBox = new JComboBox<String>(stabilityArray);
		stabilityComboBox.setSelectedIndex(0);
		stabilityAddButton = new JButton("Add");
		stabilityAddButton.addActionListener(new AttributeListener());
		stabilityAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel stabilityPanel = new JPanel();
		stabilityPanel.setLayout(new BorderLayout());
		JPanel stabilitySubPanel = new JPanel();
		stabilitySubPanel.setLayout(new BorderLayout());
		stabilitySubPanel.add(stabilityComboBox, BorderLayout.WEST);
		stabilitySubPanel.add(stabilityAddButton, BorderLayout.EAST);
		stabilityPanel.add(stabilityLabel, BorderLayout.WEST);
		stabilityPanel.add(stabilitySubPanel, BorderLayout.CENTER);

		combinationLabel = new JLabel("As a Combination: ");
		combinationComboBox = new JComboBox<String>(combinationArray);
		combinationComboBox.setSelectedIndex(0);
		combinationAddButton = new JButton("Add");
		combinationAddButton.addActionListener(new AttributeListener());
		combinationAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel combinationPanel = new JPanel();
		combinationPanel.setLayout(new BorderLayout());
		JPanel combinationSubPanel = new JPanel();
		combinationSubPanel.setLayout(new BorderLayout());
		combinationSubPanel.add(combinationComboBox, BorderLayout.WEST);
		combinationSubPanel.add(combinationAddButton, BorderLayout.EAST);
		combinationPanel.add(combinationLabel, BorderLayout.WEST);
		combinationPanel.add(combinationSubPanel, BorderLayout.CENTER);

		coneLabel = new JLabel("Cone Range: ");
		coneSelectionPanel = new JPanel();
		coneSelectionPanel.setLayout(new BorderLayout());
		coneSelectionPanel.add(lowerConeComboBox, BorderLayout.WEST);
		coneSelectionPanel.add(new JLabel(" to "), BorderLayout.CENTER);
		coneSelectionPanel.add(upperConeComboBox, BorderLayout.EAST);
		coneAddButton = new JButton("Add");
		coneAddButton.addActionListener(new AttributeListener());
		coneAddButton.setPreferredSize(new Dimension(BUTTON_WIDTH, 20));

		JPanel conePanel = new JPanel();
		conePanel.setLayout(new BorderLayout());
		JPanel coneSubPanel = new JPanel();
		coneSubPanel.setLayout(new BorderLayout());
		coneSubPanel.add(coneSelectionPanel, BorderLayout.WEST);
		coneSubPanel.add(coneAddButton, BorderLayout.EAST);
		conePanel.add(coneLabel, BorderLayout.WEST);
		conePanel.add(coneSubPanel, BorderLayout.CENTER);

		setLayout(new GridLayout(8, 1));
		add(colorPanel);
		add(conePanel);
		add(firingPanel);
		add(finishPanel);
		add(reliabilityPanel);
		add(functionalityPanel);
		add(stabilityPanel);
		add(combinationPanel);
	}

	public class AttributeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == colorAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Color: " + (String) colorComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Color: " + (String) colorComboBox.getSelectedItem());
				}
			} else if (e.getSource() == coneAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Cone Range: " + (String) lowerConeComboBox.getSelectedItem()
							+ " to " + (String) upperConeComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Cone Range: " + (String) lowerConeComboBox.getSelectedItem() + " to "
							+ (String) upperConeComboBox.getSelectedItem());
				}
			} else if (e.getSource() == firingAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Firing: " + (String) firingComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Firing: " + (String) firingComboBox.getSelectedItem());
				}
			} else if (e.getSource() == finishAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Finish: " + (String) finishComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Finish: " + (String) finishComboBox.getSelectedItem());
				}
			} else if (e.getSource() == reliabilityAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Reliability: " + (String) reliabilityComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Reliability: " + (String) reliabilityComboBox.getSelectedItem());
				}
			} else if (e.getSource() == functionalityAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel
							.addAttribute("Functionality: " + (String) functionalityComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Functionality: " + (String) functionalityComboBox.getSelectedItem());
				}
			} else if (e.getSource() == stabilityAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("Stability: " + (String) stabilityComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("Stability: " + (String) stabilityComboBox.getSelectedItem());
				}
			} else if (e.getSource() == combinationAddButton) {
				if (masterSearchPanel != null) {
					masterSearchPanel.addAttribute("As Combo: " + (String) combinationComboBox.getSelectedItem());
				} else {
					masterEditPanel.addAttribute("As Combo: " + (String) combinationComboBox.getSelectedItem());
				}
			}
		}
	}
}
