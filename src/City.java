import java.util.Objects;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class City extends Circle {

    private double x;
    private double y;
    private final String name;

    public City(String name, double x, double y) {
        super(x, y, 10);
        this.x = x;
        this.y = y;
        setFill(Color.BLUE);
        this.name = name;
        setId(name);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (other instanceof City city) {
            return name.equals(city.name);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return name;
    }
}
