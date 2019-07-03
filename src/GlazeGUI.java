import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GlazeGUI extends JFrame {
	/**
	 * V4 - This version aims to improve the memory allocation of the app
	 * Additionally, the hope is to add some other nice functionality Close Edit
	 * window on delete Open new Edit window on duplicate Ask to save before
	 * closing window
	 *
	 * Clean up all the code!!! Comment add the Classes!
	 * 
	 * Make a Panel to display/add Glaze Layering Tests - IF TIME!!! Allow user
	 * to choose which glazes they want to see layered Allow print/export image
	 * file
	 * 
	 * GlazeEditPanel ... Check if the user has editing privlages before
	 * allowing them to make changes - IF TIME!!! Check if the components add up
	 * to 100. If not, offer a button to scale to 100
	 * 
	 * Make a 'main page' has a sign in option that allows the user to edit the
	 * recipes - IF TIME!!!
	 */

	private boolean hasEditPrivlages = true;
	public MainPanel mainPanel;
	private GlazeGUI masterGUI;

	public static void main(String[] args) {
		new GlazeGUI();
	}

	public GlazeGUI() {
		masterGUI = this;

		setTitle("Glaze Catalog - Search for a Glaze");
		setSize(850, 730);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		mainPanel = new MainPanel();
		add(mainPanel);

		setVisible(true);
	}

	public void updateMainPanel() {
		mainPanel.update();
	}

	private class MainPanel extends JPanel {
		private GlazePreviewPanel previewPanel;
		private RecentGlazePanel recentPanel;
		private SuggestedGlazePanel suggestedPanel;
		private SearchBar searchBar;

		private String[] recipeNames;
		private GlazeRecipe[] allRecipes;
		private GlazeRecipe[] recentRecipes;
		private GlazeRecipe[] topRecipes;

		private ArrayList<GlazeEditPanel> allEditPanels;
		private JButton exportButton;
		private JButton newButton;
		private JComboBox<String> searchBox;

		private final Color highlightColor = new Color(0, 153, 255, 190);
		private final Font sectionFont = new Font("Helvetica", Font.BOLD, 14);

		public MainPanel() {
			allRecipes = uploadRecipes();
			recipeNames = parseNames(allRecipes);
			uploadRecentRecipes();
			sortByViews();
			findTopRecentRecipes();
			allEditPanels = new ArrayList<GlazeEditPanel>();

			previewPanel = new GlazePreviewPanel();
			recentPanel = new RecentGlazePanel();
			suggestedPanel = new SuggestedGlazePanel();
			searchBar = new SearchBar();

			searchBox = new JComboBox<String>(parseNames(uploadRecipes()));
			searchBox.setEditable(true);
			searchBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openEditPanel((GlazeRecipe) getRecipeByName((String) searchBox.getSelectedItem()));
				}
			});

			exportButton = new JButton("Export Catalog as a PDF");
			exportButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame saveFrame = new JFrame("Export Glaze Catalog");
					saveFrame.setSize(700, 630);
					saveFrame.setResizable(true);
					saveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

					saveFrame.add(new PdfGenerationPanel());
					saveFrame.setVisible(true);
				}
			});

			newButton = new JButton("Create a New Recipe");
			newButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openEditPanel(new GlazeRecipe());
				}
			});

			JPanel exportAndNewButtonPanel = new JPanel();
			exportAndNewButtonPanel.setBorder(new EmptyBorder(0, 0, 20, 20));
			exportAndNewButtonPanel.setLayout(new BorderLayout());
			JPanel internalPanel = new JPanel();
			internalPanel.setLayout(new GridLayout(1, 2));
			internalPanel.add(exportButton);
			internalPanel.add(newButton);
			exportAndNewButtonPanel.add(internalPanel, BorderLayout.EAST);

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(previewPanel, BorderLayout.NORTH);
			JPanel mainAndRecent = new JPanel();
			mainAndRecent.setLayout(new GridLayout(2, 1));
			mainAndRecent.add(recentPanel);
			mainAndRecent.add(suggestedPanel);
			mainPanel.add(mainAndRecent, BorderLayout.CENTER);

			setLayout(new BorderLayout());
			add(searchBar, BorderLayout.NORTH);
			add(mainPanel, BorderLayout.CENTER);
			add(exportAndNewButtonPanel, BorderLayout.SOUTH);

			validate();
			repaint();

		}

		// Finds up to the top 5 recent recipes
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

		public void update() {
			allRecipes = uploadRecipes();
			recipeNames = parseNames(allRecipes);
			uploadRecentRecipes();
			sortByViews();
			findTopRecentRecipes();
		}

		private void findTopRecentRecipes() {
			if (recentRecipes.length < 5) {
				topRecipes = new GlazeRecipe[recentRecipes.length];
			} else {
				topRecipes = new GlazeRecipe[5];
			}
			for (int k = 0; k < topRecipes.length; k++) {
				topRecipes[k] = recentRecipes[k];
			}

		}

		private void sortByViews() {
			boolean isSwapped = false;
			do {
				isSwapped = false;
				for (int i = 0; i < recentRecipes.length - 1; i++) {
					if (recentRecipes[i].getViews() < recentRecipes[i + 1].getViews()) {
						GlazeRecipe temp = recentRecipes[i + 1];
						recentRecipes[i + 1] = recentRecipes[i];
						recentRecipes[i] = temp;
						isSwapped = true;
					}
				}
			} while ((isSwapped));

		}

		private void uploadRecentRecipes() {
			try {
				String fileContents = new String(Files.readAllBytes(Paths.get("view_log.txt")));
				String[] glazeInfo = fileContents.split("@");

				int count = 0;
				for (int k = 0; k < glazeInfo.length; k++) {
					String[] vals = glazeInfo[k].split("~");
					String glazeName = vals[0].trim();
					File glazeFile = new File("Glaze Recipes/" + glazeName);
					if (!glazeName.trim().equals("") && glazeFile.isDirectory()) {
						count++;
					}

				}
				recentRecipes = new GlazeRecipe[count];
				count = 0;
				for (int k = 0; k < glazeInfo.length; k++) {
					String[] vals = glazeInfo[k].split("~");
					String glazeName = vals[0].trim();
					File glazeFile = new File("Glaze Recipes/" + glazeName);
					if (!glazeName.trim().equals("") && glazeFile.isDirectory()) {
						recentRecipes[count] = new GlazeRecipe("Glaze Recipes/" + glazeName);
						recentRecipes[count].setViews(Integer.parseInt(vals[1].trim()));
						count++;
					}

				}
			} catch (Exception e) {
				System.out.println("Error reading from view_log.txt in GlazeGUI");
				e.printStackTrace();
			}
		}

		private GlazeRecipe[] uploadRecipes() {
			File directory = new File("Glaze Recipes/");
			ArrayList<GlazeRecipe> allRecipes = new ArrayList<GlazeRecipe>();

			File[] fList = directory.listFiles();
			if (fList != null) {
				for (File file : fList) {
					if (file.isDirectory()) {
						allRecipes.add(new GlazeRecipe(file.getAbsolutePath()));
					}
				}
			}
			GlazeRecipe[] recipeAry = new GlazeRecipe[allRecipes.size()];
			recipeAry = allRecipes.toArray(recipeAry);

			return recipeAry;
		}

		private String[] parseNames(GlazeRecipe[] gr) {
			String[] names = new String[gr.length];
			for (int k = 0; k < gr.length; k++) {
				names[k] = gr[k].getName();
			}

			return names;
		}

		private GlazeRecipe getRecipeByName(String name) {
			for (GlazeRecipe gr : allRecipes) {
				if (gr.getName().equals(name)) {
					return gr;
				}
			}
			return null;
		}

		private class SearchBar extends JPanel implements FocusListener {
			private final String searchHint = "Search for a Glaze...";
			private JTextField searchField;
			private JPanel resultsPanel;

			private JButton advancedSearchButton;

			private String[] suggestedResults;
			private boolean panelAdded = false;
			private final Color foregroundColor = Color.GRAY;

			public SearchBar() {
				suggestedResults = null;

				setBorder(new EmptyBorder(10, 20, 15, 20));
				resultsPanel = new JPanel();
				resultsPanel.setBorder(new EmptyBorder(0, 0, 0, 150));
				resultsPanel.setLayout(new GridLayout(5, 1));

				advancedSearchButton = new JButton("Advanced Search");
				advancedSearchButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JFrame advFrame = new JFrame("Glaze Catalog - Advanced Search");
						advFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						advFrame.setSize(800, 800);
						advFrame.setResizable(true);
						advFrame.add(new GlazeSearchPanel(masterGUI));
						advFrame.setVisible(true);

					}
				});

				searchField = new JTextField(20);
				searchField.setEditable(true);
				searchField.addFocusListener(this);
				searchField.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent ke) {
						if (!panelAdded) {
							add(resultsPanel);
							validate();
							repaint();
							panelAdded = true;
						}

						// check for new suggestions
						String text = searchField.getText();

						if (!text.equals("")) {
							String[] closestResults = getClosestMatches(text);
							if (!compareArrays(closestResults, suggestedResults)) {
								resultsPanel.removeAll();
								suggestedResults = closestResults;
								for (int k = 0; k < suggestedResults.length; k++) {
									resultsPanel.add(new SearchResultPanel(suggestedResults[k]));
								}
								resultsPanel.validate();
								resultsPanel.repaint();
								validate();
								repaint();

							}
						}
					}
				});

				JPanel searchPanel = new JPanel();
				searchPanel.setLayout(new BorderLayout());
				searchPanel.add(searchField, BorderLayout.CENTER);
				searchPanel.add(advancedSearchButton, BorderLayout.EAST);

				setLayout(new BorderLayout());
				add(searchPanel, BorderLayout.NORTH);

			}

			private String[] getClosestMatches(String text) {
				MatchObj[] matches = new MatchObj[recipeNames.length];
				for (int k = 0; k < recipeNames.length; k++) {
					matches[k] = new MatchObj(recipeNames[k], compareNames(text, recipeNames[k]));
				}

				// Sort increasing
				boolean isSwapped = false;
				do {
					isSwapped = false;
					for (int i = 0; i < matches.length - 1; i++) {
						if (matches[i].getMatch() < matches[i + 1].getMatch()) {
							MatchObj temp = matches[i + 1];
							matches[i + 1] = matches[i];
							matches[i] = temp;
							isSwapped = true;
						}
					}

				} while ((isSwapped));

				String[] results;
				if (matches.length < 5) {
					results = new String[matches.length];
				} else {
					results = new String[5];
				}

				for (int k = 0; k < results.length; k++) {
					results[k] = matches[k].getName();
				}

				return results;
			}

			private boolean compareArrays(String[] array1, String[] array2) {
				if (array1 == null || array2 == null) {
					return false;
				}

				for (int k = 0; k < array1.length && k < array2.length; k++) {
					if (!array1[k].equals(array2[k])) {
						return false;
					}
				}

				return true;
			}

			public void focusGained(FocusEvent e) {
				// Initialize the results panel
				if (!panelAdded) {
					add(resultsPanel, BorderLayout.CENTER);
					validate();
					repaint();
					panelAdded = true;
				}

				searchField.setForeground(Color.BLACK);
				searchField.setText("");

			}

			public void focusLost(FocusEvent e) {
				closeResultsPanel();

				searchField.setForeground(foregroundColor);
				searchField.setText(searchHint);

			}

			private void closeResultsPanel() {
				remove(resultsPanel);
				resultsPanel.removeAll();
				resultsPanel.validate();
				validate();

				panelAdded = false;
			}

			private int compareNames(String s1, String s2) {
				boolean isSeq = false;
				int numSeq = 0;
				int numOff = 0;

				int sum = 0;
				for (int k = 0; k < s1.length() &&  k < s2.length() && numOff <= 2; k++) {
					if (("" + s1.charAt(k)).toLowerCase().equals(("" + s2.charAt(k)).toLowerCase())) {
						if (isSeq) {
							sum += 1 + numSeq * numSeq;
							numSeq++;
						} else {
							isSeq = true;
							numSeq = 1;
						}
					} else {
						numOff++;
						numSeq = 0;
						isSeq = false;
					}
				}

				return sum;
			}

			private class SearchResultPanel extends JPanel implements MouseListener {
				private String recipeName;
				private JLabel label;
				private final Font itemFont = new Font("Helvetica", Font.PLAIN, 12);

				public SearchResultPanel(String recipeName) {
					this.recipeName = recipeName;
					createPanel();
					addMouseListener(this);

				}

				private void createPanel() {
					setBackground(Color.WHITE);
					setBorder(new EmptyBorder(5, 15, 0, 15)); // 5, 15, 0, 15
					label = new JLabel(recipeName);
					label.setHorizontalAlignment(JLabel.LEFT);
					label.setFont(itemFont);

					setLayout(new BorderLayout());
					add(label, BorderLayout.WEST);

				}

				public void mouseClicked(MouseEvent e) {
					openEditPanel(getRecipeByName(recipeName));
				}

				public void mouseReleased(MouseEvent e) {
				}

				public void mouseEntered(MouseEvent e) {
					setBackground(highlightColor);
				}

				public void mouseExited(MouseEvent e) {
					setBackground(Color.WHITE);
				}

				public void mousePressed(MouseEvent e) {
				}
			}

			private class MatchObj {
				private int match;
				private String name;

				public MatchObj(String name, int match) {
					this.name = name;
					this.match = match;
				}

				public String getName() {
					return name;
				}

				private int getMatch() {
					return match;
				}
			}
		}

		private class SuggestedGlazePanel extends JPanel {
			private GlazeRecipe[] suggested;

			public SuggestedGlazePanel() {
				suggested = new GlazeRecipe[topRecipes.length];
				int count = 0;
				for (int k = 0; k < topRecipes.length - 1; k++) {
					int pos = getHighestMatchIndex(topRecipes[k], allRecipes);
					if (pos > 0) {
						suggested[count] = allRecipes[pos];
						count++;
					}
				}
				// Ensure that 5 recipes have been added. If not, then pad with
				// random ones
				if (count < 4) {
					GlazeRecipe[] temp = new GlazeRecipe[5];
					int count2 = 0;
					for (int k = 0; k < suggested.length; k++) {
						if (suggested[k] != null) {
							temp[count2] = suggested[k];
							count2++;
						}
					}
					for (int k = count; k < temp.length; k++) {
						temp[k] = allRecipes[(int) (Math.random() * allRecipes.length)];
					}
					suggested = temp;
				}
				createPanel();

			}

			// Returns the number of characteristics/attributes the recipes have
			// in common
			private void createPanel() {
				TitledBorder border = new TitledBorder("Suggested Recipes");
				border.setTitleFont(sectionFont);
				setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 20, 20, 20), border));
				setLayout(new GridLayout(1, 5));

				for (int k = 0; k < suggested.length; k++) {
					if (suggested[k] != null) {
						add(new GlazeViewerPanel(suggested[k]));
					}
				}

			}

			private int getHighestMatchIndex(GlazeRecipe target, GlazeRecipe[] recipes) {
				boolean highestFound = false;
				int maxVal = -1;
				int maxPos = 0;
				for (int k = 0; k < recipes.length; k++) {
					// Check if the recipe is one of the topRecipes or if it is
					// allready in suggested
					if (!contains(recipes[k], suggested) && !contains(recipes[k], topRecipes)) {
						int match = compareGlazes(target, recipes[k]);
						if (match > maxVal) {
							highestFound = true;
							maxVal = match;
							maxPos = k;
						}
					}
				}
				if (!highestFound) {
					return -1;
				} // Nothing found
				return maxPos;
			}

			private int compareGlazes(GlazeRecipe r1, GlazeRecipe r2) {
				int sum = 0;
				// make sure that if they are different versions of each other,
				// then don't include
				if (r1.getName().equals(r2.getName())) {
					return -1;
				}
				if (r1.getName().contains(" v") || r2.getName().contains(" v")) {
					String name1 = r1.getName() + " ";
					String name2 = r2.getName() + " ";
					String name1Sub = name1.substring(0, name1.indexOf(" "));
					String name2Sub = name2.substring(0, name1.indexOf(" "));
					if (name1Sub.contains(name2Sub)) {
						return -1;
					}
				}
				String[] r1Colors = r1.getColorAttribute();
				String[] r2Colors = r2.getColorAttribute();
				for (String s1 : r1Colors) {
					for (String s2 : r2Colors) {
						if (s1.equals(s2)) {
							sum++;
							break;
						}
					}
				}
				String[] r1Firing = r1.getFiringAttribute();
				String[] r2Firing = r2.getFiringAttribute();
				for (String s1 : r1Firing) {
					for (String s2 : r2Firing) {
						if (s1.equals(s2)) {
							sum++;
							break;
						}
					}
				}
				double r1ConeAvg = (r1.getLowerConeInt() + r1.getUpperConeInt()) / 2;
				double r2ConeAvg = (r2.getLowerConeInt() + r2.getUpperConeInt()) / 2;
				if (r1ConeAvg >= (r2ConeAvg - 3) && r1ConeAvg <= (r2ConeAvg + 3)) {
					sum += 3;
				}

				return sum;
			}

			private boolean contains(GlazeRecipe target, GlazeRecipe[] recipes) {
				if (recipes == null) {
					return false;
				}
				for (GlazeRecipe r : recipes) {
					if (r != null && r.getName().equals(target.getName())) {
						return true;
					}
				}
				return false;
			}
		}

		private class RecentGlazePanel extends JPanel {
			public RecentGlazePanel() {
				TitledBorder border = new TitledBorder("Recently Viewed Recipes");
				border.setTitleFont(sectionFont);
				setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(20, 20, 20, 20), border));
				setLayout(new GridLayout(1, 5));

				for (int k = 0; k < topRecipes.length; k++) {
					if (topRecipes[k] != null) {
						add(new GlazeViewerPanel(recentRecipes[k]));
					}
				}

			}
		}

		private class GlazeViewerPanel extends JPanel implements ActionListener, MouseListener {
			private GlazeRecipe theRecipe;
			private boolean hasImage = false;
			private GlazePhoto[] images;
			private int imagePos = 0;
			protected Timer timer;
			private int delay = 1500;

			private final int IMAGE_WIDTH = 150;
			private final int IMAGE_HEIGHT = 180;

			private final Color screenColor = new Color(255, 255, 255, 150);
			private Font titleFont = new Font("Helvetica", Font.BOLD, 14);

			public GlazeViewerPanel(GlazeRecipe theRecipe) {
				this.theRecipe = theRecipe;

				setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
				setMinimumSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
				addMouseListener(this);
				setBorder(new EmptyBorder(10, 10, 10, 10));
				images = theRecipe.getPhotos();
				if (images != null && images.length >= 1 && !images[0].getPath().contains("null_image")) {
					hasImage = true;
				}

				timer = new Timer(delay, this);

			}

			public void actionPerformed(ActionEvent e) {
				imagePos = (imagePos + 1) % images.length;
				repaint();

			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (hasImage) {
					g.drawImage(images[imagePos].getPhoto(), 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
				} else {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
				}

				g.setColor(screenColor);
				g.fillRect(0, IMAGE_HEIGHT / 2 - 15, IMAGE_WIDTH, 30);
				g.setColor(Color.BLACK);

				FontMetrics metrics = g.getFontMetrics(titleFont);
				Rectangle rect = new Rectangle(0, IMAGE_HEIGHT / 2 - 15, IMAGE_WIDTH, 30);
				String text = theRecipe.getName().trim();
				int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
				int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
				g.setFont(titleFont);
				g.drawString(text, x, y);

			}

			public void mouseEntered(MouseEvent e) {
				timer.start();
			}

			public void mouseExited(MouseEvent e) {
				timer.stop();
			}

			public void mouseClicked(MouseEvent e) {
				openEditPanel(theRecipe);
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		}

		private class GlazePreviewPanel extends JPanel implements ActionListener, MouseListener {
			protected Timer timer;
			private int delay = 6000;

			private final int IMAGE_WIDTH = 100;
			private final int IMAGE_HEIGHT = 120;

			private JLabel nameLabel;
			private JLabel coneLabel;
			private JLabel firingLabel;
			private JPanel imagePanel;
			private JLabel leftLabel;
			private JLabel rightLabel;

			private Font titleFont = new Font("Helvetica", Font.PLAIN, 24);
			private Font infoFont = new Font("Helvetica", Font.PLAIN, 15);

			private int imagePos = 0;

			public GlazePreviewPanel() {
				setBackground(Color.WHITE);
				setBorder(new EmptyBorder(20, 20, 20, 20));

				allRecipes = uploadRecipes();
				allRecipes = shuffle(allRecipes);
				allRecipes = shuffle(allRecipes);
				allRecipes = shuffle(allRecipes);
				addMouseListener(this);

				createPanel();

				timer = new Timer(delay, this);
				timer.start();

			}

			private void createPanel() {
				leftLabel = new JLabel("");
				rightLabel = new JLabel("");
				imagePanel = new JPanel();
				imagePanel.setLayout(new BorderLayout());
				imagePanel.add(leftLabel, BorderLayout.EAST);
				imagePanel.add(rightLabel, BorderLayout.WEST);

				nameLabel = new JLabel("");
				nameLabel.setFont(titleFont);
				coneLabel = new JLabel("");
				coneLabel.setFont(infoFont);
				firingLabel = new JLabel("");
				firingLabel.setFont(infoFont);

				JPanel infoPanel = new JPanel();
				JPanel subInfoPanel = new JPanel();
				subInfoPanel.setLayout(new GridLayout(2, 1));
				infoPanel.setBorder(new EmptyBorder(15, 25, 15, 15));
				infoPanel.setLayout(new BorderLayout());
				subInfoPanel.add(coneLabel);
				subInfoPanel.add(firingLabel);
				infoPanel.add(nameLabel, BorderLayout.NORTH);
				infoPanel.add(subInfoPanel, BorderLayout.SOUTH);

				setLayout(new BorderLayout());
				add(infoPanel, BorderLayout.CENTER);
				add(imagePanel, BorderLayout.EAST);

				updatePanel(imagePos);

			}

			public void actionPerformed(ActionEvent e) {
				imagePos = (imagePos + 1) % allRecipes.length;
				updatePanel(imagePos);
			}

			private BufferedImage resize(BufferedImage orig) {
				BufferedImage resized = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
				Graphics g = resized.getGraphics();
				g.drawImage(orig, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
				g.dispose();
				return resized;
			}

			private void updatePanel(int pos) {
				GlazeRecipe theRecipe = allRecipes[pos];
				nameLabel.setText(theRecipe.getName());
				coneLabel.setText("Cone Range: " + theRecipe.getLowerCone() + " to " + theRecipe.getUpperCone());
				firingLabel.setText("Firing Types: " + parseAtmoshpere(theRecipe));
				parseImages(theRecipe);
				repaint();

			}

			private void parseImages(GlazeRecipe theRecipe) {
				GlazePhoto[] images = theRecipe.getPhotos();
				if (images == null) {
					leftLabel.setIcon(new ImageIcon(GlazeRecipe.NULL_IMAGE));
				} else if (images.length == 1) {
					leftLabel.setIcon(new ImageIcon(resize(theRecipe.getPhotos()[0].getPhoto())));
					rightLabel.setIcon(new ImageIcon(resize(GlazeRecipe.NULL_IMAGE)));
				} else {
					leftLabel.setIcon(new ImageIcon(resize(theRecipe.getPhotos()[0].getPhoto())));
					rightLabel.setIcon(new ImageIcon(resize(theRecipe.getPhotos()[1].getPhoto())));
				}
			}

			private String parseAtmoshpere(GlazeRecipe recipe) {
				String atms = "";
				String[] firings = recipe.getFiringAttribute();

				if (firings.length > 2) {
					for (int k = 0; k < firings.length - 1; k++) {
						atms += firings[k].trim() + ", ";
					}
					atms += "& " + firings[firings.length - 1].trim();
				} else {
					if (firings.length == 1) {
						if (firings[0].trim().contains("Ox.")) {
							atms = "Oxidation";
						}
					} else {
						if (firings[0].contains("Ox.")) {
							atms = "Oxidation";
						} else if (firings[0].contains("Red.")) {
							atms = "Reduction";
						} else {
							atms = firings[0].trim();
						}

						if (firings[1].contains("Ox.")) {
							atms += " & Oxidation";
						} else if (firings[1].contains("Red.")) {
							atms += " & Reduction";
						} else {
							atms += " & " + firings[1].trim();
						}
					}

				}
				return atms;
			}

			private GlazeRecipe[] shuffle(GlazeRecipe[] original) {
				for (int k = 0; k < original.length; k++) {
					int randPos = (int) (Math.random() * original.length);
					GlazeRecipe temp = original[k];
					original[k] = original[randPos];
					original[randPos] = temp;
				}

				return original;
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				timer.stop();
				openEditPanel(allRecipes[imagePos]);
				timer.start();
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		}

		private class PdfGenerationPanel extends JPanel implements ActionListener {
			private PDF_Generator_v2 pdfGenerator;

			private ButtonGroup singleGroup;
			private JRadioButton alphOption;
			private JRadioButton coneOption;
			private JRadioButton firingOption;
			private JRadioButton coneThenAlphOption;
			private JRadioButton firingThenAlphOption;

			private JButton saveButton;

			private JLabel imageLabel;
			private BufferedImage alpImage;
			private BufferedImage typeImage;
			private BufferedImage rangeImage;
			private BufferedImage alphAndTypeImage;
			private BufferedImage alpAndRangeImage;

			public PdfGenerationPanel() {
				pdfGenerator = new PDF_Generator_v2();

				try {
					alpImage = uploadImage(new File("SampleAlph.png"));
				} catch (Exception e) {
					alpImage = null;
				}
				try {
					typeImage = uploadImage(new File("SampleFiringType.png"));
				} catch (Exception e) {
					typeImage = null;
				}
				try {
					rangeImage = uploadImage(new File("SampleFiringRange.png"));
				} catch (Exception e) {
					rangeImage = null;
				}
				try {
					alphAndTypeImage = uploadImage(new File("SampleAlphAndFiringType.png"));
				} catch (Exception e) {
					alphAndTypeImage = null;
				}
				try {
					alpAndRangeImage = uploadImage(new File("SampleAlphAndFiringRange.png"));
				} catch (Exception e) {
					alpAndRangeImage = null;
				}

				alphOption = new JRadioButton("Alphabetical");
				alphOption.addActionListener(this);
				alphOption.setActionCommand("A");
				coneOption = new JRadioButton("Firing Range");
				coneOption.addActionListener(this);
				coneOption.setActionCommand("Fr");
				firingOption = new JRadioButton("Firing Type");
				firingOption.addActionListener(this);
				firingOption.setActionCommand("Ft");
				coneThenAlphOption = new JRadioButton("Alphabetical & Firing Range");
				coneThenAlphOption.addActionListener(this);
				coneThenAlphOption.setActionCommand("AFr");
				coneThenAlphOption.setSelected(true);
				firingThenAlphOption = new JRadioButton("Alphabetical & Firing Type");
				firingThenAlphOption.addActionListener(this);
				firingThenAlphOption.setActionCommand("AFt");

				singleGroup = new ButtonGroup();
				singleGroup.add(alphOption);
				singleGroup.add(coneOption);
				singleGroup.add(firingOption);
				singleGroup.add(coneThenAlphOption);
				singleGroup.add(firingThenAlphOption);

				imageLabel = new JLabel();
				imageLabel.setIcon(new ImageIcon(alpAndRangeImage));
				imageLabel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10),
						BorderFactory.createTitledBorder("Sample Table of Contents")));

				saveButton = new JButton("Save Catalog to PDF");
				saveButton.addActionListener(this);

				JPanel buttonPanel = new JPanel();
				buttonPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10),
						BorderFactory.createTitledBorder("Recipe Arrangement Style")));
				buttonPanel.setLayout(new GridLayout(20, 1));
				buttonPanel.add(alphOption);
				buttonPanel.add(coneOption);
				buttonPanel.add(firingOption);
				buttonPanel.add(coneThenAlphOption);
				buttonPanel.add(firingThenAlphOption);

				setBorder(new EmptyBorder(15, 15, 15, 15));
				setLayout(new BorderLayout());
				add(buttonPanel, BorderLayout.CENTER);
				add(imageLabel, BorderLayout.EAST);
				add(saveButton, BorderLayout.SOUTH);

			}

			private BufferedImage uploadImage(File file) throws Exception {
				BufferedImage orig = ImageIO.read(file);
				BufferedImage scaled = new BufferedImage(375, 500, BufferedImage.TYPE_INT_ARGB);
				Graphics g = scaled.createGraphics();
				g.drawImage(orig, 0, 0, 375, 500, null);
				g.dispose();
				return scaled;
			}

			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("A")) {
					imageLabel.setIcon(new ImageIcon(alpImage));
				} else if (e.getActionCommand().equals("Fr")) {
					imageLabel.setIcon(new ImageIcon(rangeImage));
				} else if (e.getActionCommand().equals("Ft")) {
					imageLabel.setIcon(new ImageIcon(typeImage));
				} else if (e.getActionCommand().equals("Afr")) {
					imageLabel.setIcon(new ImageIcon(alpAndRangeImage));
				} else if (e.getActionCommand().equals("AFt")) {
					imageLabel.setIcon(new ImageIcon(alphAndTypeImage));
				} else if (e.getSource() == saveButton) {
					String selectedOption = singleGroup.getSelection().getActionCommand();
					exportCatalog(selectedOption);
				}
			}

			public void exportCatalog(String selectedOption) {
				for (GlazeEditPanel ep : allEditPanels) {
					ep.saveChanges();
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
							boolean isSaved = false;
							if (selectedOption.equals("A")) {
								isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE,
										theFile.getAbsolutePath());
							} else if (selectedOption.equals("Fr")) {
								isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
										theFile.getAbsolutePath());
							} else if (selectedOption.equals("Ft")) {
								isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.FIRING_ATTRIBUTE,
										theFile.getAbsolutePath());
							} else if (selectedOption.equals("AFr")) {
								isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
										PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());
							} else if (selectedOption.equals("AFt")) {
								isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.FIRING_ATTRIBUTE,
										PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());
							}

							if (!isSaved) {
								System.out.println("Could not save the recipe to the specified file...");
							}
						} else {
							exportCatalog(selectedOption);

						}
					} else {
						boolean isSaved = false;
						if (selectedOption.equals("A")) {
							isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE,
									theFile.getAbsolutePath());
						} else if (selectedOption.equals("Fr")) {
							isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
									theFile.getAbsolutePath());
						} else if (selectedOption.equals("Ft")) {
							isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.FIRING_ATTRIBUTE,
									theFile.getAbsolutePath());
						} else if (selectedOption.equals("AFr")) {
							isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.CONE_ATTRIBUTE,
									PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());
						} else if (selectedOption.equals("AFt")) {
							isSaved = pdfGenerator.createCatalog(PDF_Generator_v2.FIRING_ATTRIBUTE,
									PDF_Generator_v2.ALPHABETICAL_ATTRIBUTE, theFile.getAbsolutePath());
						}

						if (!isSaved) {
							System.out.println("Could not save the recipe to the specified file...");
						}
					}
				}

			}
		}
	}
}