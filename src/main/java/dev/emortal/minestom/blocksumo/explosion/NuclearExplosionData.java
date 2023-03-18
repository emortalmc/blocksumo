package dev.emortal.minestom.blocksumo.explosion;

/**
 * @param completeRad instant death, total destruction
 * @param severeRad   darkness, 60% block damage, 100% knockback
 * @param moderateRad nausea, 30% block damage, 50% knockback
 * @param lightRad    nausea, 20% knockback
 */
public record NuclearExplosionData(double completeRad, double severeRad, double moderateRad, double lightRad) {

    public static NuclearExplosionData fromRadius(double radius) {
        return new NuclearExplosionData(
                radius * 0.285,
                radius * 0.357,
                radius * 0.571,
                radius
        );
    }
}
