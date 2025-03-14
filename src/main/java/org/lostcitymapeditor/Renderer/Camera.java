package org.lostcitymapeditor.Renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

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
    public Camera(Vector3f position) {
        this.position = position;
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
        if (direction == CameraMovement.FORWARD)
            position.add(new Vector3f(front).mul(velocity));
        if (direction == CameraMovement.BACKWARD)
            position.sub(new Vector3f(front).mul(velocity));
        if (direction == CameraMovement.LEFT)
            position.sub(new Vector3f(right).mul(velocity));
        if (direction == CameraMovement.RIGHT)
            position.add(new Vector3f(right).mul(velocity));
        if (direction == CameraMovement.ZOOM_IN) {
            position.sub(new Vector3f(0,1,0).mul(velocity));
        }
        if (direction == CameraMovement.ZOOM_OUT) {
            position.add(new Vector3f(0,1,0).mul(velocity));
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
