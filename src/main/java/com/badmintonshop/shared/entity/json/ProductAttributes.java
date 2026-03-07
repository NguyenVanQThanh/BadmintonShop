package com.badmintonshop.shared.entity.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;

/**
 * Abstract base class representing the dynamic attributes of a product variant.
 * <p>
 * This class serves as the polymorphic parent for the JSONB column in the database.
 * It utilizes Jackson annotations to handle polymorphic deserialization, allowing
 * the system to automatically map the JSON data to the correct subclass (e.g., Racket, Shoe)
 * based on the "type" property.
 * </p>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME
    , include = JsonTypeInfo.As.PROPERTY
    , property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RacketAttributes.class, name = "RACKET")
    , @JsonSubTypes.Type(value = ShoeAttributes.class, name = "SHOE")
    , @JsonSubTypes.Type(value = ShuttlecockAttributes.class, name = "SHUTTLECOCK")
    , @JsonSubTypes.Type(value = RacketStringAttributes.class, name = "STRING")
    , @JsonSubTypes.Type(value = ApparelAttributes.class, name = "APPAREL")
    , @JsonSubTypes.Type(value = OtherAccessoryAttributes.class, name = "OTHER")
})
@Data
public abstract class ProductAttributes implements Serializable {

    /**
     * The specific colorway or design variation of this product variant.
     * <p>
     * Even if a product has a generic "Color" in the description, this field captures
     * the exact color code or marketing name.
     * Examples: "Navy Blue/Gold", "Kurenai", "White/Tiger Red".
     * </p>
     */
    private String color;

    /**
     * A specific description or note for this particular variant.
     * <p>
     * Use this field to highlight unique characteristics that differ from the main product description.
     * Examples:
     * <ul>
     * <li>"Limited Edition 75th Anniversary"</li>
     * <li>"Unstrung frame only" (for rackets)</li>
     * <li>"Comes with thermal bag"</li>
     * </ul>
     * </p>
     */
    private String description;
}