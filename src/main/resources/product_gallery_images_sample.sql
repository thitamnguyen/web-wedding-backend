CREATE TABLE IF NOT EXISTS product_gallery_images (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url TEXT NOT NULL,
  sort_order INT DEFAULT 0,
  CONSTRAINT fk_product_gallery_images_product
    FOREIGN KEY (product_id) REFERENCES product_items(id)
    ON DELETE CASCADE
);

CREATE INDEX idx_product_gallery_images_product_sort
  ON product_gallery_images (product_id, sort_order, id);
