package com.badmintonshop.shared.config;

import com.badmintonshop.admin.service.FirebaseStorageService;
import com.badmintonshop.shared.entity.*;
import com.badmintonshop.shared.repository.*;
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
 * <b>Image Handling:</b> Column 6 of the Products sheet contains the image filename
 * (e.g., {@code ryuga2.jpg}). On startup, the seeder loads each image from
 * {@code src/main/resources/data/Image_Badminton/}, uploads it to Firebase Storage,
 * and stores the returned public URL in the database.
 * </p>
 * <p>
 * <b>Execution Strategy:</b>
 * The seeder checks if the {@code Product} table is empty. If data exists, the process is skipped
 * to preserve data integrity and prevent duplicates.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExcelDataSeeder implements CommandLineRunner {

    private static final String SEED_FILE_PATH = "data/badminton_products_seed.xlsx";
    private static final String IMAGE_FOLDER_PATH = "data/Image_Badminton/";
    private static final String FIREBASE_IMAGE_PREFIX = "products/";
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
     * Firebase Storage service used to upload product images from classpath resources.
     */
    private final FirebaseStorageService firebaseStorageService;

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
            log.info("[Data Seeder] Database is already populated. Skipping Excel import.");
            return;
        }

        log.info("[Data Seeder] Starting import from: {}", SEED_FILE_PATH);
        ClassPathResource resource = new ClassPathResource(SEED_FILE_PATH);

        if (!resource.exists()) {
            log.error("[Data Seeder] Seed file not found: {}", SEED_FILE_PATH);
            return;
        }

        try (InputStream is = resource.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            importBrands(workbook.getSheet(SHEET_BRANDS));
            importCategories(workbook.getSheet(SHEET_CATEGORIES));
            importProducts(workbook.getSheet(SHEET_PRODUCTS));

            log.info("[Data Seeder] Import completed successfully.");
        } catch (Exception e) {
            log.error("[Data Seeder] Critical failure during import process", e);
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
            log.warn("[Data Seeder] Sheet '{}' not found. Skipping.", SHEET_BRANDS);
            return;
        }
        log.info("[Data Seeder] Importing Brands...");

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
            log.warn("[Data Seeder] Sheet '{}' not found. Skipping.", SHEET_CATEGORIES);
            return;
        }
        log.info("[Data Seeder] Importing Categories...");

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
     * <p>
     * <b>Upload Cache:</b> A local cache ({@code imageFileName -> Firebase URL}) is maintained
     * for the duration of this import run. If multiple variants share the same image filename,
     * the file is uploaded only once and the resulting URL is reused, avoiding redundant
     * Firebase Storage requests.
     * </p>
     *
     * @param sheet The Excel sheet containing Product and Variant data.
     */
    private void importProducts(Sheet sheet) {
        if (sheet == null) {
            log.warn("[Data Seeder] Sheet '{}' not found. Skipping.", SHEET_PRODUCTS);
            return;
        }
        log.info("[Data Seeder] Importing Products and Variants...");

        // Cache: imageFileName -> Firebase public URL (prevents re-uploading the same file)
        Map<String, String> imageUrlCache = new HashMap<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            try {
                processProductRow(row, imageUrlCache);
            } catch (Exception e) {
                // Log error for this specific row and continue processing remaining rows
                log.error("[Data Seeder] Failed to process row {}. Reason: {}", row.getRowNum(), e.getMessage());
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
     * <li>5: Stock Quantity</li>
     * <li>6: Image Filename (e.g., {@code ryuga2.jpg}) — uploaded to Firebase Storage</li>
     * <li>7: JSON Attributes (e.g., weight, grip, flex)</li>
     * </ul>
     * </p>
     *
     * @param row          The Excel Row to process.
     * @param imageUrlCache Shared cache map of {@code imageFileName -> Firebase URL} for this import run.
     * @throws Exception If Brand/Category is not found, image upload fails, or JSON parsing fails.
     */
    private void processProductRow(Row row, Map<String, String> imageUrlCache) throws Exception {
        // 1. Data Extraction
        String productName = getCellValueAsString(row.getCell(0));
        String brandName = getCellValueAsString(row.getCell(1));
        String categorySlug = getCellValueAsString(row.getCell(2));
        String sku = getCellValueAsString(row.getCell(3));

        // Price Cleanup: Remove commas (e.g., "4,500,000" -> "4500000")
        String priceStr = getCellValueAsString(row.getCell(4)).replaceAll(",", "").trim();
        BigDecimal price = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);

        String stockQtyStr = getCellValueAsString(row.getCell(5)).replaceAll(",", "").trim();
        int stockQuantity = stockQtyStr.isEmpty() ? 0 : (int) Double.parseDouble(stockQtyStr);

        String imageFileName = getCellValueAsString(row.getCell(6));
        String jsonAttr = getCellValueAsString(row.getCell(7));

        if (sku.isEmpty() || productName.isEmpty()) {
            return; // Skip invalid rows
        }

        // 2. Image Upload to Firebase Storage (reuse cached URL if same file was already uploaded)
        String imageUrl = imageUrlCache.computeIfAbsent(imageFileName, this::uploadImageToFirebase);

        // 3. Dependency Resolution
        Brand brand = brandRepository.findByName(brandName)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + brandName));

        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categorySlug));

        // 4. Parent Product Resolution (Find Existing or Create New)
        Product product = productRepository.findByName(productName)
                .orElseGet(() -> productRepository.save(Product.builder()
                        .name(productName)
                        .brand(brand)
                        .category(category)
                        .thumbnailUrl(imageUrl)
                        .description("Imported via Excel Seeder")
                        .status(com.badmintonshop.shared.entity.enums.ProductStatus.IN_STOCK)
                        .build()));

        // 5. Variant Creation (Idempotent check by SKU)
        if (!productVariantRepository.existsBySku(sku)) {
            Map<String, Object> attributes = new HashMap<>();

            if (!jsonAttr.isEmpty()) {
                attributes = objectMapper.readValue(jsonAttr, new TypeReference<Map<String, Object>>() {});
            }

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .sku(sku)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .imageUrl(imageUrl)
                    .attributes(attributes)
                    .build();

            productVariantRepository.save(variant);
            log.info("[Data Seeder] Imported variant: {}", sku);
        }
    }

    /**
     * Uploads a product image from the classpath to Firebase Storage.
     * <p>
     * The image is loaded from {@code data/Image_Badminton/{imageFileName}} within the classpath.
     * If the filename is empty or the file does not exist, an empty string is returned and
     * a warning is logged rather than throwing an exception.
     * </p>
     *
     * @param imageFileName The filename of the image (e.g., {@code ryuga2.jpg}).
     * @return The public Firebase Storage URL, or an empty string if the file is unavailable.
     */
    private String uploadImageToFirebase(String imageFileName) {
        if (imageFileName.isEmpty()) {
            return "";
        }

        String classpathPath = IMAGE_FOLDER_PATH + imageFileName;
        ClassPathResource imageResource = new ClassPathResource(classpathPath);

        if (!imageResource.exists()) {
            log.warn("[Data Seeder] Image file not found in classpath: '{}'. Storing empty URL.", classpathPath);
            return "";
        }

        try (InputStream imageStream = imageResource.getInputStream()) {
            String contentType = resolveContentType(imageFileName);
            String firebasePath = FIREBASE_IMAGE_PREFIX + imageFileName;
            log.info("[Data Seeder] Uploading image to Firebase: {} (ContentType: {})", firebasePath, contentType);
            String result = firebaseStorageService.uploadFileFromStream(imageStream, firebasePath, contentType);
            log.info("[Data Seeder] Image upload successful: {}", firebasePath);
            return result;
        } catch (Exception e) {
            log.error("[Data Seeder] Failed to upload image '{}' to Firebase. Storing empty URL. Reason: {}",
                    imageFileName, e.getMessage(), e);
            return "";
        }
    }

    /**
     * Resolves the MIME content type based on the file extension.
     *
     * @param fileName The file name including extension.
     * @return The corresponding MIME type string. Defaults to {@code image/jpeg} for unknown extensions.
     */
    private String resolveContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg"; // Default for .jpg / .jpeg
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
