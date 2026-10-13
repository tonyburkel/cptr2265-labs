import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderSystemRefactored {
    public static void main(String[] args) {
        OrderRepository repo = new TextFileOrderRepository("orders.txt");
        EmailNotifier notifier = new EmailNotifier();
        OrderProcessor processor = new OrderProcessor(repo, notifier);

        Order order1 = new Order(1001, "Ana Lopez", "ana@student.mctc.edu", new StudentDiscount(), new CreditCardPayment());
        order1.addItem(new Item("TXT-2265", "Software Engineering 10e", 89.99, 1, true));
        order1.addItem(new Item("SUP-0042", "Engineering notebook", 6.50, 2, true));
        processor.process(order1);

        System.out.println();
        Order order2 = new Order(1002, "Dr. Kim", "kim@mctc.edu", new StaffDiscount(), new InvoicePayment());
        order2.addItem(new Item("SUP-0007", "Whiteboard markers", 12.00, 1, true));
        processor.process(order2);

        System.out.println();
        Order order3 = new Order(1003, "Sam Ortiz", "sam@student.mctc.edu", new StudentDiscount(), new CampusCashPayment());
        order3.addItem(new Item("EBK-2265", "Software Engineering e-book", 54.00, 1, false));
        processor.process(order3);
    }
}

// --- Domain Models ---
class Item {
    String sku;
    String name;
    double price;
    int quantity;
    boolean isPhysical;

    Item(String sku, String name, double price, int quantity, boolean isPhysical) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.isPhysical = isPhysical;
    }
}

interface DiscountStrategy {
    double calculateDiscount(double subtotal);
    String getTypeName();
}

class StudentDiscount implements DiscountStrategy {
    public double calculateDiscount(double subtotal) { return subtotal * 0.10; }
    public String getTypeName() { return "student"; }
}

class StaffDiscount implements DiscountStrategy {
    public double calculateDiscount(double subtotal) { return subtotal * 0.15; }
    public String getTypeName() { return "staff"; }
}

class PublicDiscount implements DiscountStrategy {
    public double calculateDiscount(double subtotal) { return 0.0; }
    public String getTypeName() { return "public"; }
}

interface PaymentStrategy {
    void processPayment(double total);
    String getPaymentName();
}

class CreditCardPayment implements PaymentStrategy {
    public void processPayment(double total) {
        System.out.printf("Charging $%.2f to credit card on file...%n", total);
        double fee = Math.round(total * 0.029 * 100.0) / 100.0;
        System.out.printf("  Card processing fee recorded: $%.2f%n", fee);
    }
    public String getPaymentName() { return "card"; }
}

class CampusCashPayment implements PaymentStrategy {
    public void processPayment(double total) {
        System.out.printf("Deducting $%.2f from campus cash account...%n", total);
    }
    public String getPaymentName() { return "campus_cash"; }
}

class InvoicePayment implements PaymentStrategy {
    public void processPayment(double total) {
        System.out.printf("Generating invoice for $%.2f, due in 30 days...%n", total);
    }
    public String getPaymentName() { return "invoice"; }
}

class Order {
    int orderId;
    String customerName;
    String customerEmail;
    DiscountStrategy discountStrategy;
    PaymentStrategy paymentStrategy;
    List<Item> items = new ArrayList<>();

    Order(int orderId, String customerName, String customerEmail,
          DiscountStrategy discountStrategy, PaymentStrategy paymentStrategy) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.discountStrategy = discountStrategy;
        this.paymentStrategy = paymentStrategy;
    }

    void addItem(Item item) { items.add(item); }

    double subtotal() {
        double sum = 0;
        for (Item i : items) { sum += i.price * i.quantity; }
        return sum;
    }

    boolean hasPhysicalItems() {
        for (Item i : items) { if (i.isPhysical) return true; }
        return false;
    }

    double shippingCost() {
        if (!hasPhysicalItems()) return 0.0;
        return subtotal() >= 50 ? 0.0 : 5.99;
    }
}

// --- Interfaces & Services (ISP & DIP) ---
interface EmailNotifierService {
    void sendEmail(String to, String subject, String body);
}

class EmailNotifier implements EmailNotifierService {
    public void sendEmail(String to, String subject, String body) {
        System.out.println("[EMAIL to " + to + "] " + subject);
        System.out.println(body);
    }
}

interface OrderRepository {
    void save(Order order, double total);
}

class TextFileOrderRepository implements OrderRepository {
    private final String filename;
    TextFileOrderRepository(String filename) { this.filename = filename; }

    public void save(Order order, double total) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(String.format("%d|%s|%s|%.2f%n",
                    order.orderId, order.customerEmail, order.paymentStrategy.getPaymentName(), total));
        } catch (IOException e) {
            System.out.println("Could not save order: " + e.getMessage());
        }
    }
}

class ReceiptFormatter {
    static String format(Order order, double subtotal, double discount, double tax, double shipping, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(40)).append("\n");
        sb.append("MCTC CAMPUS BOOKSTORE\n");
        sb.append("Order #").append(order.orderId).append("   ").append(LocalDate.now()).append("\n");
        sb.append("Customer: ").append(order.customerName)
          .append(" (").append(order.discountStrategy.getTypeName()).append(")\n");
        sb.append("-".repeat(40)).append("\n");
        for (Item item : order.items) {
            double lineTotal = item.price * item.quantity;
            sb.append(String.format("%d x %-26s $%8.2f%n", item.quantity, item.name, lineTotal));
        }
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("%-30s $%8.2f%n", "Subtotal", subtotal));
        sb.append(String.format("%-29s -$%8.2f%n", "Discount", discount));
        sb.append(String.format("%-30s $%8.2f%n", "Tax", tax));
        sb.append(String.format("%-30s $%8.2f%n", "Shipping", shipping));
        sb.append(String.format("%-30s $%8.2f%n", "TOTAL", total));
        sb.append("=".repeat(40));
        return sb.toString();
    }
}

class OrderProcessor {
    private final OrderRepository repository;
    private final EmailNotifierService notifier;

    OrderProcessor(OrderRepository repository, EmailNotifierService notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    boolean process(Order order) {
        if (order.items.isEmpty()) {
            System.out.println("Order " + order.orderId + " rejected: no items");
            return false;
        }
        if (!order.customerEmail.contains("@")) {
            System.out.println("Order " + order.orderId + " rejected: bad email");
            return false;
        }

        double subtotal = order.subtotal();
        double discount = order.discountStrategy.calculateDiscount(subtotal);
        double shipping = order.shippingCost();
        double tax = Math.round((subtotal - discount) * 0.07875 * 100.0) / 100.0;
        double total = Math.round((subtotal - discount + tax + shipping) * 100.0) / 100.0;

        order.paymentStrategy.processPayment(total);

        String receipt = ReceiptFormatter.format(order, subtotal, discount, tax, shipping, total);

        repository.save(order, total);
        notifier.sendEmail(order.customerEmail, "Your bookstore order #" + order.orderId, receipt);
        return true;
    }
}
