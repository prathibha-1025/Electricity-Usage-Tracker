public class Operator extends User {
    public Operator() {
        super(0, "Operator");
    }

    @Override
    public void showProfile() {
        System.out.println("Operator ID: " + getId());
        System.out.println("Name: " + getName());
    }
}
