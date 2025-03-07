module org.lostcitymapeditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.swing;
    requires org.joml;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.lwjgl.nanovg;
    requires imgui.binding;
    requires imgui.lwjgl3;
    requires org.reflections;

    opens org.lostcitymapeditor to javafx.fxml;
    exports org.lostcitymapeditor.Renderer to javafx.graphics; // Export Renderer package

    opens org.lostcitymapeditor.Renderer to javafx.fxml; // Optional: If using FXML
    exports org.lostcitymapeditor;
}