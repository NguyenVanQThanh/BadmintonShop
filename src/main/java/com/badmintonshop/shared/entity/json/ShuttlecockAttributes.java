package com.badmintonshop.shared.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the technical specifications for Shuttlecocks.
 * <p>
 * Shuttlecocks are consumable items with highly specific aerodynamic properties.
 * Their performance is heavily influenced by environmental factors (temperature, altitude).
 * Therefore, storing attributes like {@code speed} and {@code materialType} is essential
 * to help customers select the right shuttle for their playing conditions.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShuttlecockAttributes extends ProductAttributes {

    /**
     * The speed rating of the shuttlecock, indicating how far it flies.
     * <p>
     * Speed is inversely related to air density (temperature/altitude).
     * Standard Metric System:
     * <ul>
     * <li><b>76</b>: Slow speed (Used in hot weather/summer or high altitude).</li>
     * <li><b>77</b>: Medium speed (Standard, most common in sea-level countries like Vietnam).</li>
     * <li><b>78</b>: Fast speed (Used in cold weather/winter or low altitude).</li>
     * </ul>
     * </p>
     */
    private Integer speed;

    /**
     * The primary material used for the skirt (feathers).
     * <p>
     * This dictates durability, flight trajectory, and price.
     * Values:
     * <ul>
     * <li><b>Goose Feather</b>: Premium, consistent flight, expensive (Tournament standard).</li>
     * <li><b>Duck Feather</b>: Good durability, slightly different flight path, affordable (Training).</li>
     * <li><b>Nylon / Plastic</b>: Extremely durable, different flight physics (Beginners/Outdoor).</li>
     * </ul>
     * </p>
     */
    private String materialType;

    /**
     * The number of shuttlecocks contained in a single packaging unit (Tube).
     * <p>
     * Standard industry packaging:
     * <ul>
     * <li><b>12</b>: Standard Dozen (The vast majority of sales).</li>
     * <li><b>6</b>: Half-dozen tube.</li>
     * <li><b>3 or 1</b>: Trial packs or Nylon shuttles.</li>
     * </ul>
     * </p>
     */
    private Integer quantityPerTube;

    /**
     * The quality classification or marketing tier assigned by the manufacturer.
     * <p>
     * Used to manage customer expectations regarding consistency and durability.
     * Examples:
     * <ul>
     * <li>"International / Tournament": Approved for official BWF matches.</li>
     * <li>"Club / Match": Reliable for serious amateur play.</li>
     * <li>"Training / Practice": Lower consistency, suitable for drills.</li>
     * <li>"Aerosensa 50", "Aeroclub TR" (Specific model grades).</li>
     * </ul>
     * </p>
     */
    private String grade;
}