package com.badmintonshop.config;

import com.badmintonshop.entity.*;
import com.badmintonshop.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Startup service responsible for seeding initial data into the database from an Excel file.
 * <p>
 * <b>File Source:</b> {@code src/main/resources/data/badminton_products_seed.xlsx}
 * </p>
 * <p>
 * <b>Execution Strategy:</b>
 * The seeder checks if the {@code Product} table is empty. If data exists, the process is skipped
 * to preserve data integrity and prevent duplicates.
 * </p>
 *
 * @author YourName
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelDataSeeder implements CommandLineRunner {

    private static final String SEED_FILE_PATH = "data/badminton_products_seed.xlsx";
    private static final String SHEET_BRANDS = "Brands";
    private static final String SHEET_CATEGORIES = "Categories";
    private static final String SHEET_PRODUCTS = "Products_Import";

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    
    /**
     * Core Jackson object mapper used for parsing JSON strings from the Excel file
     * into Java Maps for the {@code attributes} JSONB column.
     */
    private final ObjectMapper objectMapper;
    
    /**
     * Apache POI utility for formatting cell values (ensures Numbers/Dates are read as Strings).
     */
    private final DataFormatter dataFormatter = new DataFormatter();

    /**
     * Main execution method triggered by Spring Boot at application startup.
     * <p>
     * Wraps the entire import process in a Transaction to ensure atomicity,
     * though partial failures in row processing are logged rather than rolling back everything.
     * </p>
     *
     * @param args Command line arguments (not used).
     */
    @Override
    @Transactional
    public void run(String... args) {
        // Idempotency check: Prevent running if data is already present
        if (productRepository.count() > 0) {
            log.info(">>> [Data Seeder] Database is already populated. Skipping Excel import.");
            return;
        }

        log.info(">>> [Data Seeder] Starting import from: {}", SEED_FILE_PATH);
        ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);

        if (!resource.exists()) {
            log.error(">>> [Data Seeder] File not found: {}", SEED_FILE_PATH);
            return;
        }

        try (InputStream is = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            importBrands(workbook.getSheet(SHEET_BRANDS));
            importCategories(workbook.getSheet(SHEET_CATEGORIES));
            importProducts(workbook.getSheet(SHEET_PRODUCTS));

            log.info(">>> [Data Seeder] Import completed successfully! 🎉");
        } catch (Exception e) {
            log.error(">>> [Data Seeder] Critical failure during import process", e);
        }
    }

    /**
     * Imports Brand data from the designated Excel sheet.
     * <p>
     * Logic: Iterates through rows, checks if a brand name exists, and creates it if missing.
     * </p>
     *
     * @param sheet The Excel sheet containing Brand data.
     */
    private void importBrands(Sheet sheet) {
        if (sheet == null) {
            log.warn(">>> [Data Seeder] Sheet '{}' not found.", SHEET_BRANDS);
            return;
        }
        log.info("--- Importing Brands ---");
        
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip Header Row

            String name = getCellValueAsString(row.getCell(0));
            String logoUrl = getCellValueAsString(row.getCell(1));

            if (!name.isEmpty() && !brandRepository.existsByName(name)) {
                brandRepository.save(Brand.builder()
                        .name(name)
                        .logoUrl(logoUrl)
                        .build());
            }
        }
    }

    /**
     * Imports Category data handling Hierarchical Relationships (Parent-Child).
     * <p>
     * <b>Algorithm:</b>
     * <ol>
     * <li><b>Phase 1:</b> Create all categories as root entities. Store Parent-Child mappings in memory.</li>
     * <li><b>Phase 2:</b> Iterate through the mapping and update the Parent relationship for child categories.</li>
     * </ol>
     * This prevents {@code EntityNotFoundException} if a child is processed before its parent.
     * </p>
     *
     * @param sheet The Excel sheet containing Category data.
     */
    private void importCategories(Sheet sheet) {
        if (sheet == null) {
            log.warn(">>> [Data Seeder] Sheet '{}' not found.", SHEET_CATEGORIES);
            return;
        }
        log.info("--- Importing Categories ---");
        
        // Map to hold temporary relationships: ChildSlug -> ParentSlug
        Map<String, String> parentSlugMap = new HashMap<>();

        // Phase 1: Creation
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            String name = getCellValueAsString(row.getCell(0));
            String slug = getCellValueAsString(row.getCell(1));
            String parentSlug = getCellValueAsString(row.getCell(2));
            String description = getCellValueAsString(row.getCell(3));

            if (!name.isEmpty() && !categoryRepository.existsBySlug(slug)) {
                Category cat = Category.builder()
                        .name(name)
                        .slug(slug)
                        .description(description)
                        .build();
                categoryRepository.save(cat);

                if (!parentSlug.isEmpty()) {
                    parentSlugMap.put(slug, parentSlug);
                }
            }
        }

        // Phase 2: Relationship Linking
        parentSlugMap.forEach((childSlug, parentSlug) -> {
            Category child = categoryRepository.findBySlug(childSlug).orElse(null);
            Category parent = categoryRepository.findBySlug(parentSlug).orElse(null);
            
            if (child != null && parent != null) {
                child.setParent(parent);
                categoryRepository.save(child);
            }
        });
    }

    /**
     * Imports Products and their Variants.
     * <p>
     * Each row in the Excel sheet represents a specific <b>Product Variant</b> (SKU).
     * The method intelligently resolves or creates the parent Product based on the name.
     * </p>
     *
     * @param sheet The Excel sheet containing Product & Variant data.
     */
    private void importProducts(Sheet sheet) {
        if (sheet == null) {
            log.warn(">>> [Data Seeder] Sheet '{}' not found.", SHEET_PRODUCTS);
            return;
        }
        log.info("--- Importing Products & Variants ---");
        
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            try {
                processProductRow(row);
            } catch (Exception e) {
                // Log error specifically for this row but continue processing others
                log.error("Error processing row {}: {}", row.getRowNum(), e.getMessage());
            }
        }
    }

    /**
     * Parses a single Excel row and persists Product/Variant entities.
     * <p>
     * <b>Column Mapping:</b>
     * <ul>
     * <li>0: Product Name</li>
     * <li>1: Brand Name (Lookup)</li>
     * <li>2: Category Slug (Lookup)</li>
     * <li>3: SKU (Variant Unique Key)</li>
     * <li>4: Price</li>
     * <li>6: Image URL</li>
     * <li>7: JSON Attributes (Weight, Grip, etc.)</li>
     * </ul>
     * </p>
     *
     * @param row The Excel Row to process.
     * @throws Exception If Brand/Category is missing or JSON parsing fails.
     */
    private void processProductRow(Row row) throws Exception {
        // 1. Data Extraction
        String productName = getCellValueAsString(row.getCell(0));
        String brandName = getCellValueAsString(row.getCell(1));
        String categorySlug = getCellValueAsString(row.getCell(2));
        String sku = getCellValueAsString(row.getCell(3));
        
        // Price Cleanup: Remove commas (e.g., "4,500,000" -> "4500000")
        String priceStr = getCellValueAsString(row.getCell(4)).replaceAll(",", "").trim();
        BigDecimal price = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);

        String imageUrl = getCellValueAsString(row.getCell(6));
        String jsonAttr = getCellValueAsString(row.getCell(7));

        if (sku.isEmpty() || productName.isEmpty()) {
            return; // Skip invalid rows
        }

        // 2. Dependency Resolution
        Brand brand = brandRepository.findByName(brandName)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + brandName));

        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categorySlug));

        // 3. Parent Product Resolution (Find Existing or Create New)
        Product product = productRepository.findByName(productName)
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(productName)
                        .brand(brand)
                        .category(category)
                        .thumbnailUrl(imageUrl)
                        .description("Imported via Excel Seeder")
                        .isActive(true)
                        .build()));

        // 4. Variant Creation (Idempotent check by SKU)
        if (!productVariantRepository.existsBySku(sku)) {
            Map<String, Object> attributes = new HashMap<>();
            
            // Parse JSON String to Map
            if (!jsonAttr.isEmpty()) {
                attributes = objectMapper.readValue(jsonAttr, new TypeReference<Map<String, Object>>() {});
            }

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .sku(sku)
                    .price(price)
                    .stockQuantity(10) // Default initial stock
                    .imageUrl(imageUrl)
                    .attributes(attributes)
                    .build();

            productVariantRepository.save(variant);
            log.info("Imported Variant: {}", sku);
        }
    }

    /**
     * Utility method to safely extract cell values as Strings.
     * <p>
     * Uses {@link DataFormatter} to handle various cell types (Numeric, Formula, Boolean)
     * preventing {@code IllegalStateException} when reading non-string cells.
     * </p>
     *
     * @param cell The Excel cell to read.
     * @return The string representation of the cell value, or empty string if null.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell).trim();
    }
}