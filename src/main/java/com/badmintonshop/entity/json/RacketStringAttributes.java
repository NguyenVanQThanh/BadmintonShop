package com.badmintonshop.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the specific technical specifications for Badminton Racket Strings.
 * <p>
 * This class captures detailed metrics used by manufacturers (like Yonex, Victor, Li-Ning)
 * to rate string performance. These attributes are crucial for advanced players
 * when filtering or comparing products.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RacketStringAttributes extends ProductAttributes {

    /**
     * The diameter or thickness of the string, typically measured in millimeters.
     * <p>
     * Thinner strings (e.g., 0.61mm) offer better repulsion and sound but lower durability.
     * Thicker strings (e.g., 0.70mm) offer better durability and control.
     * Example values: "0.61mm", "0.65mm", "0.70mm".
     * </p>
     */
    private String gauge;

    /**
     * Rating for the string's longevity and resistance to breakage.
     * <p>
     * Scale: Typically 1 to 10 (10 being the most durable).
     * High durability is often preferred by players who break strings frequently.
     * </p>
     */
    private Integer durability;

    /**
     * Rating for the string's elasticity and power generation (bounciness).
     * <p>
     * Scale: Typically 1 to 10 (10 being high repulsion).
     * High repulsion helps generate power with less effort.
     * </p>
     */
    private Integer repulsionPower;

    /**
     * Rating for the acoustic quality when hitting the shuttlecock.
     * <p>
     * Scale: Typically 1 to 10.
     * A high score indicates a crisp, high-pitched sound (often preferred by players).
     * </p>
     */
    private Integer hittingSound;

    /**
     * Rating for the string's ability to "hold" the shuttle on the string bed
     * for precise placement.
     * <p>
     * Scale: Typically 1 to 10.
     * </p>
     */
    private Integer control;

    /**
     * The manufacturing origin of the string.
     * <p>
     * This attribute significantly impacts the price and perceived quality in the badminton market.
     * Examples: "Made in Japan" (Premium), "Made in Taiwan", "Made in China".
     * </p>
     */
    private String origin;
}