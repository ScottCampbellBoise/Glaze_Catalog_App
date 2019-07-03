import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class GlazeEditPanel extends JPanel {
	private GlazeRecipe recipe;
	private GlazeGUI masterGUI;
	private JFrame masterFrame;

	private Color backgroundColor = UIManager.getColor("Panel.background");

	String[] lowerConeArray = { "010-", "09", "08", "07", "06", "05", "04", "03", "02", "01", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "10", "11" };
	String[] upperConeArray = { "09", "08", "07", "06", "05", "04", "03", "02", "01", "1", "2", "3", "4", "5", "6", "7",
			"8", "9", "10", "11", "12+" };
	String[] firingArray = { "Unknown", "Ox.", "Red.", "Salt", "Soda", "Wood", "Other" };
	JComboBox<String> lowerConeComboBox, upperConeComboBox;

	private JPanel titlePanel;
	private JTextField nameField;
	private JPanel firingPanel;
	private JPanel firingContents;
	private JPanel coneSelectPanel;
	private JButton addFiringButton;

	private JPanel allCompPanel;
	private JPanel componentPanel;
	private JPanel[] components;
	private JPanel addPanel;
	private JPanel[] adds;
	private JButton addComponentButton;
	private JButton addAddButton;
	private EditablePhotoPanel imagePanel;

	private JPanel commentsPanel;
	private JTextArea commentsTextArea;

	private final int NUM_ATTRIBUTES = 24;
	private GlazeAttribute[] attributes;
	private JPanel attributesPanel;
	private JButton addAttributeButton;
	private AttributeSelectionPanel attributeSelectPanel;

	private final int MAX_FIRING_TYPES = 6;
	private FiringLabel[] firingLabels;

	private MessagePanel messagePanel = new MessagePanel();
	private final String[] ILLEGAL_CHARACTERS = { ".", "/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*", "\\", "<", ">",
			"|", "\"", ":" };

	private PDF_Generator_v2 pdf_generator = new PDF_Generator_v2();

	private Font sectionFont = new Font("Helvetica", Font.BOLD, 15);

	public GlazeEditPanel(GlazeGUI theGUI, JFrame masterFrame) // new glaze
																// constructor
	{
		this.masterGUI = theGUI;
		this.masterFrame = masterFrame;
		this.recipe = new GlazeRecipe();
		setUIFont(new javax.swing.plaf.FontUIResource("Times", Font.BOLD, 12));
		createPanel();
		validate();
		repaint();

	}

	public GlazeEditPanel(GlazeGUI theGUI, JFrame masterFrame, GlazeRecipe recipe) { // Edit
																						// an
																						// existing
																						// recipe
		this.masterGUI = theGUI;
		this.masterFrame = masterFrame;
		this.recipe = recipe;
		updateViewLog();
		setUIFont(new javax.swing.plaf.FontUIResource("Helvetica", Font.PLAIN, 12));
		createPanel();
		validate();
		repaint();

	}

	public void setUIFont(javax.swing.plaf.FontUIResource f) {
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}

	public void createPanel() {
		titlePanel = new JPanel();
		titlePanel.setBackground(backgroundColor);
		titlePanel.setBorder(new EmptyBorder(10, 5, 10, 5));
		nameField = new JTextField("  " + recipe.getName(), 12);
		nameField.setFont(new Font(nameField.getName(), Font.PLAIN, 24));
		nameField.setMargin(new Insets(20, 10, 10, 10));
		nameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int length = nameField.getText().length();
				if (length > 1) {
					String lastChar = nameField.getText().substring(length - 1, length);
					for (String c : ILLEGAL_CHARACTERS) {
						if (lastChar.equals(c)) {
							// remove from field, display error message
							nameField.setText(nameField.getText().substring(0, length - 1));
							messagePanel.displayMsg("Illegal Character! Please choose a valid character",
									MessagePanel.ERROR_MESSAGE);
							break;
						}
					}
				}
			}
		});

		lowerConeComboBox = new JComboBox<String>(lowerConeArray);
		lowerConeComboBox.setSelectedItem(recipe.getLowerCone().trim());
		lowerConeComboBox.setFont(new Font(lowerConeComboBox.getName(), Font.PLAIN, 16));

		upperConeComboBox = new JComboBox<String>(upperConeArray);
		upperConeComboBox.setSelectedItem(recipe.getUpperCone().trim());
		upperConeComboBox.setFont(new Font(upperConeComboBox.getName(), Font.PLAIN, 16));

		addFiringButton = new JButton("Add");
		addFiringButton.setPreferredSize(new Dimension(70, 25));
		addFiringButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> firingTypeComboBox = new JComboBox<String>(firingArray);
				firingTypeComboBox.setEditable(true);
				JOptionPane.showMessageDialog(null, firingTypeComboBox, "Select a Firing Type",
						JOptionPane.QUESTION_MESSAGE);
				String selectedFiringType = (String) firingTypeComboBox.getSelectedItem();
				if (selectedFiringType != null) {
					addFiringLabel(selectedFiringType);
				}
			}
		});

		firingPanel = new JPanel();
		TitledBorder firingBorder = new TitledBorder("Firing Types");
		firingBorder.setTitleFont(sectionFont);
		firingPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), firingBorder));
		firingContents = parseFiring();
		firingPanel.add(firingContents);

		coneSelectPanel = new JPanel();
		coneSelectPanel.setBackground(backgroundColor);
		JLabel coneLabel = new JLabel("to");
		coneLabel.setHorizontalAlignment(JLabel.CENTER);
		coneSelectPanel.setLayout(new GridLayout(1, 3));
		coneSelectPanel.add(lowerConeComboBox);
		coneSelectPanel.add(coneLabel);
		coneSelectPanel.add(upperConeComboBox);
		TitledBorder coneSelectBorder = new TitledBorder("Cone Range");
		coneSelectBorder.setTitleFont(sectionFont);
		coneSelectPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), coneSelectBorder));

		JPanel coneAndFiringPanel = new JPanel();
		coneAndFiringPanel.setBackground(backgroundColor);
		coneAndFiringPanel.setLayout(new GridLayout(1, 2));
		coneAndFiringPanel.add(coneSelectPanel);
		coneAndFiringPanel.add(firingPanel);

		titlePanel.setLayout(new BorderLayout());
		titlePanel.add(nameField, BorderLayout.WEST);
		titlePanel.add(coneAndFiringPanel, BorderLayout.EAST);

		addComponentButton = new JButton("Insert New Component");
		addComponentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateComponentsPanel(true);
			}
		});

		componentPanel = new JPanel();
		componentPanel.setBackground(backgroundColor);
		TitledBorder componentsBorder = new TitledBorder("Components");
		componentsBorder.setTitleFont(sectionFont);
		componentPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 15, 5, 10), componentsBorder));
		components = parseComponents(recipe.getComponents(), false);
		if (components == null) {
			componentPanel.setLayout(new GridLayout(5, 1));
			for (int k = 0; k < 4; k++) {
				componentPanel.add(new ComponentPanel(false));
			}
		} else if (components.length < 4) {
			componentPanel.setLayout(new GridLayout(5, 1));
			int count = 0;
			for (int k = 0; k < components.length; k++) {
				componentPanel.add(components[k]);
				count++;
			}
			for (int k = count; k < 4; k++) {
				componentPanel.add(new ComponentPanel(false));
			}
		} else {
			componentPanel.setLayout(new GridLayout(components.length + 1, 1));
			for (int k = 0; k < components.length; k++) {
				componentPanel.add(components[k]);
			}
		}
		componentPanel.add(addComponentButton);

		addAddButton = new JButton("Insert New Add");
		addAddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateAddsPanel(true);
			}
		});

		addPanel = new JPanel();
		addPanel.setBackground(backgroundColor);
		TitledBorder addsBorder = new TitledBorder("Adds");
		addsBorder.setTitleFont(sectionFont);
		addPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 15, 5, 10), addsBorder));
		adds = parseComponents(recipe.getAdds(), true);
		if (adds == null) {
			addPanel.setLayout(new GridLayout(3, 1));
			for (int k = 0; k < 2; k++) {
				addPanel.add(new ComponentPanel(true));
			}
		} else if (adds.length < 2) {
			addPanel.setLayout(new GridLayout(3, 1));
			int count = 0;
			for (int k = 0; k < adds.length; k++) {
				addPanel.add(adds[k]);
				count++;
			}
			for (int k = count; k < 2; k++) {
				addPanel.add(new ComponentPanel(true));
			}
		} else {
			addPanel.setLayout(new GridLayout(adds.length + 1, 1));
			for (int k = 0; k < adds.length; k++) {
				addPanel.add(adds[k]);
			}
		}
		addPanel.add(addAddButton);

		JPanel ingredientsPanel = new JPanel();
		ingredientsPanel.setBackground(backgroundColor);
		ingredientsPanel.setLayout(new BorderLayout());
		ingredientsPanel.add(componentPanel, BorderLayout.CENTER);
		ingredientsPanel.add(addPanel, BorderLayout.SOUTH);

		imagePanel = new EditablePhotoPanel(recipe.getPhotos());

		JPanel ingrAndImagePanel = new JPanel();
		ingrAndImagePanel.setBackground(backgroundColor);
		ingrAndImagePanel.setLayout(new BorderLayout());
		ingrAndImagePanel.add(ingredientsPanel, BorderLayout.CENTER);
		ingrAndImagePanel.add(imagePanel, BorderLayout.EAST);

		commentsPanel = new JPanel();
		commentsPanel.setBackground(backgroundColor);
		commentsPanel.setLayout(new BorderLayout());
		TitledBorder commentsBorder = new TitledBorder("Comments");
		commentsBorder.setTitleFont(sectionFont);
		commentsPanel.setBorder(BorderFactory.createCompoundBorder(commentsBorder, new EmptyBorder(5, 5, 5, 5)));
		commentsTextArea = new JTextArea(4, 60);
		commentsTextArea.setMargin(new Insets(10, 10, 10, 10));
		commentsTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
		commentsTextArea.setText(recipe.getComments().trim());
		JScrollPane commentsTextAreaScrollPane = new JScrollPane(commentsTextArea);
		commentsPanel.add(commentsTextAreaScrollPane, BorderLayout.CENTER);

		attributesPanel = new JPanel();
		attributesPanel.setBackground(backgroundColor);
		TitledBorder attributesBorder = new TitledBorder("Glaze Attributes");
		attributesBorder.setTitleFont(sectionFont);
		attributesPanel.setBorder(attributesBorder);
		addAttributeButton = new JButton("Add New Attribute");
		addAttributeButton.setPreferredSize(new Dimension(50, 20));
		addAttributeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestAttributes();
			}
		});
		parseAttributes();
		updateAttributes();

		JPanel commentsAndAttributesPanel = new JPanel();
		commentsAndAttributesPanel.setBackground(backgroundColor);
		commentsAndAttributesPanel.setLayout(new BorderLayout());
		commentsAndAttributesPanel.setBorder(new EmptyBorder(5, 15, 10, 15));
		commentsAndAttributesPanel.add(commentsPanel, BorderLayout.CENTER);
		commentsAndAttributesPanel.add(attributesPanel, BorderLayout.SOUTH);

		allCompPanel = new JPanel();
		allCompPanel.setBackground(backgroundColor);
		allCompPanel.setLayout(new BorderLayout());
		allCompPanel.add(titlePanel, BorderLayout.NORTH);
		allCompPanel.add(ingrAndImagePanel, BorderLayout.SOUTH);

		JPanel allRecipePanel = new JPanel(); // This panel holds all the glaze
												// related items
		allRecipePanel.setBackground(backgroundColor);
		allRecipePanel.setLayout(new BorderLayout());
		allRecipePanel.add(allCompPanel, BorderLayout.CENTER);
		allRecipePanel.add(commentsAndAttributesPanel, BorderLayout.SOUTH);

		JScrollPane allRecipeScroll = new JScrollPane(allRecipePanel);

		JButton saveButton = new JButton("Save Glaze");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveChanges();
			}
		});

		JButton duplicateButton = new JButton("Duplicate Glaze");
		duplicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				duplicateGlaze();
			}
		});

		JButton exportButton = new JButton("Export Recipe");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportGlaze();
			}
		});

		JButton printPreviewButton = new JButton("Print Preview");
		printPreviewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayPreview();
			}
		});

		JButton removeRecipeButton = new JButton("Delete Glaze");
		removeRecipeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteRecipe();
			}
		});

		// Create a panel to hold the save, duplicate, and print buttons
		JPanel modifyButtonsPanel = new JPanel();
		modifyButtonsPanel.setBackground(backgroundColor);
		modifyButtonsPanel.setLayout(new GridLayout(1, 5));
		modifyButtonsPanel.add(duplicateButton);
		modifyButtonsPanel.add(exportButton);
		modifyButtonsPanel.add(printPreviewButton);
		modifyButtonsPanel.add(saveButton);
		modifyButtonsPanel.add(removeRecipeButton);

		JPanel modifyAndMessagePanel = new JPanel();
		modifyAndMessagePanel.setBackground(backgroundColor);
		modifyAndMessagePanel.setLayout(new GridLayout(2, 1));
		modifyAndMessagePanel.add(messagePanel);
		modifyAndMessagePanel.add(modifyButtonsPanel);

		setLayout(new BorderLayout());
		add(allRecipeScroll, BorderLayout.CENTER);
		// add(allRecipePanel, BorderLayout.CENTER);
		add(modifyAndMessagePanel, BorderLayout.SOUTH);

	}

	private void displayPreview() {
		JFrame previewFrame = new JFrame("Print Preview: " + recipe.getName());
		previewFrame.setSize(600, 800);
		previewFrame.setResizable(false);
		previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		BufferedImage theImage = GlazeRecipe.NULL_IMAGE;

		try {
			pdf_generator.saveRecipeAsPDF(recipe, "temp_pdf.pdf");

			PDDocument document = PDDocument.load(new File("temp_pdf.pdf"));
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			theImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
			document.close();

		} catch (Exception e) {
			System.out.println("Error reading the temporary PDF file...");
			e.printStackTrace();
		}

		class MyImagePanel extends JPanel {
			private BufferedImage img;
			private int w, h;

			public MyImagePanel(BufferedImage img, int w, int h) {
				this.img = img;
				this.w = w;
				this.h = h;
			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img, 0, 0, w, h, null);
			}
		}

		previewFrame.add(new MyImagePanel(theImage, previewFrame.getWidth(), previewFrame.getHeight()));
		previewFrame.setVisible(true);

	}

	private void openEditPanel(GlazeRecipe theRecipe) {
		JFrame editFrame = new JFrame("Recipe Editor: " + theRecipe.getName());
		GlazeEditPanel ep = new GlazeEditPanel(masterGUI, editFrame, theRecipe);
		
		editFrame.setSize(540, 360);
		editFrame.setResizable(true);
		editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent e) {
		    	if (JOptionPane.showConfirmDialog(null, "Do you want to save changes?", "Warning",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					ep.saveChanges();
				}
		        e.getWindow().dispose();
		    	System.gc();
		    }
		});
		editFrame.add(ep);
		editFrame.pack();
		editFrame.setVisible(true);
	}
	
	private void exportGlaze() {
		if (JOptionPane.showConfirmDialog(null, "Do you want to save changes?", "Warning",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			saveChanges();
		}

		String userDir = System.getProperty("user.home");
		JFileChooser chooser = new JFileChooser(userDir + "/Desktop");
		chooser.setFileFilter(new FileNameExtensionFilter("pdf file", "pdf"));
		int returnVal = chooser.showSaveDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File theFile = new File(chooser.getSelectedFile().getAbsolutePath() + ".pdf");

			if (theFile.exists()) {
				int reply = JOptionPane.showConfirmDialog(null,
						"The File name is already in use. Do you want to over-write the file?", "Warning!",
						JOptionPane.YES_NO_OPTION);

				if (reply == JOptionPane.YES_OPTION) {
					boolean isSaved = pdf_generator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
							PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());
					if (!isSaved) {
						System.out.println("Could not save the recipe to the specified file...");
					}
				} else {
					exportGlaze();
				}
			} else {
				boolean isSaved = pdf_generator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
						PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());

				if (!isSaved) {
					System.out.println("Could not save the recipe to the specified file...");
				}
			}
		}

	}

	private void duplicateGlaze() {
		if (JOptionPane.showConfirmDialog(null, "Do you want to save changes?", "Warning",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			saveChanges();
		} else {
			masterGUI.updateMainPanel();
		}

		GlazeRecipe duplicatedRecipe = recipe.duplicateRecipe();
		if(duplicatedRecipe != null) {
			openEditPanel(duplicatedRecipe);
			messagePanel.displayMsg("Recipe file duplicated!", MessagePanel.GENERAL_MESSAGE);
		} else {
			messagePanel.displayMsg("Recipe file could not be duplicated!", MessagePanel.ERROR_MESSAGE);
		}

	}

	private void addNewPhoto() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				CropPhotoPanel cpp = new CropPhotoPanel(file.getAbsolutePath());
				cpp.addWindowListener(new WindowListener() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (cpp.getIsSaved()) {
							BufferedImage buffImage = cpp.getImage();
							String desc = cpp.getDescription();
							if (desc.trim().equals("")) {
								desc = "No description";
							}

							recipe.addNewPhoto(buffImage, desc);
							messagePanel.displayMsg("Successfully added the selected image!",
									MessagePanel.GENERAL_MESSAGE);
						}
					}

					@Override
					public void windowClosed(WindowEvent e) {
						if (cpp.getIsSaved()) {
							BufferedImage buffImage = cpp.getImage();
							String desc = cpp.getDescription();
							if (desc.trim().equals("")) {
								desc = "No description";
							}

							recipe.addNewPhoto(buffImage, desc);
							messagePanel.displayMsg("Successfully added the selected image!",
									MessagePanel.GENERAL_MESSAGE);
						}
					}

					@Override
					public void windowOpened(WindowEvent e) {
					}

					@Override
					public void windowIconified(WindowEvent e) {
					}

					@Override
					public void windowDeiconified(WindowEvent e) {
					}

					@Override
					public void windowActivated(WindowEvent e) {
					}

					@Override
					public void windowDeactivated(WindowEvent e) {
					}
				});
				masterGUI.updateMainPanel();

			} catch (Exception e) {
				messagePanel.displayMsg("Error reading the selected image file ...", MessagePanel.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}

	}

	private void deleteRecipe() {
		if (JOptionPane.showConfirmDialog(null, "This will permanently delete the recipe. Are you sure?", "Warning",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			masterFrame.dispose();
			boolean isDeleted = recipe.deleteRecipe();
			masterGUI.updateMainPanel();
		}

	}

	/**
	 * UPDATE TO BE MORE CAREFUL AND FOOLPROOF
	 */
	public void saveChanges() {
		// Push all the changes into the GlazeRecipe object and then save that
		// to a file
		String rawName = nameField.getText();
		if (rawName.substring(0, 1).equals("~")) {
			rawName = rawName.substring(1, rawName.length()).trim();
		}
		recipe.setName(rawName); // Update glaze name

		// Components and adds are already updated to the recipe object

		recipe.setConeRange((String) lowerConeComboBox.getSelectedItem(), (String) upperConeComboBox.getSelectedItem());

		// Firing types
		int count = 0;
		for (int k = 0; k < firingLabels.length; k++) {
			if (firingLabels[k] != null) {
				count++;
			}
		}
		String[] newFireLabels = new String[count];
		count = 0;
		for (int k = 0; k < firingLabels.length; k++) {
			if (firingLabels[k] != null) {
				newFireLabels[count] = firingLabels[k].getName();
				count++;
			}
		}
		recipe.setFiring(newFireLabels);

		ArrayList<String> newColors = new ArrayList<String>();
		ArrayList<String> newFinishes = new ArrayList<String>();
		ArrayList<String> newFunctionality = new ArrayList<String>();
		ArrayList<String> newCombos = new ArrayList<String>();

		for (GlazeAttribute ga : attributes) {
			if (ga != null && ga.getName().contains("Color:")) {
				newColors.add(ga.getName().substring(6, ga.getName().length()).trim());
			} else if (ga != null && ga.getName().contains("Finish:")) {
				newFinishes.add(ga.getName().substring(7, ga.getName().length()).trim());
			} else if (ga != null && ga.getName().contains("Reliability:")) {
				recipe.setReliability(ga.getName().substring(12, ga.getName().length()).trim());
			} else if (ga != null && ga.getName().contains("Functionality:")) {
				newFunctionality.add(ga.getName().substring(14, ga.getName().length()).trim());
			} else if (ga != null && ga.getName().contains("Stability:")) {
				recipe.setStability(ga.getName().substring(10, ga.getName().length()).trim());
			} else if (ga != null && ga.getName().contains("As Combo:")) {
				newCombos.add(ga.getName().substring(9, ga.getName().length()).trim());
			}
		}

		String[] colorArr = new String[newColors.size()];
		colorArr = newColors.toArray(colorArr);
		recipe.setColor(colorArr);
		colorArr = null;

		String[] finishesArr = new String[newFinishes.size()];
		finishesArr = newFinishes.toArray(finishesArr);
		recipe.setFinish(finishesArr);
		finishesArr = null;

		String[] funcArr = new String[newFunctionality.size()];
		funcArr = newFunctionality.toArray(funcArr);
		recipe.setFunctionality(funcArr);
		funcArr = null;

		String[] comboArr = new String[newCombos.size()];
		comboArr = newCombos.toArray(comboArr);
		recipe.setCombination(comboArr);
		comboArr = null;

		recipe.setComment(commentsTextArea.getText().trim());
		// photos are added automatically

		recipe.updateFile();
		masterGUI.updateMainPanel();
		nameField.setText(recipe.getName());
		messagePanel.displayMsg("All changes are saved!", MessagePanel.GENERAL_MESSAGE);

	}

	private void requestAttributes() {
		attributeSelectPanel = new AttributeSelectionPanel(this);
		JOptionPane.showMessageDialog(null, attributeSelectPanel, "Select Attributes", JOptionPane.INFORMATION_MESSAGE);

	}

	public void addAttribute(String attributeName) {
		// Check to see if it is a firing attribute - if so, add to the
		// FiringTypes box
		if (attributeName.contains("Firing:")) {
			addFiringLabel(attributeName.substring(7, attributeName.length()).trim());
		} else {
			boolean isAlreadyAdded = false;

			for (int k = 0; k < NUM_ATTRIBUTES; k++) {
				if (attributes[k] != null && attributes[k].getName().equals(attributeName)) {
					isAlreadyAdded = true;
				}
			}

			if (!isAlreadyAdded) {
				for (int k = 0; k < NUM_ATTRIBUTES; k++) {
					if (attributes[k] == null) {
						attributes[k] = new GlazeAttribute(attributeName);
						break;
					}
				}
			}
			updateAttributes();
		}
	}

	private void parseAttributes() {
		attributes = new GlazeAttribute[NUM_ATTRIBUTES];
		String[] colorAttributes = recipe.getColorAttribute();
		String[] finishesAttributes = recipe.getFinishAttribute();
		String reliabilityAttributes = recipe.getReliabilityAttribute();
		String[] functionalityAttribute = recipe.getFunctionalityAttribute();
		String stabilityAttributes = recipe.getStabilityAttribute();
		String[] combinationAttributes = recipe.getCombinationAttribute();

		int count = 0;
		for (int k = 0; k < colorAttributes.length && count < NUM_ATTRIBUTES; k++) {
			attributes[count] = new GlazeAttribute("Color: " + colorAttributes[k].trim());
			count++;
		}
		for (int k = 0; k < finishesAttributes.length && count < NUM_ATTRIBUTES; k++) {
			attributes[count] = new GlazeAttribute("Finish: " + finishesAttributes[k].trim());
			count++;
		}
		for (int k = 0; k < functionalityAttribute.length && count < NUM_ATTRIBUTES; k++) {
			attributes[count] = new GlazeAttribute("Functionality: " + functionalityAttribute[k].trim());
			count++;
		}
		for (int k = 0; k < combinationAttributes.length && count < NUM_ATTRIBUTES; k++) {
			attributes[count] = new GlazeAttribute("As Combo: " + combinationAttributes[k].trim());
			count++;
		}
		if (count < NUM_ATTRIBUTES) {
			attributes[count] = new GlazeAttribute("Reliability: " + reliabilityAttributes);
			count++;
		}
		if (count < NUM_ATTRIBUTES) {
			attributes[count] = new GlazeAttribute("Stability: " + stabilityAttributes + "");
			count++;
		}

	}

	private void updateAddsPanel(boolean addNewItem) {
		addPanel.removeAll();
		addPanel.revalidate();

		adds = parseComponents(recipe.getAdds(), true);
		if (adds == null) {
			addPanel.setLayout(new GridLayout(3, 1));
			for (int k = 0; k < 2; k++) {
				addPanel.add(new ComponentPanel(true));
			}
		} else if (adds.length < 2) {
			addPanel.setLayout(new GridLayout(3, 1));
			int count = 0;
			for (int k = 0; k < adds.length; k++) {
				addPanel.add(adds[k]);
				count++;
			}
			for (int k = count; k < 2; k++) {
				addPanel.add(new ComponentPanel(true));
			}
		} else {
			if (addNewItem) {
				addPanel.setLayout(new GridLayout(adds.length + 2, 1));
				for (int k = 0; k < adds.length; k++) {
					if (adds[k] != null) {
						addPanel.add(adds[k]);
					}
				}
				addPanel.add(new ComponentPanel(true));
			} else {
				addPanel.setLayout(new GridLayout(adds.length + 1, 1));
				for (int k = 0; k < adds.length; k++) {
					if (adds[k] != null) {
						addPanel.add(adds[k]);
					}
				}
			}
		}
		addPanel.add(addAddButton);
		/**
		 * addPanel.revalidate(); addPanel.repaint(); super.revalidate();
		 * super.repaint();
		 */

	}

	private void updateComponentsPanel(boolean addNewItem) {
		componentPanel.removeAll();
		componentPanel.revalidate();

		components = parseComponents(recipe.getComponents(), false);
		if (components == null) {
			componentPanel.setLayout(new GridLayout(5, 1));
			for (int k = 0; k < 4; k++) {
				componentPanel.add(new ComponentPanel(false));
			}
		} else if (components.length < 4) {
			componentPanel.setLayout(new GridLayout(5, 1));
			int count = 0;
			for (int k = 0; k < components.length; k++) {
				componentPanel.add(components[k]);
				count++;
			}
			for (int k = count; k < 4; k++) {
				componentPanel.add(new ComponentPanel(false));
			}
		} else {
			if (addNewItem) {
				componentPanel.setLayout(new GridLayout(components.length + 2, 1));
				for (int k = 0; k < components.length; k++) {
					if (components[k] != null) {
						componentPanel.add(components[k]);
					}
				}
				componentPanel.add(new ComponentPanel(false));
			} else {
				componentPanel.setLayout(new GridLayout(components.length + 1, 1));
				for (int k = 0; k < components.length; k++) {
					if (components[k] != null) {
						componentPanel.add(components[k]);
					}
				}
			}
		}
		componentPanel.add(addComponentButton);
		/**
		 * componentPanel.revalidate(); componentPanel.repaint();
		 * super.revalidate(); super.repaint();
		 */

	}

	private JPanel[] parseComponents(GlazeComponent[] components, boolean isAdd) {
		if (components != null) {
			JPanel[] compPanels = new JPanel[components.length];

			for (int k = 0; k < components.length; k++) {
				if (components[k] != null) {
					compPanels[k] = new ComponentPanel(components[k], isAdd);
				}
			}
			return compPanels;
		} else {
			return null;
		}
	}

	private JPanel parseFiring() {
		firingLabels = new FiringLabel[MAX_FIRING_TYPES];
		JPanel myPanel = new JPanel();
		String[] attributes = recipe.getFiringAttribute();

		for (int k = 0; k < attributes.length; k++) {
			FiringLabel myLabel = new FiringLabel(attributes[k] + "  ");
			myPanel.add(myLabel);
			firingLabels[k] = myLabel;
		}
		myPanel.add(addFiringButton);

		return myPanel;
	}

	private void updateFiringLabels() {
		firingPanel.remove(firingContents);
		firingContents = new JPanel();

		firingPanel.setLayout(new GridLayout(1, MAX_FIRING_TYPES + 1));

		for (int k = 0; k < MAX_FIRING_TYPES; k++) {
			if (firingLabels[k] != null) {
				firingContents.add(firingLabels[k]);
			}
		}
		firingContents.add(addFiringButton);

		firingPanel.add(firingContents, BorderLayout.EAST);
		firingPanel.validate();

	}

	private void addFiringLabel(String firingType) {
		boolean isAlreadyAdded = false;
		for (int k = 0; k < MAX_FIRING_TYPES; k++) {
			if (firingLabels[k] != null && firingLabels[k].getName().trim().equals(firingType)) {
				isAlreadyAdded = true;
			}
		}

		boolean isValidLabel = false;
		for (int k = 0; k < firingArray.length; k++) {
			if (firingArray[k].equals(firingType)) {
				isValidLabel = true;
			}
		}

		if (!isAlreadyAdded && isValidLabel) {
			for (int k = 0; k < MAX_FIRING_TYPES; k++) {
				if (firingLabels[k] == null) {
					firingLabels[k] = new FiringLabel(firingType + "  ");
					break;
				}
			}
		}
		updateFiringLabels();
	}

	private void updateAttributes() {
		attributesPanel.removeAll();
		attributesPanel.validate();

		TitledBorder attributesBorder = new TitledBorder("Glaze Attributes");
		attributesBorder.setTitleFont(sectionFont);
		attributesPanel.setBorder(attributesBorder);
		attributesPanel.setLayout(new BorderLayout());

		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new GridLayout(3, 8));

		for (int k = 0; k < NUM_ATTRIBUTES; k++) {
			if (attributes[k] != null) {
				lowerPanel.add(attributes[k]);
			}
		}

		attributesPanel.add(addAttributeButton, BorderLayout.SOUTH);
		attributesPanel.add(lowerPanel, BorderLayout.CENTER);

	}

	private void updateViewLog() {
		try {
			String fileContents = new String(Files.readAllBytes(Paths.get("view_log.txt")));
			String updatedContents = "";
			String[] glazeInfo = fileContents.split("@");
			boolean alreadyExists = false;
			for (String info : glazeInfo) {
				String[] viewInfo = info.trim().split("~");
				String name = viewInfo[0].toLowerCase().trim();
				if (!alreadyExists && name.equals(recipe.getName().toLowerCase().trim())) {
					alreadyExists = true;
					int numViews = Integer.parseInt(viewInfo[1].trim());
					numViews++;
					updatedContents = viewInfo[0].trim() + " ~ " + numViews + "@\n" + updatedContents;
				} else if (!info.trim().equals("")) {
					updatedContents += info.trim() + "@\n";
				}
			}

			if (!alreadyExists) {
				updatedContents = recipe.getName().trim() + " ~ " + "1@\n" + updatedContents;
			}
			PrintWriter output = new PrintWriter("view_log.txt");
			output.print(updatedContents);
			output.close();
		} catch (Exception e) {
			System.out.println("Error reading/writing view_log.txt");
			e.printStackTrace();
		}
	}

	private class MessagePanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		private JTextField messageField;
		private JButton exitButton;

		public static final int NO_MESSAGE = 0;
		public static final int ERROR_MESSAGE = 1;
		public static final int WARNING_MESSAGE = 2;
		public static final int GENERAL_MESSAGE = 3;

		private boolean isDisplayed = false;
		private Timer timer;
		private final int delay = 10000;

		public MessagePanel() {
			messageField = new JTextField("");
			messageField.setEnabled(false);

			exitButton = new JButton();
			exitButton.setIcon(new ImageIcon("exit_icon.png"));
			exitButton.setPreferredSize(new Dimension(20, 20));
			exitButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayMsg("", NO_MESSAGE);
				}
			});

			setBorder(new EmptyBorder(5, 5, 5, 5));
			setLayout(new BorderLayout());
			add(messageField, BorderLayout.CENTER);

			displayMsg("", 0);

			timer = new Timer(delay, this);

		}

		public void actionPerformed(ActionEvent e) {
			if (isDisplayed) {
				isDisplayed = false;
				timer.stop();
				displayMsg("", NO_MESSAGE);
			}
		}

		public void displayMsg(String msg, int msgType) {
			messageField.setText("\t" + msg);
			if (msgType == ERROR_MESSAGE) {
				messageField.setBackground(new Color(255, 179, 179, 100)); // Red
																			// Color
				addExitButton();
				isDisplayed = true;
				timer.start();
			} else if (msgType == WARNING_MESSAGE) {
				messageField.setBackground(new Color(255, 240, 179, 100)); // Yellow
																			// Color
				addExitButton();
				isDisplayed = true;
				timer.start();
			} else if (msgType == GENERAL_MESSAGE) {
				messageField.setBackground(new Color(125, 255, 104, 100)); // Green
																			// Color
				addExitButton();
				isDisplayed = true;
				timer.start();
			} else {
				messageField.setBackground(Color.LIGHT_GRAY);
				removeExitButton();
			}
			messageField.setForeground(Color.BLACK);
		}

		private void removeExitButton() {
			try {
				remove(exitButton);
				validate();
			} catch (Exception e) {

			}
		}

		private void addExitButton() {
			add(exitButton, BorderLayout.EAST);
		}
	}

	private class GlazeAttribute extends JPanel {
		private static final long serialVersionUID = 1L;
		private JLabel label;
		private JButton button;
		private String attributeName;

		public GlazeAttribute(String attributeName) {
			setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), new EtchedBorder()));
			this.attributeName = attributeName;
			label = new JLabel(attributeName);
			label.setHorizontalAlignment(SwingConstants.CENTER);
			label.setOpaque(true);
			label.setBackground(Color.WHITE);

			button = new JButton();
			button.setIcon(new ImageIcon("exit_icon.png"));
			button.setPreferredSize(new Dimension(20, 20));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (int k = 0; k < NUM_ATTRIBUTES; k++) {
						if (attributes[k] != null && attributes[k].getName().equals(attributeName)) {
							for (int i = k; i < NUM_ATTRIBUTES - 1; i++) {
								attributes[i] = attributes[i + 1];
							}
							break;
						}
					}
					updateAttributes();
				}
			});

			setLayout(new BorderLayout());
			add(label, BorderLayout.CENTER);
			add(button, BorderLayout.EAST);

		}

		public String getName() {
			return attributeName;
		}
	}

	private class ComponentPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JTextField componentField;
		private JTextField amtField;
		private JButton removeButton;

		private GlazeComponent comp;
		private boolean isAdd;

		private boolean isEmpty = false;
		private boolean noName = false;
		private boolean noAmt = false;

		private final Color unsavedColor = new Color(255, 240, 179);
		private final Color errorColor = new Color(255, 179, 179);
		private final Color savedColor = Color.WHITE;

		public ComponentPanel(boolean isAdd) {
			this.comp = new GlazeComponent();

			this.isEmpty = true;
			this.noName = true;
			this.noAmt = true;
			this.isAdd = isAdd;

			createContents();

			setLayout(new BorderLayout());
			add(componentField, BorderLayout.WEST);
			add(amtField, BorderLayout.CENTER);
			add(removeButton, BorderLayout.EAST);

		}

		public ComponentPanel(GlazeComponent comp, boolean isAdd) {
			this.comp = comp;
			this.isAdd = isAdd;

			createContents();

			setLayout(new BorderLayout());
			add(componentField, BorderLayout.WEST);
			add(amtField, BorderLayout.CENTER);
			add(removeButton, BorderLayout.EAST);

		}

		public void createContents() {
			if (isEmpty) {
				componentField = new JTextField("", 25);
			} else {
				componentField = new JTextField(" " + comp.getName().trim(), 25);
			}
			componentField.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					componentField.setBackground(unsavedColor);
					componentField.select(0, componentField.getText().length());
				}

				public void focusLost(FocusEvent e) {
					try {
						String newName = componentField.getText().trim();
						comp.setName(newName);
						if (newName != null && !newName.equals("")) {
							isEmpty = false;
							noName = false;
						}

						if (!noAmt && !noName) {
							componentField.setBackground(savedColor);
							if (isAdd) {
								recipe.addAdd(comp);
								updateAddsPanel(false);
							} else {
								recipe.addComponent(comp);
								updateComponentsPanel(false);
							}

						}
					} catch (Exception ex) {
						ex.printStackTrace();
						messagePanel.displayMsg("Error updating recipe file ...", MessagePanel.ERROR_MESSAGE);
					}
				}
			});
			componentField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					comp.setName(componentField.getText().trim());
					if (comp.getName() != null && !comp.getName().equals("")) {
						isEmpty = false;
						noName = false;
					}

					if (!noAmt && !noName) {
						if (isAdd) {
							recipe.addAdd(comp);
							updateAddsPanel(false);
						} else {
							recipe.addComponent(comp);
							updateComponentsPanel(false);
						}

					}
				}
			});

			if (isEmpty) {
				amtField = new JTextField("", 8);
			} else {
				amtField = new JTextField(comp.getAmount() + "", 8);
			}
			amtField.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					amtField.setBackground(unsavedColor);
					amtField.select(0, amtField.getText().length());
				}

				public void focusLost(FocusEvent e) {
					try {
						try {
							double newAmt = Double.parseDouble(amtField.getText());
							comp.setAmount(newAmt);
						} catch (Exception ex) {
							messagePanel.displayMsg("Amount must be a valid integer!", MessagePanel.ERROR_MESSAGE);
						}
						if (comp.getAmount() < 0) {
							comp.setAmount(0.0);
							noAmt = true;
							amtField.setText("");
						} else {
							noAmt = false;

							if (!noAmt && !noName) {
								amtField.setBackground(savedColor);
								if (isAdd) {
									recipe.addAdd(comp);
									updateAddsPanel(false);
								} else {
									recipe.addComponent(comp);
									updateComponentsPanel(false);
								}
							}
						}
					} catch (Exception ex2) {
						ex2.printStackTrace();
						messagePanel.displayMsg("Error updating the recipe file ...", MessagePanel.ERROR_MESSAGE);
					}
				}
			});
			amtField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						double newAmt = Double.parseDouble(amtField.getText());
						comp.setAmount(newAmt);
					} catch (Exception ex) {
						messagePanel.displayMsg("Amount must be a valid integer!", MessagePanel.ERROR_MESSAGE);
					}
					if (comp.getAmount() < 0) {
						comp.setAmount(0.0);
						noAmt = true;
						amtField.setText("");
					} else {
						noAmt = false;

						if (!noAmt && !noName) {
							if (isAdd) {
								recipe.addAdd(comp);
								updateAddsPanel(false);
							} else {
								recipe.addComponent(comp);
								updateComponentsPanel(false);
							}

						}
					}
				}
			});

			removeButton = new JButton();
			removeButton.setIcon(new ImageIcon("exit_icon.png"));
			removeButton.setPreferredSize(new Dimension(20, 20));
			removeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!noName && !noAmt) {
						if (!isAdd) {
							recipe.removeComponent(comp);
							updateComponentsPanel(false);
						} else {
							recipe.removeAdd(comp);
							updateAddsPanel(false);
						}

					} else {
						if (!isAdd) {
							updateComponentsPanel(false);
						} else {
							updateAddsPanel(false);
						}

					}
				}
			});

		}

		public String getName() {
			return comp.getName();
		}

		public double getAmount() {
			return comp.getAmount();
		}
	}

	private class FiringLabel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JLabel label;
		private JButton button;
		private String firingType;

		public FiringLabel(String firingType) {
			setBorder(BorderFactory.createEtchedBorder());
			this.firingType = firingType;
			label = new JLabel(firingType);
			label.setOpaque(true);
			label.setBackground(backgroundColor);

			button = new JButton();
			button.setIcon(new ImageIcon("exit_icon.png"));
			button.setPreferredSize(new Dimension(20, 20));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (int k = 0; k < MAX_FIRING_TYPES; k++) {
						if (firingLabels[k].getName().equals(firingType)) {
							for (int i = k; i < MAX_FIRING_TYPES - 1; i++) {
								firingLabels[i] = firingLabels[i + 1];
							}
							break;
						}
					}
					updateFiringLabels();
				}
			});

			setLayout(new BorderLayout());
			add(label, BorderLayout.CENTER);
			add(button, BorderLayout.EAST);

		}

		public String getName() {
			return firingType;
		}
	}

	private class EditablePhotoPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private GlazePhoto[] originalPhotos;
		private JButton prevPhotoButton;
		private JButton nextPhotoButton;

		private int photoPosition;

		public EditablePhotoPanel(GlazePhoto[] originalPhotos) {
			this.originalPhotos = originalPhotos;
			this.photoPosition = 0;
			TitledBorder photosBorder = new TitledBorder(null, "Glaze Photos", TitledBorder.CENTER, TitledBorder.TOP);
			photosBorder.setTitleFont(sectionFont);
			setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 15), photosBorder));

			prevPhotoButton = new JButton("<");
			prevPhotoButton.setPreferredSize(new Dimension(30, 30));
			prevPhotoButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					photoPosition = (recipe.getNumPhotos() + photoPosition - 1) % recipe.getNumPhotos();
					updatePanel();
				}
			});

			nextPhotoButton = new JButton(">");
			nextPhotoButton.setPreferredSize(new Dimension(30, 30));
			nextPhotoButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					photoPosition = (photoPosition + 1) % recipe.getNumPhotos();
					updatePanel();
				}
			});

			updatePanel();

		}

		private void updatePanel() {
			removeAll();
			originalPhotos = recipe.getPhotos();
			EditablePhoto currentPhoto = new EditablePhoto(originalPhotos[photoPosition]);
			setLayout(new BorderLayout());
			add(prevPhotoButton, BorderLayout.WEST);
			add(nextPhotoButton, BorderLayout.EAST);
			add(currentPhoto, BorderLayout.CENTER);
			validate();
			repaint();

		}

		private class EditablePhoto extends JPanel {
			private static final long serialVersionUID = 1L;
			private GlazePhoto photo;
			private String desc;
			private BufferedImage img;
			private TitledBorder border;
			private final int PHOTO_WIDTH = 160;
			private final int PHOTO_HEIGHT = 200;

			public EditablePhoto(GlazePhoto photo) {
				this.photo = photo;
				this.desc = photo.getDesc();
				this.img = resizePhoto(photo.getPhoto(), PHOTO_WIDTH, PHOTO_HEIGHT);
				createPhotoDescPanel();

			}

			private BufferedImage resizePhoto(BufferedImage original, int newWidth, int newHeight) {
				int w = original.getWidth();
				int h = original.getHeight();
				BufferedImage dimg = new BufferedImage(newWidth, newHeight, original.getType());
				Graphics2D g = dimg.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
				g.dispose();
				return dimg;
			}

			private void createPhotoDescPanel() {
				JLabel photoLabel = new JLabel(new ImageIcon(img));
				JPanel buttonPanel = new JPanel();
				JButton changeDescButton = new JButton("Change Description");
				JButton removePhotoButton = new JButton("Remove");
				JButton addPhotoButton = new JButton("Add Photo");

				addPhotoButton.setPreferredSize(new Dimension(100, 20));
				addPhotoButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addNewPhoto();
						originalPhotos = recipe.getPhotos();
					}
				});

				changeDescButton.setPreferredSize(new Dimension(140, 20));
				changeDescButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						NewPhotoDescPanel inputPanel = new NewPhotoDescPanel();
						JOptionPane.showMessageDialog(null, inputPanel, "Update Description",
								JOptionPane.INFORMATION_MESSAGE);
						String newDesc = inputPanel.getNewDesc();
						if (newDesc != null) {
							recipe.setPhotoDesc(photo.getPath(), newDesc);
							photo.setDesc(newDesc);
							border.setTitle(newDesc);
							validate();
							repaint();
						}
					}
				});

				removePhotoButton.setPreferredSize(new Dimension(90, 20));
				removePhotoButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int dialogResult = JOptionPane.showConfirmDialog(null,
								"This will permanently delete the photo... Are you sure?", "Warning",
								JOptionPane.YES_NO_OPTION);
						if (dialogResult == JOptionPane.YES_OPTION) {
							recipe.removePhoto(originalPhotos[photoPosition].getPath());
							originalPhotos = recipe.getPhotos();
							photoPosition = (recipe.getNumPhotos() + photoPosition - 1) % recipe.getNumPhotos();
							updatePanel();
						}
					}
				});

				buttonPanel.add(addPhotoButton);
				buttonPanel.add(changeDescButton);
				buttonPanel.add(removePhotoButton);
				buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

				border = new TitledBorder(desc);
				border.setTitleJustification(TitledBorder.CENTER);
				border.setTitlePosition(TitledBorder.BOTTOM);
				photoLabel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 5, 5, 5), border));

				setLayout(new BorderLayout());
				add(photoLabel, BorderLayout.CENTER);
				add(buttonPanel, BorderLayout.SOUTH);

			}
		}
	}
}