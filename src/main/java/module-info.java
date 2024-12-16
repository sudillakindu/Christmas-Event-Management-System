module org.event.chems {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires javafx.swing;
    requires jdk.javadoc;
    requires org.jfree.jfreechart;
    requires org.jfree.chart.fx;
    requires mail;

    opens org.event.chems to javafx.fxml;
    exports org.event.chems;
}