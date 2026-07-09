package dev.efnilite.vilib.particle;

import org.bukkit.Particle;

/**
 * Folia 1.21.x 兼容的 ParticleData。
 * 原版 vilib 的 Particles 类在 1.21 上因粒子 API 变更而出错，
 * 此类标记为已弃用以触发安全回退。
 */
public class ParticleData<T> {

    private final Particle particle;
    private final T data;
    private final int size;
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final double speed;

    public ParticleData(Particle particle, T data, int size, double offsetX, double offsetY, double offsetZ, double speed) {
        this.particle = particle;
        this.data = data;
        this.size = size;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.speed = speed;
    }

    public ParticleData(Particle particle, T data, int size) {
        this(particle, data, size, 0, 0, 0, 0);
    }

    public Particle particle() { return particle; }
    public T data() { return data; }
    public int size() { return size; }
    public double offsetX() { return offsetX; }
    public double offsetY() { return offsetY; }
    public double offsetZ() { return offsetZ; }
    public double speed() { return speed; }
    
    // Builder methods for chaining
    public ParticleData<T> size(int size) { return new ParticleData<>(particle, data, size, offsetX, offsetY, offsetZ, speed); }
    public ParticleData<T> offsetX(double x) { return new ParticleData<>(particle, data, size, x, offsetY, offsetZ, speed); }
    public ParticleData<T> offsetY(double y) { return new ParticleData<>(particle, data, size, offsetX, y, offsetZ, speed); }
    public ParticleData<T> offsetZ(double z) { return new ParticleData<>(particle, data, size, offsetX, offsetY, z, speed); }
    public ParticleData<T> speed(double speed) { return new ParticleData<>(particle, data, size, offsetX, offsetY, offsetZ, speed); }
}
