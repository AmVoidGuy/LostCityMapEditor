module org.lostcitymapeditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens org.lostcitymapeditor to javafx.fxml;
    exports org.lostcitymapeditor;
}