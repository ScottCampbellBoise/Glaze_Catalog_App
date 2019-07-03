import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class NewPhotoDescPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private int charCount;
	private JTextField messageField;
	private JLabel keyCount;
	private JLabel titleLabel;

	public NewPhotoDescPanel() {
		messageField = new JTextField(15);
		keyCount = new JLabel("Char Count: ", SwingConstants.RIGHT);
		titleLabel = new JLabel("Describe the Image - Max. 15 Characters", SwingConstants.CENTER);
		charCount = 0;

		setLayout(new BorderLayout());
		add(titleLabel, BorderLayout.NORTH);
		add(messageField, BorderLayout.CENTER);
		add(keyCount, BorderLayout.SOUTH);

		messageField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				charCount = messageField.getText().length();
				if (charCount > 15) {
					charCount = 15;
					messageField.setText(messageField.getText().substring(0, 15));
				}
				keyCount.setText("Char Count: " + charCount);
				validate();
				repaint();
			}
		});
	}

	public String getNewDesc() {
		return messageField.getText();
	}
}