import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

// This class stores both a photo and a description of the photo
public class GlazePhoto {
	BufferedImage photo;
	String desc;
	String path;

	public GlazePhoto() {
		try {
			this.photo = GlazeRecipe.NULL_IMAGE;
			this.desc = "No Desc";
			this.path = "null_image.png";
		} catch (Exception e) {
			System.out.println("Error reading null_image.png in GlazePhoto Class ...");
		}
	}

	public GlazePhoto(String path, BufferedImage photo, String desc) {
		this.path = path;
		this.photo = photo;
		this.desc = desc;
	}

	public BufferedImage getPhoto() {
		return photo;
	}

	public String getDesc() {
		return desc;
	}

	public String getPath() {
		return path;
	}

	public void setPhoto(BufferedImage newPhoto) {
		photo = newPhoto;
	}

	public void setDesc(String newDesc) {
		desc = newDesc;
	}
}