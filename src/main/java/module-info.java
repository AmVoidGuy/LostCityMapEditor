module org.lostcitymapeditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
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
    exports org.lostcitymapeditor.Renderer to javafx.graphics;

    opens org.lostcitymapeditor.Renderer to javafx.fxml;
    exports org.lostcitymapeditor;
}