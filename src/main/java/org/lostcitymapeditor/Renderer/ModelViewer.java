package org.lostcitymapeditor.Renderer;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.lostcitymapeditor.OriginalCode.Model;

import java.awt.event.MouseEvent;

public class ModelViewer extends JFXPanel {
    private Group root;
    private PerspectiveCamera camera;
    private Group modelGroup;
    private double modelRotation = 0;
    private AnimationTimer rotationTimer;

    public ModelViewer(int width, int height) {
        this.setSize(width, height);

        Platform.runLater(() -> {
            root = new Group();
            createScene(width, height);
        });
    }

    private void createScene(int width, int height) {
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(25);

        modelGroup = new Group();
        root.getChildren().add(modelGroup);

        SubScene subScene = new SubScene(root, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.DARKGRAY);
        subScene.setCamera(camera);
        root.getTransforms().add(new Translate(0, 50, 0));
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(subScene);

        Scene mainScene = new Scene(stackPane, width, height);
        this.setScene(mainScene);

        startRotation();
    }

    public void loadModel(Model model) {
        Platform.runLater(() -> {
            modelGroup.getChildren().clear();

            if (model == null) {
                return;
            }

            TriangleMesh mesh = new TriangleMesh();

            for (int i = 0; i < model.vertexCount; i++) {
                mesh.getPoints().addAll(model.verticesX[i], model.verticesY[i], model.verticesZ[i]);
            }

            for (int i = 0; i < model.vertexCount; i++) {
                mesh.getTexCoords().addAll(0, 0);
            }

            for (int i = 0; i < model.faceCount; i++) {
                mesh.getFaces().addAll(
                        model.faceIndicesA[i], 0,
                        model.faceIndicesB[i], 0,
                        model.faceIndicesC[i], 0
                );
            }

            MeshView meshView = new MeshView(mesh);

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(Color.LIGHTGRAY);
            meshView.setMaterial(material);

            double scale = calculateAppropriateScale(model);
            meshView.setScaleX(scale);
            meshView.setScaleY(scale);
            meshView.setScaleZ(scale);

            centerModel(meshView, model);

            modelGroup.getChildren().add(meshView);
        });
    }

    // In your ModelViewer class
    @Override
    public void processMouseEvent(MouseEvent e) {
        // Only process events that are directly over this component
        if (contains(e.getPoint())) {
            super.processMouseEvent(e);
        } else {
            // Let the event propagate up to the parent
            getParent().dispatchEvent(e);
        }
    }

    private double calculateAppropriateScale(Model model) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < model.vertexCount; i++) {
            minX = Math.min(minX, model.verticesX[i]);
            minY = Math.min(minY, model.verticesY[i]);
            minZ = Math.min(minZ, model.verticesZ[i]);
            maxX = Math.max(maxX, model.verticesX[i]);
            maxY = Math.max(maxY, model.verticesY[i]);
            maxZ = Math.max(maxZ, model.verticesZ[i]);
        }

        float width = maxX - minX;
        float height = maxY - minY;
        float depth = maxZ - minZ;

        float maxDimension = Math.max(Math.max(width, height), depth);

        return 100.0 / maxDimension;
    }

    private void centerModel(MeshView meshView, Model model) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < model.vertexCount; i++) {
            minX = Math.min(minX, model.verticesX[i]);
            minY = Math.min(minY, model.verticesY[i]);
            minZ = Math.min(minZ, model.verticesZ[i]);
            maxX = Math.max(maxX, model.verticesX[i]);
            maxY = Math.max(maxY, model.verticesY[i]);
            maxZ = Math.max(maxZ, model.verticesZ[i]);
        }

        float centerX = (minX + maxX) / 2.0f;
        float centerY = (minY + maxY) / 2.0f;
        float centerZ = (minZ + maxZ) / 2.0f;

        meshView.getTransforms().add(new Translate(-centerX, -centerY, -centerZ));
    }

    private void startRotation() {
        if (rotationTimer != null) {
            rotationTimer.stop();
        }

        rotationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                rotateModel();
            }
        };

        rotationTimer.start();
    }

    private void rotateModel() {
        modelRotation = (modelRotation + 0.5) % 360;

        modelGroup.getTransforms().clear();

        Rotate rotateY = new Rotate(modelRotation, Rotate.Y_AXIS);
        modelGroup.getTransforms().add(rotateY);
    }

    public void stopRotation() {
        if (rotationTimer != null) {
            rotationTimer.stop();
        }
    }
}