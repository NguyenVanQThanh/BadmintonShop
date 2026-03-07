package com.badmintonshop.shared.entity.json;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the technical specifications for Badminton Rackets.
 * <p>
 * Racket specifications are the primary decision-making factors for customers.
 * This class captures the "Holy Trinity" of racket stats: Weight, Balance, and Stiffness,
 * along with grip size and tension limits.
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RacketAttributes extends ProductAttributes {

    /**
     * The weight classification of the racket frame.
     * <p>
     * Typically follows the Yonex "U" standard:
     * <ul>
     * <li><b>2U</b>: 90-94g (Heavy, rare in modern play).</li>
     * <li><b>3U</b>: 85-89g (Standard for singles/power players).</li>
     * <li><b>4U</b>: 80-84g (Standard for doubles/speed players).</li>
     * <li><b>5U/F</b>: <80g (Lightweight, beginner-friendly).</li>
     * </ul>
     * Note: Li-Ning uses W1/W2/W3 syntax, so this field is kept as String.
     * </p>
     */
    private String weight;

    /**
     * The circumference size of the handle.
     * <p>
     * Typically follows the "G" standard (G2 to G6).
     * <b>Note:</b> A higher number indicates a thinner handle.
     * <ul>
     * <li><b>G5</b>: Standard/Most common size.</li>
     * <li><b>G6</b>: Thin (often for players with smaller hands).</li>
     * </ul>
     * </p>
     */
    private String gripSize;

    /**
     * The balance point of the racket, determining its play style.
     * <p>
     * Values can be descriptive or specific measurements (in mm from the handle butt).
     * Common Values:
     * <ul>
     * <li><b>Head Heavy</b> (>295mm): For Power/Smashing (Attacking style).</li>
     * <li><b>Head Light</b> (<285mm): For Speed/Defense (Defensive style).</li>
     * <li><b>Even Balance</b> (285-295mm): All-around control.</li>
     * </ul>
     * </p>
     */
    private String balance;

    /**
     * The manufacturer's recommended string tension range.
     * <p>
     * This is a safety limit for the frame. Exceeding this may void the warranty.
     * Examples: "20-28 lbs", "Max 30 lbs", "19-24 lbs".
     * </p>
     */
    private String tension;

    /**
     * The flexibility of the racket shaft.
     * <p>
     * This dictates how much the shaft bends during a swing.
     * <ul>
     * <li><b>Extra Stiff / Stiff</b>: High accuracy, suitable for pros with fast swing speeds.</li>
     * <li><b>Medium</b>: Balanced performance.</li>
     * <li><b>Flexible / Hi-Flex</b>: Generates power easily, suitable for beginners or slow swing speeds.</li>
     * </ul>
     * </p>
     */
    private String stiffness;
}