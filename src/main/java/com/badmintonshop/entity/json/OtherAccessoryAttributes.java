package com.badmintonshop.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents attributes for miscellaneous badminton accessories that do not fall into 
 * primary categories (like Rackets, Shoes, Strings, or Apparel).
 * <p>
 * This is a flexible "catch-all" container designed to handle diverse items 
 * such as Grips, Bags, Wristbands, Socks, and Training Equipment.
 * The interpretation of fields like {@code specification} depends on the {@code subCategory}.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtherAccessoryAttributes extends ProductAttributes {

    /**
     * The specific classification of the accessory.
     * <p>
     * Since this class covers a wide range of products, this field is critical for 
     * filtering and categorization on the frontend.
     * Recommended Standard Values:
     * <ul>
     * <li>"GRIP": Racket grips (Quấn cán).</li>
     * <li>"BAG": Racket bags, backpacks (Bao vợt, Balo).</li>
     * <li>"SOCKS": Sports socks (Tất cầu lông).</li>
     * <li>"WRISTBAND": Sweatbands (Băng tay/đầu).</li>
     * <li>"TRAINING": Powerballs, heavy rackets (Dụng cụ tập luyện).</li>
     * </ul>
     * </p>
     */
    private String subCategory;

    /**
     * A generic field capturing the primary technical dimension or capacity of the item.
     * <p>
     * The meaning of this value varies based on the {@code subCategory}:
     * <ul>
     * <li>For <b>GRIP</b>: Thickness (e.g., "0.6mm", "Thin").</li>
     * <li>For <b>BAG</b>: Capacity or Dimensions (e.g., "2 Compartments", "75x24x32cm").</li>
     * <li>For <b>SOCKS</b>: Size range (e.g., "Free Size", "25-28cm").</li>
     * <li>For <b>TRAINING</b>: Weight or Resistance (e.g., "140g", "20kg spin force").</li>
     * </ul>
     * </p>
     */
    private String specification;

    /**
     * The primary material composition of the accessory.
     * <p>
     * Examples:
     * <ul>
     * <li>"Polyurethane (PU)" (for Grips - creating tackiness).</li>
     * <li>"Polyester/PVC" (for Bags - durability).</li>
     * <li>"Cotton/Spandex" (for Socks - comfort and elasticity).</li>
     * </ul>
     * </p>
     */
    private String material;
}