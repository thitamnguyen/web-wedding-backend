CREATE TABLE IF NOT EXISTS blog_posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  category VARCHAR(50) NOT NULL,
  category_label VARCHAR(100),
  excerpt TEXT,
  content LONGTEXT,
  cover_image_url VARCHAR(500),
  published_at DATETIME,
  author_name VARCHAR(120),
  author_title VARCHAR(160),
  read_time_minutes INT,
  tags TEXT,
  published BIT DEFAULT 1
);

INSERT INTO blog_posts
(title, slug, category, category_label, excerpt, content, cover_image_url, published_at, author_name, author_title, read_time_minutes, tags, published)
VALUES
(
  '7 khoảnh khắc vàng để chụp ảnh cưới ngoài trời tại Đà Nẵng',
  '7-khoanh-khac-vang-de-chup-anh-cuoi-ngoai-troi-tai-da-nang',
  'dia-diem',
  'Địa điểm',
  'Một bài viết thực tế về thời điểm, ánh sáng và bố cục giúp bộ ảnh cưới ngoài trời đẹp tự nhiên hơn.',
  '<p>Đà Nẵng có lợi thế hiếm: biển, cầu, phố hiện đại và những resort biệt lập chỉ cách nhau vài phút di chuyển. Nếu biết chọn đúng khung giờ, cùng một buổi chụp có thể cho ra nhiều cảm xúc khác nhau mà vẫn giữ được sự tự nhiên của câu chuyện tình yêu.</p>

<h2>1. Bình minh ở bãi biển</h2>
<p>Buổi sáng sớm là thời điểm ánh sáng mềm, gió chưa quá mạnh và bãi biển còn trống. Đây là lúc phù hợp để chụp những khung hình gần gũi, tối giản và tập trung vào cảm xúc của hai bạn.</p>

<figure>
  <img src="https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop" alt="Cặp đôi chụp ảnh cưới lúc bình minh bên bãi biển" />
  <figcaption>Ánh sáng sớm giúp da lên màu đẹp và hạn chế bóng gắt.</figcaption>
</figure>

<h2>2. Hoàng hôn tại các điểm cao</h2>
<p>Nếu thích cảm giác điện ảnh hơn, hoàng hôn ở những điểm cao hoặc khu vực ven biển có đường chân trời thoáng sẽ tạo chiều sâu tốt. Lúc này, tóc, váy và veil chuyển động rất đẹp trong gió.</p>

<blockquote>Đôi khi một khung cảnh đẹp chưa đủ, điều làm ảnh cưới sống động là khoảng thời gian và cách hai bạn tương tác với nhau.</blockquote>

<h2>3. Kết hợp nhiều bối cảnh trong cùng một ngày</h2>
<p>Đi biển vào sáng sớm, chuyển sang studio hoặc resort vào trưa, rồi khép lại bằng khung cảnh hoàng hôn là một lịch trình rất hợp lý. Ekip có thể kiểm soát ánh sáng tốt hơn và bộ ảnh sẽ đa dạng hơn mà không bị rời rạc.</p>',
  'https://images.unsplash.com/photo-1522673607200-1648832cee98?q=80&w=1800&auto=format&fit=crop',
  '2026-06-06 09:00:00',
  'Nguyễn Minh Anh',
  'Senior Wedding Photographer',
  7,
  'Địa điểm,Đà Nẵng,Chụp ảnh cưới,Ánh sáng tự nhiên',
  1
),
(
  'Cách chọn concept cưới phù hợp với tính cách hai bạn',
  'cach-chon-concept-cuoi-phu-hop-voi-tinh-cach-hai-ban',
  'concept',
  'Concept',
  'Concept đẹp không chỉ nằm ở màu sắc mà còn ở việc nó phản chiếu đúng cá tính, nhịp sống và câu chuyện của cặp đôi.',
  '<p>Một bộ ảnh cưới chỉ thật sự bền khi concept không tách rời con người của hai bạn. Trước khi chọn màu chủ đạo hay đạo cụ, hãy bắt đầu từ điều đơn giản hơn: bạn muốn bộ ảnh mang cảm giác lãng mạn, tối giản, trẻ trung hay sang trọng?</p>

<h2>Concept nên bắt đầu từ câu chuyện</h2>
<p>Nếu cả hai quen nhau trong những chuyến đi, concept có thể là editorial travel với váy nhẹ, vest gọn và bối cảnh mở. Nếu hai bạn thích sự tinh tế, studio ánh sáng trắng cùng phông nền trơn sẽ giúp cảm xúc trở nên tập trung hơn.</p>

<figure>
  <img src="https://images.unsplash.com/photo-1519225495810-7517c33c2178?q=80&w=1600&auto=format&fit=crop" alt="Cô dâu chú rể trong concept cưới tối giản" />
  <figcaption>Concept tối giản phù hợp với cặp đôi thích cảm giác sang và tinh gọn.</figcaption>
</figure>

<h2>Đồng bộ trang phục với mood ảnh</h2>
<p>Trang phục là một phần rất lớn của concept. Váy công chúa sẽ hợp với không gian rộng, nhiều lớp trang trí. Ngược lại, váy satin hoặc suit tối màu sẽ đẹp hơn trong những layout hiện đại, ít chi tiết.</p>

<ul>
  <li>Chọn bảng màu trước rồi mới chọn trang phục.</li>
  <li>Giữ tối đa 2 đến 3 màu chủ đạo để ảnh không bị rối.</li>
  <li>Trao đổi trước với stylist để tránh các phụ kiện lệch tông.</li>
</ul>',
  'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?q=80&w=1800&auto=format&fit=crop',
  '2026-06-05 09:00:00',
  'Lê Thu Hà',
  'Creative Director',
  6,
  'Concept,Phong cách,Trang phục,Editorial',
  1
),
(
  'Checklist chuẩn bị trước buổi chụp pre-wedding',
  'checklist-chuan-bi-truoc-buoi-chup-pre-wedding',
  'kinh-nghiem',
  'Chia sẻ kinh nghiệm',
  'Một checklist ngắn giúp cô dâu chú rể giảm căng thẳng và buổi chụp diễn ra trơn tru hơn.',
  '<p>Buổi chụp pre-wedding thường kéo dài hơn dự kiến nếu thiếu sự chuẩn bị. Chỉ cần vài việc nhỏ được chốt trước, ekip sẽ tiết kiệm rất nhiều thời gian và hai bạn cũng thoải mái hơn khi đứng trước ống kính.</p>

<h2>Trước ngày chụp 3 đến 5 ngày</h2>
<p>Hãy thử lại toàn bộ trang phục, kiểm tra giày, phụ kiện và lịch di chuyển. Nếu có concept yêu cầu makeup đặc biệt hoặc tóc búi cao, nên hẹn thử trước để hạn chế thay đổi sát giờ.</p>

<figure>
  <img src="https://images.unsplash.com/photo-1519741497674-611481863552?q=80&w=1600&auto=format&fit=crop" alt="Chuẩn bị trang phục trước buổi chụp pre-wedding" />
  <figcaption>Sự chuẩn bị tốt giúp buổi chụp diễn ra nhanh và ít phát sinh.</figcaption>
</figure>

<h2>Trong ngày chụp</h2>
<p>Ăn sáng nhẹ, uống đủ nước và mang theo khăn giấy, kẹp tóc, bông tai dự phòng. Nếu thời tiết thay đổi, ekip có thể đổi thứ tự set chụp để giữ chất lượng ánh sáng tốt nhất.</p>

<h2>Sau buổi chụp</h2>
<p>Hãy thống nhất sớm về timeline hậu kỳ, số lượng ảnh chọn và phong cách retouch. Điều này giúp toàn bộ quá trình sau đó rõ ràng hơn và không bị kéo dài.</p>',
  'https://images.unsplash.com/photo-1529634806980-85c3dd6d34ac?q=80&w=1800&auto=format&fit=crop',
  '2026-06-04 09:00:00',
  'Trần Gia Huy',
  'Wedding Planner',
  5,
  'Kinh nghiệm,Checklist,Pre-wedding,Chuẩn bị',
  1
),
(
  'Vì sao album cưới có ảnh cận cảnh lại chạm cảm xúc hơn',
  'vi-sao-album-cuoi-co-anh-can-canh-lai-cham-cam-xuc-hon',
  'cam-xuc',
  'Cảm xúc',
  'Ảnh cận cảnh không chỉ đẹp về mặt thị giác, mà còn là nơi lưu lại những chi tiết rất thật của ngày cưới.',
  '<p>Trong một album cưới đầy đủ, những bức ảnh cận cảnh thường là phần khiến người xem dừng lại lâu nhất. Đó có thể là ánh mắt, nụ cười nhỏ, chiếc nhẫn, hay bàn tay nắm nhẹ trong khoảnh khắc cả hai chưa kịp tạo dáng.</p>

<h2>Chi tiết tạo nên ký ức</h2>
<p>Ảnh toàn cảnh cho bạn thấy bối cảnh, nhưng ảnh cận lại cho thấy cảm xúc. Khi kết hợp hai lớp hình ảnh này trong cùng một câu chuyện, album sẽ có nhịp điệu tốt hơn và đọng lại lâu hơn.</p>

<figure>
  <img src="https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?q=80&w=1600&auto=format&fit=crop" alt="Khoảnh khắc cận cảnh trong album cưới" />
  <figcaption>Những chi tiết nhỏ thường là thứ khiến album trở nên chân thật.</figcaption>
</figure>

<h2>Đừng chỉ chụp để đủ số lượng</h2>
<p>Hãy để nhiếp ảnh gia có thời gian quan sát những hành động tự nhiên: chỉnh tóc, cài khuy áo, nhìn nhau trước khi cười. Những khoảnh khắc đó thường mang giá trị cảm xúc cao hơn những pose quá hoàn hảo.</p>',
  'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?q=80&w=1800&auto=format&fit=crop',
  '2026-06-03 09:00:00',
  'Mai Thanh Tâm',
  'Lead Retoucher',
  4,
  'Cảm xúc,Album cưới,Chi tiết,Khoảnh khắc',
  1
);
