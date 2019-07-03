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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class GlazeSearchPanel extends JPanel {
	private GlazeGUI masterGUI;

	final private int NUM_ATTRIBUTES = 8;
	private GlazeAttribute[] attributes = new GlazeAttribute[NUM_ATTRIBUTES];
	private GlazeRecipe[] allRecipes;

	private JPanel attributesPanel;
	private JPanel attributeSearchPanel;
	private AttributeSelectionPanel searchPanel;
	private JPanel resultsPanel;
	private JLabel infoLabel;

	private final Font sectionFont = new Font("Helvetica", Font.BOLD, 14);

	public GlazeSearchPanel(GlazeGUI theGUI) {
		this.masterGUI = theGUI;
		allRecipes = uploadRecipes();

		setBorder(new EmptyBorder(20, 20, 20, 20));

		resultsPanel = new JPanel();
		resultsPanel.setLayout(new GridLayout(10, 1));
		TitledBorder resultsBorder = new TitledBorder("Search Results");
		resultsBorder.setTitleFont(sectionFont);
		resultsBorder.setTitleJustification(TitledBorder.CENTER);
		resultsBorder.setTitlePosition(TitledBorder.TOP);
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), resultsBorder));

		attributesPanel = new JPanel();
		infoLabel = new JLabel("Click 'x' to remove from search");
		infoLabel.setBorder(new EmptyBorder(15, 10, 15, 10));

		searchPanel = new AttributeSelectionPanel(this);
		TitledBorder searchBorder = new TitledBorder("Search Attributes");
		searchBorder.setTitleFont(sectionFont);
		searchBorder.setTitleJustification(TitledBorder.CENTER);
		searchBorder.setTitlePosition(TitledBorder.TOP);
		searchPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), searchBorder));

		attributeSearchPanel = new JPanel();
		attributeSearchPanel.setPreferredSize(new Dimension(this.getWidth(), 280));
		attributeSearchPanel.setMinimumSize(new Dimension(800, 280));
		attributeSearchPanel.setLayout(new BorderLayout());
		attributeSearchPanel.add(attributesPanel, BorderLayout.EAST);
		attributeSearchPanel.add(searchPanel, BorderLayout.CENTER);

		JScrollPane resultsScroll = new JScrollPane(resultsPanel);

		setLayout(new BorderLayout());
		add(attributeSearchPanel, BorderLayout.NORTH);
		add(resultsScroll, BorderLayout.CENTER);

		updateAttributes();
	}

	private void updateResults() {
		// get attributes from searchPanel
		ArrayList<GlazeRecipe> resultSet = new ArrayList<GlazeRecipe>();
		resultSet = new ArrayList<GlazeRecipe>();
		for (int k = 0; k < allRecipes.length; k++) {
			resultSet.add(allRecipes[k]);
		}

		for (GlazeAttribute attr : attributes) {
			if (attr != null) {
				String[] info = attr.getName().split(":");
				if (info[0].contains("Color")) {
					for (int k = 0; k < resultSet.size(); k++) {
						if (!contains(info[1], resultSet.get(k).getColorAttribute())) {
							resultSet.remove(k);
						}
					}
				} else if (info[0].contains("Cone")) {

					// This is harder

				} else if (info[0].contains("Firing")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!contains(info[1], resultSet.get(k).getFiringAttribute())) {
								resultSet.remove(k);
							}
						}
					}
				} else if (info[0].contains("Finish")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!contains(info[1], resultSet.get(k).getFinishAttribute())) {
								resultSet.remove(k);
							}
						}
					}
				} else if (info[0].contains("Reliability")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!resultSet.get(k).getReliabilityAttribute().trim().equals(info[1].trim())) {
							}
						}
					}
				} else if (info[0].contains("Functionality")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!contains(info[1], resultSet.get(k).getFunctionalityAttribute())) {
								resultSet.remove(k);
							}
						}
					}
				} else if (info[0].contains("Stability")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!resultSet.get(k).getStabilityAttribute().trim().equals(info[1].trim())) {
							}
						}
					}
				} else if (info[0].contains("As Combo")) {
					if (!info[1].equals("Unknown")) {
						for (int k = 0; k < resultSet.size(); k++) {
							if (!contains(info[1], resultSet.get(k).getCombinationAttribute())) {
								resultSet.remove(k);
							}
						}
					}
				}
			}
		}

		resultsPanel.removeAll();
		resultsPanel.validate();
		if (resultSet.size() < 1) {
			resultsPanel.add(new JLabel("No Recipes could be found with the given requirements..."));
		} else {
			resultsPanel.setLayout(new GridLayout((resultSet.size() / 5 + 1), 1));
			int count = 0;
			for (int k = 0; k < resultSet.size() - 5; k += 5) {
				JPanel subPanel = new JPanel();
				subPanel.setLayout(new FlowLayout());

				for (int i = 0; i < 5; i++) {
					subPanel.add(new GlazeViewerPanel(resultSet.get(k + i)));
					count++;
				}
				resultsPanel.add(subPanel);
			}
			JPanel finalPanel = new JPanel();
			finalPanel.setLayout(new FlowLayout());
			for (int k = count; k < resultSet.size(); k++) {
				finalPanel.add(new GlazeViewerPanel(resultSet.get(k)));
			}
			resultsPanel.add(finalPanel);
			resultsPanel.validate();
			resultsPanel.repaint();
			validate();
			repaint();
		}
	}

	private boolean contains(String target, String[] ary) {
		for (String s : ary) {
			if (s.trim().equals(target.trim())) {
				return true;
			}
		}
		return false;
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

	public void addAttribute(String attributeName) {
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
		updateResults();
	}

	public void updateAttributes() {
		attributeSearchPanel.remove(attributesPanel);

		attributesPanel = new JPanel();
		TitledBorder border = new TitledBorder("Selected Glaze Attributes");
		border.setTitleFont(sectionFont);
		border.setTitleJustification(TitledBorder.LEFT);
		border.setTitlePosition(TitledBorder.TOP);
		attributesPanel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 10, 10), border));
		attributesPanel.add(infoLabel);
		attributesPanel.setLayout(new GridLayout(9, 1));

		for (int k = 0; k < NUM_ATTRIBUTES; k++) {
			if (attributes[k] != null) {
				attributesPanel.add(attributes[k]);
			}
		}

		attributeSearchPanel.add(attributesPanel, BorderLayout.EAST);
		attributeSearchPanel.validate();
		attributeSearchPanel.repaint();
		updateResults();
	}

	private void openEditPanel(GlazeRecipe theRecipe) {
		JFrame editFrame = new JFrame("Recipe Editor: " + theRecipe.getName());
		editFrame.setSize(540, 360);
		editFrame.setResizable(true);
		editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		GlazeEditPanel ep = new GlazeEditPanel(masterGUI, editFrame, theRecipe);
		editFrame.add(ep);

		editFrame.pack();
		editFrame.setVisible(true);
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

	public class GlazeAttribute extends JPanel {
		private JLabel label;
		private JButton button;
		private String attributeName;

		public GlazeAttribute(String attributeName) {
			setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 10, 0, 10),
					BorderFactory.createEtchedBorder()));
			this.attributeName = attributeName;
			label = new JLabel(attributeName + "   ");
			label.setOpaque(true);
			label.setBackground(Color.WHITE);
			label.setBorder(new EmptyBorder(0, 5, 0, 5));
			button = new JButton("x");
			button.setPreferredSize(new Dimension(20, 20));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// remove attribute from the array
					// update the Panel
					for (int k = 0; k < NUM_ATTRIBUTES; k++) {
						if (attributes[k].getName().equals(attributeName)) {
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
}