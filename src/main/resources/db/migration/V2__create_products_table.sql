-- Products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    vendor VARCHAR(255),
    product_type VARCHAR(255),
    created_at TIMESTAMPTZ
);

-- Variants table (FK added after all tables exist)
CREATE TABLE variants (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    product_id BIGINT NOT NULL,
    image_id BIGINT,
    title VARCHAR(255) NOT NULL,
    option1 VARCHAR(255),
    option2 VARCHAR(255),
    option3 VARCHAR(255),
    sku VARCHAR(255),
    price NUMERIC(10, 2),
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ
);

-- Images table
CREATE TABLE images (
    id BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE NOT NULL,
    src VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ
);

-- Foreign keys with explicit names (clear error messages if anything fails)
ALTER TABLE variants
    ADD CONSTRAINT fk_variants_product
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE;

ALTER TABLE variants
    ADD CONSTRAINT fk_variants_image
    FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE SET NULL;

-- Indexes for better performance
CREATE INDEX idx_variants_product_id ON variants(product_id);
CREATE INDEX idx_variants_image_id ON variants(image_id);