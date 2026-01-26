package org.lostcitymapeditor.Renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;
    private float yaw;
    private float pitch;
    private float zoom;
    private float movementSpeed;
    private float mouseSensitivity;

    // Fixed per-scroll "zoom step" (world units). No new dependencies.
    // Tune this number to taste. It is intentionally decoupled from deltaTime.
    private float scrollZoomStep = 250.0f;

    public Camera(Vector3f position) {
        this.position = position;

        // Preserve your existing coordinate convention.
        this.worldUp = new Vector3f(0.0f, -1.0f, 0.0f);

        this.yaw = 90.0f;
        this.pitch = 90.0f;

        this.movementSpeed = 3000f;
        this.mouseSensitivity = 0.1f;
        this.zoom = 40.0f;

        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.up = new Vector3f();
        this.right = new Vector3f();

        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f();
        Vector3f target = new Vector3f();
        position.add(front, target);
        return view.lookAt(position, target, up);
    }

    public float getZoom() {
        return zoom;
    }

    public void processKeyboard(CameraMovement direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;

        /*
            Map-editor UX change retained:
              - FORWARD/BACKWARD/LEFT/RIGHT are projected onto the "horizontal plane"
                (plane perpendicular to worldUp), so WASD pans instead of unintentionally
                moving vertically when pitch is steep.
         */

        // Project front onto plane perpendicular to worldUp.
        Vector3f forwardFlat = new Vector3f(front);
        float frontDotUp = forwardFlat.dot(worldUp);
        forwardFlat.fma(-frontDotUp, worldUp); // forwardFlat -= (frontDotUp * worldUp)

        if (forwardFlat.lengthSquared() > 1e-8f) {
            forwardFlat.normalize();
        } else {
            forwardFlat.set(0.0f, 0.0f, 0.0f);
        }

        // Project right onto plane perpendicular to worldUp.
        Vector3f rightFlat = new Vector3f(right);
        float rightDotUp = rightFlat.dot(worldUp);
        rightFlat.fma(-rightDotUp, worldUp); // rightFlat -= (rightDotUp * worldUp)

        if (rightFlat.lengthSquared() > 1e-8f) {
            rightFlat.normalize();
        } else {
            rightFlat.set(0.0f, 0.0f, 0.0f);
        }

        if (direction == CameraMovement.FORWARD)
            position.add(new Vector3f(forwardFlat).mul(velocity));
        if (direction == CameraMovement.BACKWARD)
            position.sub(new Vector3f(forwardFlat).mul(velocity));
        if (direction == CameraMovement.LEFT)
            position.sub(new Vector3f(rightFlat).mul(velocity));
        if (direction == CameraMovement.RIGHT)
            position.add(new Vector3f(rightFlat).mul(velocity));

        // Keep your existing zoom behavior as-is (frame-rate scaled via deltaTime).
        if (direction == CameraMovement.ZOOM_IN) {
            position.sub(new Vector3f(0, 1, 0).mul(velocity));
        }
        if (direction == CameraMovement.ZOOM_OUT) {
            position.add(new Vector3f(0, 1, 0).mul(velocity));
        }
    }

    public void processKeyboardInput(long window, float deltaTime) {
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            processKeyboard(CameraMovement.FORWARD, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            processKeyboard(CameraMovement.BACKWARD, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            processKeyboard(CameraMovement.LEFT, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            processKeyboard(CameraMovement.RIGHT, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)
            processKeyboard(CameraMovement.ZOOM_IN, deltaTime);
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS)
            processKeyboard(CameraMovement.ZOOM_OUT, deltaTime);
    }

    /**
     * Mouse wheel zoom alias:
     *   yOffset > 0 => ZOOM_IN
     *   yOffset < 0 => ZOOM_OUT
     *
     * This intentionally does NOT require deltaTime to avoid breaking your callback wiring.
     */
    public void processMouseScroll(float yOffset) {
        if (yOffset > 0.0f) {
            // Zoom in: move opposite the +Y axis used in your Q/E logic.
            position.sub(new Vector3f(0, 1, 0).mul(scrollZoomStep));
        } else if (yOffset < 0.0f) {
            position.add(new Vector3f(0, 1, 0).mul(scrollZoomStep));
        }
    }

    public void processMouseMovement(float xOffset, float yOffset, boolean constrainPitch) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw -= xOffset;
        pitch -= yOffset;

        if (constrainPitch) {
            if (pitch < -89.0f)
                pitch = -89.0f;
            if (pitch > 89.0f)
                pitch = 89.0f;
        }

        updateCameraVectors();
    }

    private void updateCameraVectors() {
        Vector3f newFront = new Vector3f();
        newFront.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        newFront.y = (float) Math.sin(Math.toRadians(pitch));
        newFront.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));

        front = newFront.normalize();
        right = new Vector3f(front).cross(worldUp).normalize();
        up = new Vector3f(right).cross(front).normalize();
    }

    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        ZOOM_IN,
        ZOOM_OUT
    }
}
