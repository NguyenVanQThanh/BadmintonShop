package com.badmintonshop.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the technical specifications for Badminton Footwear.
 * <p>
 * Unlike running shoes, badminton shoes are engineered for lateral stability (moving side-to-side),
 * anti-slip traction on indoor courts, and high-impact shock absorption.
 * These attributes help customers choose shoes that prevent injury and enhance footwork.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShoeAttributes extends ProductAttributes {

    /**
     * The size designation of the shoe.
     * <p>
     * Stored as a String to accommodate various formats and half-sizes.
     * <b>Important:</b> Badminton shoes must fit snugly to prevent blisters during rapid changes of direction.
     * Examples: "40", "40.5", "25.5 CM", "UK 7.5".
     * </p>
     */
    private String size;

    /**
     * The target gender or fit profile.
     * <p>
     * This primarily dictates the width of the shoe (Last shape).
     * <ul>
     * <li><b>Men</b>: Standard to Wide fit.</li>
     * <li><b>Women</b>: Narrower heel and mid-foot fit.</li>
     * <li><b>Unisex</b>: Very common in high-performance models; usually follows Men's width sizing.</li>
     * <li><b>Junior</b>: Specialized support for growing feet.</li>
     * </ul>
     * </p>
     */
    private String gender;

    /**
     * The technology or pattern of the outsole (bottom layer).
     * <p>
     * <b>Critical Requirement:</b> Must be "Non-marking" gum rubber to protect court surfaces.
     * Specific patterns offer better grip:
     * <ul>
     * <li>"Hexagrip": Provides 3% more grip and is 20% lighter (Yonex standard).</li>
     * <li>"Radial Blade": For multi-directional traction.</li>
     * <li>"Round Sole": For smooth movements.</li>
     * </ul>
     * </p>
     */
    private String soleType;

    /**
     * The midsole shock absorption technology.
     * <p>
     * This is the most marketable feature, protecting the player's knees and ankles during jump smashes.
     * Examples:
     * <ul>
     * <li>"Power Cushion / Power Cushion+": The signature Yonex egg-drop technology.</li>
     * <li>"Energymax / Energymax 3.0": Victor's elastic cushioning.</li>
     * <li>"Bounse+": Li-Ning's rebound technology.</li>
     * </ul>
     * </p>
     */
    private String cushionTech;
}