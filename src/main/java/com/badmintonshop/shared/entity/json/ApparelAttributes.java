package com.badmintonshop.shared.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the specific attributes for badminton apparel and clothing.
 * <p>
 * Badminton apparel requires specific data regarding sizing standards (Asian vs. Euro),
 * fabric technology (breathability, sweat-wicking), and cut (gender-specific designs).
 * This class is mapped to the "APPAREL" type in the JSONB structure.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApparelAttributes extends ProductAttributes {

    /**
     * The size designation of the apparel item.
     * <p>
     * Note: Badminton brands (like Yonex, Victor) often have different sizing standards
     * based on the region (e.g., "Asian Size L" might equal "Euro Size M").
     * It is recommended to store the exact label value or a standardized code.
     * Examples: "XS", "S", "M", "L", "XL", "2XL".
     * </p>
     */
    private String size;

    /**
     * The fabric composition or material technology used.
     * <p>
     * In badminton, material is critical for performance (sweat management and flexibility).
     * Examples: "100% Polyester", "Spandex Blend", "Cool-Dry Technology", "TruBreeze".
     * </p>
     */
    private String material;

    /**
     * The target gender demographic for the item.
     * <p>
     * This affects the cut and fit of the garment.
     * Values:
     * <ul>
     * <li>"Men": Standard male cut.</li>
     * <li>"Women": Fitted cut, often shorter sleeves or waist-tapered.</li>
     * <li>"Unisex": Common for team jerseys and club uniforms.</li>
     * </ul>
     * </p>
     */
    private String gender;

    /**
     * The specific category or style of the apparel.
     * <p>
     * Used for granular filtering on the storefront.
     * Examples: "Jersey" (Áo thi đấu), "Training Shirt" (Áo tập),
     * "Shorts" (Quần đùi), "Skorts" (Váy giả quần for Women), "Tracksuit" (Bộ đồ gió).
     * </p>
     */
    private String type;
}