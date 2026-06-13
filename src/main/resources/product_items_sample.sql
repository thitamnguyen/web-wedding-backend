CREATE TABLE IF NOT EXISTS product_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  category_key VARCHAR(80) NOT NULL,
  category_label VARCHAR(120),
  excerpt TEXT,
  content LONGTEXT,
  cover_image_url VARCHAR(500),
  gallery_images TEXT,
  price_range VARCHAR(120),
  badge VARCHAR(120),
  published_at DATETIME,
  published BIT DEFAULT 1
);

ALTER TABLE product_items
  ADD COLUMN IF NOT EXISTS photographer_id BIGINT NULL;

ALTER TABLE product_items
  ADD COLUMN IF NOT EXISTS makeup_artist_id BIGINT NULL;

INSERT INTO product_items
(title, slug, category_key, category_label, excerpt, content, cover_image_url, gallery_images, price_range, badge, published_at, published)
VALUES
(
  'Concept Nổi Bật: The Bloom Atelier',
  'concept-noi-bat-the-bloom-atelier',
  'concept-noi-bat',
  'Concept Nổi Bật',
  'Concept sang trọng kết hợp hoa tươi, ánh sáng mềm và bố cục editorial cho bộ ảnh cưới mang cảm giác điện ảnh.',
  '<p>The Bloom Atelier là concept được thiết kế cho các cặp đôi thích sự thanh lịch, nữ tính và có chiều sâu cảm xúc. Bối cảnh được dựng như một studio nghệ thuật với lớp hoa và vải mềm đan xen.</p><h2>Điểm nhấn</h2><p>Tập trung vào ánh sáng tự nhiên, phông nền sáng, hoa tươi và chuyển động váy nhẹ để khung hình có nhịp thở.</p><figure><img src="https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop" alt="Concept The Bloom Atelier" /></figure><p>Phù hợp với cặp đôi muốn một bộ ảnh vừa hiện đại vừa lãng mạn.</p>',
  'https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop',
  'Từ 12.500.000đ',
  'Bán chạy',
  '2026-06-06 10:00:00',
  1
),
(
  'Concept Nổi Bật: Minimal White Room',
  'concept-noi-bat-minimal-white-room',
  'concept-noi-bat',
  'Concept Nổi Bật',
  'Concept tối giản với nền trắng, đường nét sạch và điểm nhấn ánh mắt, phù hợp cặp đôi thích sự tinh gọn.',
  '<p>Minimal White Room giữ tinh thần tối giản nhưng không lạnh. Mọi chi tiết được tiết chế để cảm xúc của hai bạn trở thành trung tâm của khung hình.</p><h2>Phong cách</h2><p>Trang phục tinh giản, make up trong trẻo và góc máy ngang thấp tạo cảm giác thời trang.</p><figure><img src="https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop" alt="Concept Minimal White Room" /></figure>',
  'https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1518131678677-a2d10239bd2d?q=80&w=1600&auto=format&fit=crop',
  'Từ 9.800.000đ',
  'Mới',
  '2026-06-05 10:00:00',
  1
),
(
  'Album Pre Wedding: Da Nang Sunset Story',
  'album-pre-wedding-da-nang-sunset-story',
  'album-pre-wedding',
  'Album Pre Wedding',
  'Bộ album ngoại cảnh tại Đà Nẵng với lịch chụp bình minh, biển và hoàng hôn tạo cảm giác rất điện ảnh.',
  '<p>Da Nang Sunset Story là gói chụp dành cho các cặp đôi muốn kết hợp nhiều bối cảnh trong cùng một ngày. Từ biển, cầu và resort đến hoàng hôn cuối ngày.</p><h2>Lịch trình</h2><p>Bình minh ở biển, trưa chụp nhẹ trong không gian nghỉ dưỡng và kết ở đồi hoặc ven biển khi ánh sáng dịu xuống.</p><figure><img src="https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop" alt="Album Pre Wedding Da Nang Sunset Story" /></figure>',
  'https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop',
  'Từ 18.500.000đ',
  'Ngoại cảnh',
  '2026-06-06 09:30:00',
  1
),
(
  'Album Pre Wedding: Hoi An Heritage Walk',
  'album-pre-wedding-hoi-an-heritage-walk',
  'album-pre-wedding',
  'Album Pre Wedding',
  'Hành trình chụp tại phố cổ Hội An với gam vàng, áo dài và sự lãng mạn cổ điển.',
  '<p>Gói này khai thác chất hoài cổ của Hội An bằng những khung hình gần gũi, chậm rãi, nhiều chi tiết đời thường nhưng vẫn sang.</p><figure><img src="https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop" alt="Album Pre Wedding Hoi An Heritage Walk" /></figure>',
  'https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1518131394553-c46756843472?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1532712938310-34cb3982ef74?q=80&w=1600&auto=format&fit=crop',
  'Từ 16.900.000đ',
  'Phổ biến',
  '2026-06-05 09:00:00',
  1
),
(
  'BST Váy Cưới: Royal Pearl Collection',
  'bst-vay-cuoi-royal-pearl-collection',
  'bst-vay-cuoi',
  'BST Váy Cưới',
  'Bộ sưu tập váy cưới dáng công chúa, đính ngọc trai và chất liệu cao cấp cho lễ cưới trang trọng.',
  '<p>Royal Pearl Collection được chọn theo tinh thần sang trọng và nổi bật trên sân khấu cưới. Phom váy được xử lý để tôn dáng nhưng vẫn giữ chuyển động nhẹ.</p><figure><img src="https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop" alt="BST Váy Cưới Royal Pearl Collection" /></figure>',
  'https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop',
  'Từ 8.900.000đ',
  'Cao cấp',
  '2026-06-04 11:00:00',
  1
),
(
  'BST Váy Cưới: Silk Satin Modern Line',
  'bst-vay-cuoi-silk-satin-modern-line',
  'bst-vay-cuoi',
  'BST Váy Cưới',
  'Dòng váy tối giản dành cho cô dâu thích vẻ đẹp hiện đại, thanh lịch và tinh tế.',
  '<p>Silk Satin Modern Line tập trung vào phom rơi tự nhiên, điểm nhấn ở vai, cổ và chất liệu lụa phản sáng vừa đủ để lên ảnh đẹp.</p><figure><img src="https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop" alt="BST Váy Cưới Silk Satin Modern Line" /></figure>',
  'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop',
  'Từ 6.500.000đ',
  'Best Seller',
  '2026-06-03 10:15:00',
  1
),
(
  'Album Phóng sự cưới: Wedding Day Motion',
  'album-phong-su-cuoi-wedding-day-motion',
  'album-phong-su-cuoi',
  'Album Phóng sự cưới',
  'Bộ ảnh phóng sự cưới ghi lại lễ gia tiên, nghi thức và cảm xúc thực tế trong ngày cưới.',
  '<p>Wedding Day Motion không tập trung vào tạo dáng quá nhiều mà chú trọng kể chuyện, từ lúc chuẩn bị đến khoảnh khắc hai gia đình gặp nhau.</p><figure><img src="https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop" alt="Album Phóng sự cưới Wedding Day Motion" /></figure>',
  'https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1600&auto=format&fit=crop',
  'Từ 14.500.000đ',
  'Phóng sự',
  '2026-06-06 08:30:00',
  1
),
(
  'Album Phóng sự cưới: Documentary Signature',
  'album-phong-su-cuoi-documentary-signature',
  'album-phong-su-cuoi',
  'Album Phóng sự cưới',
  'Gói phóng sự theo phong cách documentary, ưu tiên ánh sáng thật và cảm xúc không sắp đặt.',
  '<p>Documentary Signature cho ra bộ ảnh có nhịp điệu tự nhiên, thích hợp với cặp đôi muốn lưu giữ đúng không khí của ngày cưới.</p><figure><img src="https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop" alt="Album Phóng sự cưới Documentary Signature" /></figure>',
  'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop',
  'Từ 16.800.000đ',
  'Mới',
  '2026-06-05 08:45:00',
  1
),
(
  'Bridal Makeup: Soft Glow Signature',
  'bridal-makeup-soft-glow-signature',
  'bridal-makeup',
  'Bridal Makeup',
  'Phong cách makeup trong trẻo, sáng da và nổi bật nét đẹp tự nhiên của cô dâu.',
  '<p>Soft Glow Signature là phong cách phù hợp nhất cho cô dâu thích vẻ đẹp tươi sáng, nhẹ nhàng nhưng vẫn lên hình rất rõ.</p><figure><img src="https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop" alt="Bridal Makeup Soft Glow Signature" /></figure>',
  'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop',
  'Từ 3.500.000đ',
  'Hot',
  '2026-06-06 11:30:00',
  1
),
(
  'Bridal Makeup: Editorial Luxe Skin',
  'bridal-makeup-editorial-luxe-skin',
  'bridal-makeup',
  'Bridal Makeup',
  'Makeup theo hướng editorial, sang hơn, nét hơn và phù hợp chụp studio hoặc concept hiện đại.',
  '<p>Editorial Luxe Skin sử dụng nền da mịn, khối nhẹ và điểm nhấn mắt tinh tế để tạo ra vẻ đẹp thời trang hơn.</p><figure><img src="https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop" alt="Bridal Makeup Editorial Luxe Skin" /></figure>',
  'https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop',
  'https://images.unsplash.com/photo-1515934751635-c81c6bc9a2d8?q=80&w=1600&auto=format&fit=crop,https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=1600&auto=format&fit=crop',
  'Từ 4.200.000đ',
  'Studio',
  '2026-06-04 12:00:00',
  1
);

UPDATE product_items SET photographer_id = 1 WHERE slug IN ('concept-noi-bat-the-bloom-atelier', 'album-phong-su-cuoi-wedding-day-motion');
UPDATE product_items SET photographer_id = 2 WHERE slug IN ('concept-noi-bat-minimal-white-room', 'album-phong-su-cuoi-documentary-signature');
UPDATE product_items SET photographer_id = 3 WHERE slug IN ('album-pre-wedding-da-nang-sunset-story');
UPDATE product_items SET photographer_id = 4 WHERE slug IN ('album-pre-wedding-hoi-an-heritage-walk');
UPDATE product_items SET makeup_artist_id = 1 WHERE slug = 'bridal-makeup-soft-glow-signature';
UPDATE product_items SET makeup_artist_id = 2 WHERE slug = 'bridal-makeup-editorial-luxe-skin';
