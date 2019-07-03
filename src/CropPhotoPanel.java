import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CropPhotoPanel extends JFrame {
	private JFrame masterFrame;

	private final int FRAME_WIDTH = 480;
	private final int FRAME_HEIGHT = 700;
	private final int IMAGE_WIDTH = 240;
	private final int IMAGE_HEIGHT = 300;
	private int imageWidth = IMAGE_WIDTH;
	private int imageHeight = IMAGE_HEIGHT;

	private BufferedImage rawImage;
	private String rawPath;
	private int RAW_IMAGE_WIDTH;
	private int RAW_IMAGE_HEIGHT;

	private ResizeablePhoto resizeablePhoto;
	private NewPhotoDescPanel descPanel;
	private JLabel photoLabel;
	private JSlider scaleSlider;

	private boolean isSaved = false;

	public static void main(String[] args) {
		try {
			new CropPhotoPanel("/Users/ScottCampbell/Desktop/img.png");
		} catch (Exception e) {
			System.out.println("Error Reading a the file ... ");
		}
	}

	public CropPhotoPanel(String rawPath) {
		try {
			this.rawPath = rawPath;
			this.rawImage = ImageIO.read(new File(rawPath));
			RAW_IMAGE_WIDTH = rawImage.getWidth();
			RAW_IMAGE_HEIGHT = rawImage.getHeight();

			setTitle("Upload a Photo");
			setSize(FRAME_WIDTH, FRAME_HEIGHT);
			setResizable(false);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			masterFrame = this;

			createPanel();

			setVisible(true);
		} catch (Exception e) {
			System.out.println("Error reading in file!!!");
		}
	}

	private void createPanel() {
		resizeablePhoto = new ResizeablePhoto();

		scaleSlider = new JSlider(JSlider.HORIZONTAL, 100, 300, 100);
		scaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				imageWidth = IMAGE_WIDTH * scaleSlider.getValue() / 100;
				imageHeight = IMAGE_HEIGHT * scaleSlider.getValue() / 100;
				resizeablePhoto.repaintPanel();
			}
		});
		scaleSlider.setMajorTickSpacing(25);
		scaleSlider.setMinorTickSpacing(5);
		scaleSlider.setPaintTicks(true);
		scaleSlider.setPaintLabels(true);

		descPanel = new NewPhotoDescPanel();
		descPanel.setBorder(new EmptyBorder(25, 65, 15, 65));

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(masterFrame, WindowEvent.WINDOW_CLOSING));
				isSaved = true;
			}
		});

		JPanel configPanel = new JPanel();
		configPanel.setLayout(new BorderLayout());
		configPanel.add(scaleSlider, BorderLayout.NORTH);
		configPanel.add(descPanel, BorderLayout.CENTER);
		configPanel.add(saveButton, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(resizeablePhoto, BorderLayout.CENTER);
		add(configPanel, BorderLayout.SOUTH);
	}

	public boolean getIsSaved() {
		return isSaved;
	}

	public BufferedImage getImage() {
		return resizeablePhoto.getCroppedPhoto();
	}

	public String getDescription() {
		return descPanel.getNewDesc();
	}

	private class ResizeablePhoto extends JPanel implements ActionListener {
		private final int Y_OFFSET = 130;

		private int mouse_x;
		private int mouse_y;
		private int imgX = 0;
		private int imgY = 0;

		private final int screenColor = new Color(30, 30, 30, 200).getRGB();
		private final int transColor = new Color(0, 0, 0, 0).getRGB();
		private BufferedImage screenImg;

		public ResizeablePhoto() {
			addMouseListener(new MyMouseListener());
			addMouseMotionListener(new MyMouseMotionListener());

			screenImg = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
			for (int row = 0; row < screenImg.getWidth(); row++) {
				for (int col = 0; col < screenImg.getHeight(); col++) {
					if (col > ((screenImg.getWidth() / 2) - (imageWidth / 2))
							&& col < ((screenImg.getWidth() / 2) + (imageWidth / 2))
							&& row > ((screenImg.getHeight() / 2) - (imageHeight / 2))
							&& row < ((screenImg.getHeight() / 2) + (imageHeight / 2))) {
						screenImg.setRGB(col, row, transColor);
					} else {
						screenImg.setRGB(col, row, screenColor);
					}
				}
			}

			Graphics g = screenImg.getGraphics();
			g.setColor(Color.BLACK);
			g.drawRect((screenImg.getWidth() / 2) - (imageWidth / 2), (screenImg.getHeight() / 2) - (imageHeight / 2),
					imageWidth, imageHeight);
			g.dispose();

			imgX = -(imageWidth - FRAME_WIDTH) / 2; // Center of the screen
			imgY = -(imageWidth - FRAME_HEIGHT) / 2 - Y_OFFSET; // Center of the
																// screen

			// timer = new Timer(delay, this);
			// timer.start();
		}

		public void repaintPanel() {
			repaint();
		}

		public void actionPerformed(ActionEvent e) {
			repaint();
		}

		/**
		 * UPDATE TO BE HIGHER QUALITY...
		 */
		public BufferedImage getCroppedPhoto() {
			Dimension size = getSize();
			BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = img.createGraphics();
			paint(g2);
			BufferedImage suborig = img.getSubimage((this.getWidth() - IMAGE_WIDTH) / 2,
					(this.getHeight() - IMAGE_HEIGHT) / 2, IMAGE_WIDTH, IMAGE_HEIGHT);
			return suborig;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Cover the panel in a half transparent screen except for a section
			// in the middle for the photo
			g.drawImage(rawImage, imgX, imgY, imageWidth, imageHeight, null);
			g.drawImage(screenImg, -(screenImg.getWidth() - this.getWidth()) / 2,
					-(screenImg.getHeight() - this.getHeight()) / 2, null);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Times", Font.BOLD, 20));
			g.drawString("Drag the Image to Fit", 150, 50);
		}

		private class MyMouseListener implements MouseListener {
			public void mouseClicked(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				mouse_x = e.getX();
				mouse_y = e.getY();
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		}

		private class MyMouseMotionListener implements MouseMotionListener {
			public void mouseDragged(MouseEvent e) {
				int mouse_dx = (int) ((e.getX() - mouse_x) * 0.02);
				int mouse_dy = (int) ((e.getY() - mouse_y) * 0.02);

				if (imgX + mouse_dx + imageWidth < (FRAME_WIDTH - IMAGE_WIDTH / 2)
						|| imgX + mouse_dx > (IMAGE_WIDTH / 2)) {
					mouse_dx = 0;
				}
				if (imgY + mouse_dy + imageHeight < (FRAME_HEIGHT - Y_OFFSET - 20 - IMAGE_HEIGHT / 2)
						|| imgY + mouse_dy > IMAGE_HEIGHT / 2 - 50) {
					mouse_dy = 0;
				}

				imgX += mouse_dx;
				imgY += mouse_dy;

				repaint();
			}

			public void mouseMoved(MouseEvent e) {
			}
		}
	}
}
