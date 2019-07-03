import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class GlazeRecipe {
	private String filePath; // this is the folder that holds the txt file and
								// image files

	public static BufferedImage NULL_IMAGE;

	private String name = "Glaze Name";
	private GlazeComponent[] glazeComponents = { new GlazeComponent("Comp 1.", 0.0) };
	private GlazeComponent[] glazeAdds = { new GlazeComponent("Add. 1", 0.0) };
	private String[] colors = { "Other" };
	private String[] firing = { "Ox." };
	private String lowerCone = "6";
	private String upperCone = "6";
	private String[] finishes = { "Unknown" };
	private String reliability = "Unknown";
	private String[] functionality = { "Unknown" };
	private String stability = "Unknown";
	private String[] combination = { "Unknown" };
	private String comments = "No Comments...";
	private GlazePhoto[] photos = { new GlazePhoto() };

	private int numViews = 0; // A helper variable for the main page of the
								// Application

	public GlazeRecipe() {
	}

	public GlazeRecipe(String filePath) {
		this.filePath = filePath;
		File theFile = new File(filePath);
		try {
			NULL_IMAGE = ImageIO.read(new File("null_image.png"));

			String fileContents = new String(
					Files.readAllBytes(Paths.get(filePath + "/" + theFile.getName() + ".txt")));
			String[] lines = fileContents.split("@");
			if (lines.length >= 14) {
				name = lines[0];
				glazeComponents = parseComponents(lines[1].split("~"));
				glazeAdds = parseComponents(lines[2].split("~"));
				colors = lines[3].split("~");
				firing = lines[4].split("~");
				lowerCone = lines[5];
				upperCone = lines[6];
				finishes = lines[7].split("~");
				reliability = lines[8].trim();
				functionality = lines[9].split("~");
				stability = lines[10].trim();
				combination = lines[11].split("~");
				comments = lines[12];
				photos = parsePhotos(lines[13].split("~"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error reading words from the file " + filePath + " ...");
		}

	}

	public GlazePhoto[] parsePhotos(String[] lines) {
		GlazePhoto[] allPhotos = new GlazePhoto[lines.length];
		// each line is split into photo filepath and photo description -
		// description <= 15 chars
		for (int k = 0; k < lines.length; k++) {
			String[] vals = lines[k].split(";");
			try {
				BufferedImage photo = ImageIO.read(new File(vals[0].trim()));
				String desc = vals[1];
				allPhotos[k] = new GlazePhoto(vals[0].trim(), photo, desc);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("GlazeRecipe: Error Reading Image from file: " + filePath + "/" + vals[0].trim());
				// Display JOptionPane....
			}
		}

		return allPhotos;
	}

	public GlazeComponent[] parseComponents(String[] lines) {
		GlazeComponent[] components = new GlazeComponent[lines.length];
		for (int k = 0; k < lines.length; k++) {
			String[] vals = lines[k].split(";");
			if (vals[0].length() < 3) {
				return null;
			}
			components[k] = new GlazeComponent(vals[0], Double.parseDouble(vals[1].trim()));
		}

		return components;
	}

	public int getViews() {
		return numViews;
	}

	public String getName() {
		return name;
	}

	public GlazeComponent[] getComponents() {
		return glazeComponents;
	}

	public GlazeComponent[] getAdds() {
		return glazeAdds;
	}

	public String[] getFiringAttribute() {
		return firing;
	}

	public String[] getColorAttribute() {
		return colors;
	}

	public String getLowerCone() {
		return lowerCone;
	}

	public String getUpperCone() {
		return upperCone;
	}

	public String[] getFinishAttribute() {
		return finishes;
	}

	public String getReliabilityAttribute() {
		return reliability;
	}

	public String[] getFunctionalityAttribute() {
		return functionality;
	}

	public String getStabilityAttribute() {
		return stability;
	}

	public String[] getCombinationAttribute() {
		return combination;
	}

	public String getComments() {
		return comments;
	}

	public GlazePhoto[] getPhotos() {
		return photos;
	}

	public int getNumPhotos() {
		return photos.length;
	}

	public int getLowerConeInt() {
		try {
			if (lowerCone.trim().substring(0, 1).equals("0")) {
				return Integer.parseInt("-" + lowerCone.trim().substring(1, lowerCone.length()));
			}
			return Integer.parseInt(lowerCone);
		} catch (Exception e) {
			return 0;
		}
	}

	public int getUpperConeInt() {
		try {
			if (upperCone.trim().substring(0, 1).equals("0")) {
				return Integer.parseInt("-" + upperCone.trim().substring(1, upperCone.length()));
			}
			return Integer.parseInt(upperCone);
		} catch (Exception e) {
			return 0;
		}
	}

	public void removeComponent(GlazeComponent comp) {
		if (glazeComponents != null) {
			if (glazeComponents.length == 1 && glazeComponents[0].getID() == comp.getID()) {
				glazeComponents = null;
			} else {
				// Make sure that the component to remove exists
				boolean exists = false;
				int size = 0;
				for (int k = 0; k < glazeComponents.length; k++) {
					if (glazeComponents[k] != null && glazeComponents[k].getID() == comp.getID()) {
						exists = true;
					}
					if (glazeComponents[k] != null) {
						size++;
					}
				}

				if (exists) {
					GlazeComponent[] updatedArray = new GlazeComponent[size];
					int count = 0;
					for (int k = 0; k < glazeComponents.length; k++) {
						if (glazeComponents[k] != null && glazeComponents[k].getID() != comp.getID()) {
							updatedArray[count] = glazeComponents[k];
							count++;
						}
					}
					this.glazeComponents = updatedArray;
				}
			}
		}

	}

	public void removeAdd(GlazeComponent comp) {
		if (glazeAdds != null) {
			if (glazeAdds.length == 1 && glazeAdds[0].getID() == comp.getID()) {
				glazeAdds = null;
				System.out.println("Succesfully Removed!");
			} else {
				// Make sure that the componet to remove exists
				boolean exists = false;
				int size = 0;
				for (int k = 0; k < glazeAdds.length; k++) {
					if (glazeAdds[k] != null && glazeAdds[k].getID() == comp.getID()) {
						exists = true;
					}
					if (glazeAdds[k] != null) {
						size++;
					}
				}

				if (exists) {
					GlazeComponent[] updatedArray = new GlazeComponent[size];
					int count = 0;
					for (int k = 0; k < glazeAdds.length; k++) {
						if (glazeAdds[k] != null && glazeAdds[k].getID() != comp.getID()) {
							updatedArray[count] = glazeAdds[k];
							count++;
						}
					}
					this.glazeAdds = updatedArray;
				}
			}
		}

	}

	public void removePhoto(String path) {
		for (int k = 0; k < photos.length; k++) {
			// set the remove picture to null
			if (photos[k].getPath().contains(path)) {
				photos[k] = null;
			}
		}
		// resize the array to remove the null
		if (photos.length == 1) {

			photos = new GlazePhoto[1];
			photos[1] = new GlazePhoto();
		} else if (photos.length > 1) {
			GlazePhoto[] temp = new GlazePhoto[photos.length - 1];
			int count = 0;
			for (int k = 0; k < photos.length; k++) {
				if (photos[k] != null) {
					temp[count] = photos[k];
					count++;
				}
			}
			photos = temp;
		}

	}

	public void setViews(int numViews) {
		this.numViews = numViews;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void addComponent(GlazeComponent newComponent) {
		if (glazeComponents == null) {
			glazeComponents = new GlazeComponent[1];
			glazeComponents[0] = newComponent;
		} else {
			// Check if duplicate first ... if there is a component with the
			// same name and amount, then do not add it
			boolean isDuplicate = false;
			for (int k = 0; k < glazeComponents.length; k++) {
				if (glazeComponents[k] != null
						&& glazeComponents[k].getName().toLowerCase().trim()
								.equals(newComponent.getName().toLowerCase().trim())
						&& glazeComponents[k].getAmount() == newComponent.getAmount()) {
					isDuplicate = true;
					break;
				}
			}
			// check if there is a component with the same name already. if so,
			// replace it with the new component
			boolean isUpdate = false;
			for (int k = 0; k < glazeComponents.length; k++) {
				if (glazeComponents[k] != null
						&& glazeComponents[k].getName().trim().toLowerCase()
								.equals(newComponent.getName().trim().toLowerCase())
						&& glazeComponents[k].getAmount() != newComponent.getAmount()) {
					isUpdate = true;
					glazeComponents[k] = newComponent;
					break;
				}
			}
			// If it isn't a duplicate, and if it wasn't updated, add to the
			// array
			if (!isDuplicate && !isUpdate) {
				GlazeComponent[] updatedArray = new GlazeComponent[glazeComponents.length + 1];
				for (int k = 0; k < glazeComponents.length; k++) {
					updatedArray[k] = glazeComponents[k];
				}
				updatedArray[glazeComponents.length] = newComponent;
				glazeComponents = updatedArray;
			}
		}

	}

	public void addAdd(GlazeComponent newComponent) {
		if (glazeAdds == null) {
			glazeAdds = new GlazeComponent[1];
			glazeAdds[0] = newComponent;
		} else {
			// Check if duplicate first ... if there is a component with the
			// same name and amount, then do not add it
			boolean isDuplicate = false;
			for (int k = 0; k < glazeAdds.length; k++) {
				if (glazeAdds[k] != null
						&& glazeAdds[k].getName().toLowerCase().trim()
								.equals(newComponent.getName().toLowerCase().trim())
						&& glazeAdds[k].getAmount() == newComponent.getAmount()) {
					isDuplicate = true;
					break;
				}
			}
			// check if there is a component with the same name already. if so,
			// replace it with the new component
			boolean isUpdate = false;
			for (int k = 0; k < glazeAdds.length; k++) {
				if (glazeAdds[k] != null
						&& glazeAdds[k].getName().trim().toLowerCase()
								.equals(newComponent.getName().trim().toLowerCase())
						&& glazeAdds[k].getAmount() != newComponent.getAmount()) {
					isUpdate = true;
					glazeAdds[k] = newComponent;
					break;
				}
			}
			// If it isn't a duplicate, and if it wasn't updated, add to the
			// array
			if (!isDuplicate && !isUpdate) {
				GlazeComponent[] updatedArray = new GlazeComponent[glazeAdds.length + 1];
				for (int k = 0; k < glazeAdds.length; k++) {
					updatedArray[k] = glazeAdds[k];
				}
				updatedArray[glazeAdds.length] = newComponent;
				glazeAdds = updatedArray;
			}
		}

	}

	public void setComponents(GlazeComponent[] newComps) {
		this.glazeComponents = newComps;
	}

	public void setAdds(GlazeComponent[] newAdds) {
		this.glazeAdds = newAdds;
	}

	public void setColor(String[] newColor) {
		colors = newColor;
	}

	public void setFiring(String[] newFiring) {
		firing = newFiring;
	}

	public void setConeRange(String newLowerCone, String newUpperCone) {
		lowerCone = newLowerCone;
		upperCone = newUpperCone;
	}

	public void setFinish(String[] newFinish) {
		finishes = newFinish;
	}

	public void setReliability(String newReliability) {
		reliability = newReliability;
	}

	public void setFunctionality(String[] newFunctionality) {
		functionality = newFunctionality;
	}

	public void setStability(String newStability) {
		stability = newStability;
	}

	public void setCombination(String[] newCombo) {
		combination = newCombo;
	}

	public void setComment(String newComment) {
		comments = newComment;
	}

	public void addPhoto(GlazePhoto newPhoto) {
		GlazePhoto[] temp = new GlazePhoto[photos.length + 1];
		for (int k = 0; k < photos.length; k++) {
			temp[k] = photos[k];
		}
		temp[temp.length - 1] = newPhoto;
		photos = temp;

	}

	public void addNewPhoto(BufferedImage img, String desc) {
		GlazePhoto newPhoto = new GlazePhoto(filePath + "/glaze " + (photos.length + 1) + ".png", img, desc);

		GlazePhoto[] temp = new GlazePhoto[photos.length + 1];
		for (int k = 0; k < photos.length; k++) {
			temp[k] = photos[k];
		}
		temp[temp.length - 1] = newPhoto;
		photos = temp;

		savePhotos(filePath);

	}

	public void setPhotoDesc(String path, String newDesc) {
		// Will set the photo description corresponding to path
		for (GlazePhoto gp : photos) {
			if (gp.getPath().contains(path)) {
				gp.setDesc(newDesc);
			}
		}
	}

	public boolean updateFile() {
		/**
		 * Check if the glaze recipe name is different than the file name. If
		 * so, save the entire file to a new directory with updated name, and
		 * delete the old
		 */
		File origFile;
		if (filePath == null) {
			try {
				String newName = getNewVersionString();
				name = newName;
				filePath = "Glaze Recipes/" + newName.trim();
				origFile = new File(filePath);
				origFile.mkdir();
				PrintWriter writer = new PrintWriter(filePath.trim() + "/" + origFile.getName().trim() + ".txt",
						"UTF-8");
				writer.println("");
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		origFile = new File(filePath);

		if (origFile.getName().trim().equals(name.trim())) { // same name - just
																// update the
																// text file
			updateRecipeFile(filePath.trim() + "/" + origFile.getName().trim() + ".txt");

			return true;
		} else { // different name: update to different directory, then destroy
					// the original
			GlazeRecipe dupRecipe = duplicateRecipe();
			if (dupRecipe != null) {
				deleteDirectory(origFile.getAbsoluteFile());
				filePath = "Glaze Recipes/" + name;

				return true;
			} else {
				return false;
			}
		}
	}

	private void updateRecipeFile(String otherPath) {
		BufferedWriter writer = null;
		try {
			File glazeFile = new File(otherPath);
			writer = new BufferedWriter(new FileWriter(glazeFile));

			writer.write(name.trim() + "@\n");

			String compString = "";
			if (glazeComponents != null) {
				for (GlazeComponent gc : glazeComponents) {
					if (gc != null) {
						compString += (gc.getName().trim() + " ; " + gc.getAmount() + " ~ ");
					}
				}
				compString = compString.trim().substring(0, compString.length() - 3).trim() + "@\n";
				writer.write(compString);
			} else {
				writer.write("@\n");
			}

			String addString = "";
			if (glazeAdds != null) {
				for (GlazeComponent gc : glazeAdds) {
					if (gc != null) {
						addString += (gc.getName().trim() + " ; " + gc.getAmount() + " ~ ");
					}
				}
				addString = addString.substring(0, addString.length() - 3).trim() + "@\n";
				writer.write(addString);
			} else {
				writer.write("@\n");
			}

			String colorString = "";
			for (String gc : colors) {
				colorString += (gc.trim() + " ~ ");
			}
			colorString = colorString.substring(0, colorString.length() - 3).trim() + "@\n";
			writer.write(colorString);

			String firingString = "";
			for (String gc : firing) {
				if (gc != null) {
					firingString += (gc.trim() + " ~ ");
				}
			}
			firingString = firingString.substring(0, firingString.length() - 3).trim() + "@\n";
			writer.write(firingString);

			writer.write(lowerCone + "@\n");
			writer.write(upperCone + "@\n");

			String finishString = "";
			for (String gc : finishes) {
				finishString += (gc.trim() + " ~ ");
			}
			finishString = finishString.substring(0, finishString.length() - 3).trim() + "@\n";
			writer.write(finishString);

			writer.write(reliability.trim() + "@\n");

			String funcString = "";
			for (String gc : functionality) {
				funcString += (gc.trim() + " ~ ");
			}
			funcString = funcString.substring(0, funcString.length() - 3).trim() + "@\n";
			writer.write(funcString);

			writer.write(stability.trim() + "@\n");

			String comboString = "";
			for (String gc : combination) {
				comboString += (gc.trim() + " ~ ");
			}
			comboString = comboString.substring(0, comboString.length() - 3).trim() + "@\n";
			writer.write(comboString);

			// comments
			writer.write(comments + "@\n");

			// photos
			String photoString = "";
			for (int k = 0; k < this.photos.length; k++) {
				GlazePhoto gp = photos[k];
				if (gp.getPath().contains("null_image")) {
					String newPath = "null_image.png";
					photoString += (newPath + " ; " + gp.getDesc().trim() + " ~ ");
				} else {
					String newPath = otherPath.substring(0, otherPath.lastIndexOf("/")) + "/glaze " + (k + 1) + ".png";
					photoString += (newPath + " ; " + gp.getDesc().trim() + " ~ ");
				}
			}
			photoString = photoString.substring(0, photoString.length() - 3).trim() + "@\n";
			writer.write(photoString);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void savePhotos(String rootDir) {
		int count = 1;
		for (GlazePhoto p : photos) {
			if (!p.getPath().contains("null_image")) {
				try {
					File outputfile = new File(rootDir + "/glaze " + count + ".png");
					ImageIO.write(p.getPhoto(), "PNG", outputfile);
					count++;
				} catch (Exception e) {
					System.out.println("Error saving an image to file.");
				}
			}
		}
	}

	private String getNewVersionString() {
		String newFileName = name.trim();
		int lastSpaceIndex = newFileName.lastIndexOf(" ");
		if (lastSpaceIndex > 0 && newFileName.substring(lastSpaceIndex, newFileName.length()).contains("v")) // already
																												// has
																												// a
																												// v
																												// extension
		{
			newFileName = newFileName.substring(0, lastSpaceIndex);
		}

		if (new File("Glaze Recipes/" + newFileName).exists()) {
			int num = 2;
			while (new File("Glaze Recipes/" + newFileName + " v" + num).exists()) {
				num++;
			}
			newFileName += " v" + num;
		}

		return newFileName;
	}

	public GlazeRecipe duplicateRecipe() {
		String newFileName = getNewVersionString();

		File newDir = new File("Glaze Recipes/" + newFileName);
		try {
			newDir.mkdir();
			String originalName = name;
			name = newFileName;
			updateRecipeFile("Glaze Recipes/" + newFileName + "/" + newFileName + ".txt");
			name = originalName;
			savePhotos("Glaze Recipes/" + newFileName);
			return new GlazeRecipe(newDir.getAbsolutePath());
		} catch (Exception e) {
			return null;
		}
	}

	public boolean deleteRecipe() {
		File rootDir = new File(filePath);
		return deleteDirectory(rootDir);
	}

	private boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}
}