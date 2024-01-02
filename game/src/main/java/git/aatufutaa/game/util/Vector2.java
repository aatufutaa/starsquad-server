package git.aatufutaa.game.util;

import lombok.Getter;

public class Vector2 {

    @Getter
    private float x;
    @Getter
    private float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void multiply(float value) {
        this.x *= value;
        this.y *= value;
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public void normalize() {
        float length = this.length();
        if (length > 0) {
            this.x /= length;
            this.y /= length;
        }
    }

    public float distance(Vector2 vec) {
        return (float) Math.hypot(vec.x - this.x, vec.y - this.y);
    }

    @Override
    public String toString() {
        return "Vector2{" + this.x + "," + this.y + "}";
    }
}
