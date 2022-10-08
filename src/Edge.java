import java.util.Objects;

public class Edge <T> {

    private final T destination;
    private final String name;
    private int weight;

    public Edge(T destination, String name, int weight) {
        this.destination = Objects.requireNonNull(destination);
        this.name = Objects.requireNonNull(name);

        //    if (Double.isNaN(weight)) {
        //        throw new IllegalArgumentException();
        //    }
        this.weight = weight;
    }

    public T getDestination() {
        return destination;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight (int weight) throws IllegalArgumentException {

        if(weight < 0) {
            throw new IllegalArgumentException("vikten får ej vara negativ, tänk positivt!");
        }

        this.weight = weight;
    }

    public boolean equals(Object other) {
        if (other instanceof Edge edge) {
            return Objects.equals(name, edge.name) &&
                    Objects.equals(destination, edge.destination);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(name, destination);
    }

    @Override
    public String toString() {
        return "till " + destination +
                " med " + name +
                " tar " + weight;
    }
}
