// This class stores a component name and its amount
//TO DO: check if the component has a warning and display it ...
public class GlazeComponent {
	private String name;
	private double amount;
	private long ID;

	public GlazeComponent() {
		this.name = "Name";
		this.amount = 0;
		this.ID = System.currentTimeMillis() + (long) (Math.random() * 100000000);
	}

	public GlazeComponent(String name, double amount) {
		this.name = name;
		this.amount = amount;
		this.ID = System.currentTimeMillis() + (long) (Math.random() * 100000000);
	}

	public String getName() {
		return name;
	}

	public double getAmount() {
		return amount;
	}

	public long getID() {
		return ID;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void setAmount(double newAmt) {
		this.amount = newAmt;
	}

	public void setID(long newID) {
		this.ID = newID;
	}
}